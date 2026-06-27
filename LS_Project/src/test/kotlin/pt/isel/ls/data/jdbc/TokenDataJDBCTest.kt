package pt.isel.ls.data.jdbc

import pt.isel.ls.domain.Token
import java.sql.Connection
import java.sql.Statement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class TokenDataJDBCTest {
    private val dbManager = DBManager()

    private fun getConnection(): Connection {
        val connection = dbManager.getConnection()
        connection.autoCommit = false
        return connection
    }

    private fun createDummyUser(connection: Connection): Int {
        var userId = -1
        val sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)"
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { st ->
            st.setString(1, "Token Test User")
            st.setString(2, "user_${Uuid.random()}@isel.pt")
            st.setString(3, "password")
            st.executeUpdate()
            val rs = st.generatedKeys
            if (rs.next()) userId = rs.getInt(1)
        }
        return userId
    }

    @Test
    fun `createToken generates a new valid token for an existing user`() {
        val connection = getConnection()
        try {
            val tokenData = TokenDataJDBC(dbManager, connection)
            val uid = createDummyUser(connection)

            val token = tokenData.createToken(uid)

            assertNotNull(token)
            assertEquals(uid, token.uid)

            val retrieved = tokenData.getUserByToken(token.token.toString())
            assertNotNull(retrieved)
            assertEquals(token.token, retrieved.token)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `createToken replaces the existing token if the user already has one`() {
        val connection = getConnection()
        try {
            val tokenData = TokenDataJDBC(dbManager, connection)
            val uid = createDummyUser(connection)

            val firstToken = tokenData.createToken(uid)
            val secondToken = tokenData.createToken(uid)

            assertNotEquals(firstToken.token, secondToken.token)
            assertNull(tokenData.getUserByToken(firstToken.token.toString()))
            val active = tokenData.getUserToken(uid)
            assertEquals(secondToken.token, active?.token)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `getUserByToken returns the correct Token object for a valid token string`() {
        val connection = getConnection()
        try {
            val tokenData = TokenDataJDBC(dbManager, connection)
            val uid = createDummyUser(connection)
            val created = tokenData.createToken(uid)

            val retrieved = tokenData.getUserByToken(created.token.toString())

            assertNotNull(retrieved)
            assertEquals(created.token, retrieved.token)
            assertEquals(uid, retrieved.uid)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `deleteToken successfully removes the token from the database`() {
        val connection = getConnection()
        try {
            val tokenData = TokenDataJDBC(dbManager, connection)
            val uid = createDummyUser(connection)
            val token = tokenData.createToken(uid)

            assertNotNull(tokenData.getUserToken(uid))

            tokenData.deleteToken(token)

            assertNull(tokenData.getUserByToken(token.token.toString()))
            assertNull(tokenData.getUserToken(uid))
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `getToken returns null when searching for a valid UUID that does not exist in DB`() {
        val connection = getConnection()
        try {
            val tokenData = TokenDataJDBC(dbManager, connection)
            val randomValidUuid = Uuid.random().toString()

            val retrieved = tokenData.getUserByToken(randomValidUuid)

            assertNull(retrieved)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `getToken returns null and handles IllegalArgumentException for an invalid UUID string`() {
        val connection = getConnection()
        try {
            val tokenData = TokenDataJDBC(dbManager, connection)
            val invalidUuidString = "not-a-valid-uuid-12345"

            val retrieved = tokenData.getUserByToken(invalidUuidString)

            assertNull(retrieved)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `getUserToken returns null if the user id has no associated token`() {
        val connection = getConnection()
        try {
            val tokenData = TokenDataJDBC(dbManager, connection)
            val uidWithoutToken = createDummyUser(connection)

            val retrieved = tokenData.getUserToken(uidWithoutToken)

            assertNull(retrieved)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `deleteToken does not throw exception when deleting a token that does not exist`() {
        val connection = getConnection()
        try {
            val tokenData = TokenDataJDBC(dbManager, connection)
            val dummyToken = Token(Uuid.random(), 9999)

            tokenData.deleteToken(dummyToken)

            assertNull(tokenData.getUserByToken(dummyToken.token.toString()))
        } finally {
            connection.rollback()
            connection.close()
        }
    }
}

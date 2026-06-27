package pt.isel.ls.data.jdbc

import pt.isel.ls.domain.User
import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName
import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class UserDataJDBCTest {
    private val dbManager = DBManager()

    private fun getConnection(): Connection {
        val connection = dbManager.getConnection()
        connection.autoCommit = false
        return connection
    }

    @Test
    fun `can create and retrieve a user`() {
        val connection = getConnection()
        try {
            val userData = UserDataJDBC(dbManager, connection)
            val email = "test_${Uuid.random()}@isel.pt"
            val user =
                User(
                    uid = 0,
                    name = UserName("Test User"),
                    email = Email(email),
                    password = Password("strongpassword"),
                )

            val generatedId = userData.createUser(user)
            val retrievedUser = userData.getUser(generatedId)

            assertNotNull(retrievedUser)
            assertEquals("Test User", retrievedUser.name.value)
            assertEquals(email, retrievedUser.email.value)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `cannot create two users with the same email`() {
        val connection = getConnection()
        try {
            val userData = UserDataJDBC(dbManager, connection)
            val emailStr = "duplicate_${Uuid.random()}@isel.pt"
            val email = Email(emailStr)

            val user1 = User(0, UserName("U111111"), email, Password("p111111"))
            val user2 = User(0, UserName("U222222"), email, Password("p222222"))

            userData.createUser(user1)
            assertFails {
                userData.createUser(user2)
            }
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `updateUser successfully changes the user name`() {
        val connection = getConnection()
        try {
            val userData = UserDataJDBC(dbManager, connection)
            val email = "update_${Uuid.random()}@isel.pt"
            val originalUser = User(0, UserName("Old Name"), Email(email), Password("pass123"))

            val id = userData.createUser(originalUser)
            val savedUser = userData.getUser(id)
            assertNotNull(savedUser)

            val newName = "New Name"
            val updatedUser = userData.updateUser(savedUser, newName)

            assertNotNull(updatedUser)
            assertEquals(newName, updatedUser.name.value)

            val retrieved = userData.getUser(id)
            assertEquals(newName, retrieved?.name?.value)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `deleteUser successfully removes the user`() {
        val connection = getConnection()
        try {
            val userData = UserDataJDBC(dbManager, connection)
            val email = "delete_${Uuid.random()}@isel.pt"
            val user = User(0, UserName("To Delete"), Email(email), Password("pass123"))

            val id = userData.createUser(user)
            assertNotNull(userData.getUser(id))

            userData.deleteUser(id)
            assertNull(userData.getUser(id))
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `can handle special characters in strings safely`() {
        val connection = getConnection()
        try {
            val userData = UserDataJDBC(dbManager, connection)
            val trickyName = "O'Connor OR 1=1; --"
            val email = "sql_${Uuid.random()}@isel.pt"
            val user = User(0, UserName(trickyName), Email(email), Password("injected"))

            val id = userData.createUser(user)
            val retrieved = userData.getUser(id)

            assertNotNull(retrieved)
            assertEquals(trickyName, retrieved.name.value)
        } finally {
            connection.rollback()
            connection.close()
        }
    }
}

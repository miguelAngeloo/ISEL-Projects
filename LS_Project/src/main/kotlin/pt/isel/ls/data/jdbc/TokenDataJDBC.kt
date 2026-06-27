package pt.isel.ls.data.jdbc

import pt.isel.ls.data.TokenData
import pt.isel.ls.domain.Token
import java.sql.Connection
import java.sql.Statement
import kotlin.uuid.Uuid

class TokenDataJDBC(
    private val dbManager: DBManager,
    private val connection: Connection? = null,
) : TokenData {
    override fun createToken(uid: Int): Token =
        dbManager.execute(connection) { conn ->
            conn.prepareStatement("DELETE FROM tokens WHERE user_id = ?").use { st ->
                st.setInt(1, uid)
                st.executeUpdate()
            }

            val sql = "INSERT INTO tokens (token, user_id) VALUES (?::uuid, ?)"
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { statement ->
                val newToken = Uuid.random()
                statement.setString(1, newToken.toString())
                statement.setInt(2, uid)
                statement.executeUpdate()

                val rs = statement.generatedKeys
                if (rs.next()) {
                    Token(newToken, uid)
                } else {
                    throw IllegalStateException("Failed to retrieve generated keys")
                }
            }
        }

    override fun getUserByToken(token: String): Token? =
        try {
            val validUuid = Uuid.parse(token)
            dbManager.execute(connection) { conn ->
                conn.prepareStatement(
                    "SELECT user_id FROM tokens WHERE token = ?::uuid",
                ).use { statement ->
                    statement.setString(1, validUuid.toString())
                    val rs = statement.executeQuery()
                    if (rs.next()) {
                        Token(
                            token = Uuid.parse(token),
                            uid = rs.getInt("user_id"),
                        )
                    } else {
                        null
                    }
                }
            }
        } catch (_: IllegalArgumentException) {
            null
        }

    override fun getUserToken(uid: Int): Token? =
        dbManager.execute(connection) { conn ->
            conn.prepareStatement(
                "SELECT token FROM tokens WHERE user_id = ?",
            ).use { statement ->
                statement.setInt(1, uid)
                val rs = statement.executeQuery()
                if (rs.next()) {
                    Token(
                        token = Uuid.parse(rs.getString("token")),
                        uid = uid,
                    )
                } else {
                    null
                }
            }
        }

    override fun deleteToken(token: Token): Unit =
        dbManager.execute(connection) { conn ->
            conn.prepareStatement(
                "DELETE FROM tokens WHERE token = ?::uuid",
            ).use { statement ->
                statement.setString(1, token.token.toString())
                statement.executeUpdate()
            }
        }
}

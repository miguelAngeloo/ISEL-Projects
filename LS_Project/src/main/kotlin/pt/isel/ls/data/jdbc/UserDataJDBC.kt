package pt.isel.ls.data.jdbc

import pt.isel.ls.data.UserData
import pt.isel.ls.domain.User
import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class UserDataJDBC(
    private val dbManager: DBManager,
    private val connection: Connection? = null,
) : UserData {
    override fun createUser(user: User): Int =
        dbManager.execute(connection) { conn ->
            val sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)"
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.setString(1, user.name.value)
                statement.setString(2, user.email.value)
                statement.setString(3, user.password.value)
                statement.executeUpdate()

                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    generatedKeys.getInt(1)
                } else {
                    throw IllegalStateException("No ID obtained.")
                }
            }
        }

    override fun getUser(uid: Int): User? =
        dbManager.execute(connection) { conn ->
            conn.prepareStatement(
                "SELECT id, name, email, password FROM USERS WHERE id = ?",
            ).use { statement ->
                statement.setInt(1, uid)
                val rs = statement.executeQuery()
                if (rs.next()) mapResultSetToUser(rs) else null
            }
        }

    override fun getUser(email: String): User? =
        dbManager.execute(connection) { conn ->
            conn.prepareStatement(
                "SELECT id, name, email, password FROM USERS WHERE email = ?",
            ).use { statement ->
                statement.setString(1, email)
                val rs = statement.executeQuery()
                if (rs.next()) mapResultSetToUser(rs) else null
            }
        }

    override fun updateUser(
        user: User,
        name: String,
    ): User? =
        dbManager.execute(connection) { conn ->
            conn.prepareStatement(
                "UPDATE USERS SET name = ? WHERE id = ?",
            ).use { statement ->
                statement.setString(1, name)
                statement.setInt(2, user.uid)
                val rowsUpdated = statement.executeUpdate()
                if (rowsUpdated > 0) user.copy(name = UserName(name)) else null
            }
        }

    override fun deleteUser(uid: Int): Unit =
        dbManager.execute(connection) { conn ->
            conn.prepareStatement(
                "DELETE FROM USERS WHERE id = ?",
            ).use { statement ->
                statement.setInt(1, uid)
                statement.executeUpdate()
            }
        }

    private fun mapResultSetToUser(rs: ResultSet): User =
        User(
            uid = rs.getInt("id"),
            name = UserName(rs.getString("name")),
            email = Email(rs.getString("email")),
            password = Password(rs.getString("password")),
        )
}

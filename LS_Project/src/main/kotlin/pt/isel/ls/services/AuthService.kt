package pt.isel.ls.services

import org.mindrot.jbcrypt.BCrypt
import pt.isel.ls.data.StorageDataManager
import pt.isel.ls.domain.Token
import pt.isel.ls.domain.User
import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName
import pt.isel.ls.exceptions.AlreadyExistsException
import pt.isel.ls.exceptions.UnauthorizedException

class AuthService(
    dataManager: StorageDataManager,
) {
    private val userStorage = dataManager.userData
    private val tokenStorage = dataManager.tokenData

    fun login(
        email: String,
        password: String,
    ): Token {
        val user =
            userStorage.getUser(email)
                ?: throw UnauthorizedException("Invalid credentials")

        if (!passwordMatches(password, user.password.value)) {
            throw UnauthorizedException("Invalid credentials")
        }

        val token = tokenStorage.createToken(user.uid)

        return token
    }

    fun logout(tokenString: String) {
        val tokenObj =
            tokenStorage.getUserByToken(tokenString)
                ?: throw UnauthorizedException("Invalid or already expired token")

        tokenStorage.deleteToken(tokenObj)
    }

    fun register(
        name: UserName,
        email: Email,
        password: Password,
    ): Pair<Int, Token> {
        val existingUser = userStorage.getUser(email.value)
        if (existingUser != null) {
            throw AlreadyExistsException("User with email ${email.value} already exists")
        }

        val hashedPassword = Password(BCrypt.hashpw(password.value, BCrypt.gensalt()))
        val userToCreate = User(0, name, email, hashedPassword)
        val newUserId = userStorage.createUser(userToCreate)

        val token = tokenStorage.createToken(newUserId)
        return Pair(newUserId, token)
    }

    private fun passwordMatches(
        password: String,
        hashedPassword: String,
    ): Boolean =
        try {
            BCrypt.checkpw(password, hashedPassword)
        } catch (e: IllegalArgumentException) {
            false
        }
}

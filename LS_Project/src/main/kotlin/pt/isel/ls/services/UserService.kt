package pt.isel.ls.services

import pt.isel.ls.data.StorageDataManager
import pt.isel.ls.domain.User
import pt.isel.ls.domain.wrappers.UserName
import pt.isel.ls.exceptions.NotFoundException
import pt.isel.ls.exceptions.UnauthorizedException

class UserService(
    dataManager: StorageDataManager,
) {
    private val userStorage = dataManager.userData
    private val tokenStorage = dataManager.tokenData

    fun getUserDetails(uid: Int): User {
        return userStorage.getUser(uid)
            ?: throw NotFoundException("User with id $uid not found")
    }

    fun updateUser(
        tokenString: String,
        name: String,
    ): User {
        val token =
            tokenStorage.getUserByToken(tokenString)
                ?: throw UnauthorizedException("Invalid or expired token")
        val user =
            userStorage.getUser(token.uid)
                ?: throw NotFoundException("User with id ${token.uid} not found")

        userStorage.updateUser(user, name)

        return user.copy(name = UserName(name))
    }

    fun deleteUser(tokenString: String) {
        val token =
            tokenStorage.getUserByToken(tokenString)
                ?: throw UnauthorizedException("Invalid or expired token")

        val user =
            userStorage.getUser(token.uid)
                ?: throw NotFoundException("User with id ${token.uid} not found")

        userStorage.deleteUser(user.uid)

        tokenStorage.deleteToken(token)
    }
}

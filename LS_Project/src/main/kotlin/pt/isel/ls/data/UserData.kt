package pt.isel.ls.data

import pt.isel.ls.domain.User

interface UserData {
    fun createUser(user: User): Int

    fun getUser(uid: Int): User?

    fun getUser(email: String): User?

    fun updateUser(
        user: User,
        name: String,
    ): User?

    fun deleteUser(uid: Int)
}

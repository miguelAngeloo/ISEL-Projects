package pt.isel.ls.data.mem

import pt.isel.ls.data.UserData
import pt.isel.ls.domain.User
import pt.isel.ls.domain.wrappers.UserName

class UserDataMem : MemStorage(), UserData {
    override fun createUser(user: User): Int {
        val newUid = uid
        usersDB.add(user.copy(uid = newUid))

        return newUid
    }

    override fun getUser(uid: Int): User? = usersDB.find { it.uid == uid }

    override fun getUser(email: String): User? = usersDB.find { it.email.value == email }

    override fun updateUser(
        user: User,
        name: String,
    ): User? {
        val index = usersDB.indexOfFirst { it.uid == user.uid }
        if (index == -1) return null

        val newUser = user.copy(name = UserName(name))
        usersDB[index] = newUser

        return newUser
    }

    override fun deleteUser(uid: Int) {
        usersDB.removeIf { it.uid == uid }
    }
}

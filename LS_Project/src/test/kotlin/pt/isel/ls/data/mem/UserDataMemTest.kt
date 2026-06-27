package pt.isel.ls.data.mem

import pt.isel.ls.domain.User
import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserDataMemTest {
    private lateinit var userDataMem: UserDataMem

    @BeforeTest
    fun setup() {
        val memStorage = MemStorage()
        memStorage.clear()

        userDataMem = UserDataMem()
    }

    @Test
    fun `createUser assigns auto-incremented ID and stores user`() {
        val userToCreate =
            User(
                uid = 0,
                name = UserName("Alice"),
                email = Email("alice@isel.pt"),
                password = Password("secure123"),
            )

        val newUid = userDataMem.createUser(userToCreate)

        assertEquals(1, newUid)

        val storedUser = userDataMem.getUser(newUid)
        assertNotNull(storedUser)
        assertEquals(newUid, storedUser.uid)
        assertEquals("Alice", storedUser.name.value)
    }

    @Test
    fun `createUser assigns sequential IDs for multiple users`() {
        val user1 = User(0, UserName("Alice"), Email("alice@isel.pt"), Password("pwd1pwd"))
        val user2 = User(0, UserName("Bob"), Email("bob@isel.pt"), Password("pwd2pwd"))

        val uid1 = userDataMem.createUser(user1)
        val uid2 = userDataMem.createUser(user2)
        assertEquals(1, uid1)
        assertEquals(2, uid2)
    }

    @Test
    fun `getUser by ID returns correct user`() {
        val user = User(0, UserName("Charlie"), Email("charlie@isel.pt"), Password("pwd123"))
        val uid = userDataMem.createUser(user)

        val retrievedUser = userDataMem.getUser(uid)

        assertNotNull(retrievedUser)
        assertEquals(uid, retrievedUser.uid)
        assertEquals("Charlie", retrievedUser.name.value)
        assertEquals("charlie@isel.pt", retrievedUser.email.value)
    }

    @Test
    fun `getUser by ID returns null when user does not exist`() {
        val retrievedUser = userDataMem.getUser(999)
        assertNull(retrievedUser)
    }

    @Test
    fun `getUser by Email returns correct user`() {
        val user = User(0, UserName("Dave"), Email("dave@isel.pt"), Password("pwd123"))
        val uid = userDataMem.createUser(user)

        val retrievedUser = userDataMem.getUser("dave@isel.pt")

        assertNotNull(retrievedUser)
        assertEquals(uid, retrievedUser.uid)
        assertEquals("Dave", retrievedUser.name.value)
        assertEquals("dave@isel.pt", retrievedUser.email.value)
    }

    @Test
    fun `getUser by Email returns null when email does not exist`() {
        val retrievedUser = userDataMem.getUser("ghost@isel.pt")

        assertNull(retrievedUser)
    }

    @Test
    fun `updateUser modifies user name in storage and returns updated user`() {
        val user = User(0, UserName("OldName"), Email("user@isel.pt"), Password("pwd123"))
        val uid = userDataMem.createUser(user)
        val createdUser = userDataMem.getUser(uid)!!

        val updatedUser = userDataMem.updateUser(createdUser, "NewName")

        assertNotNull(updatedUser)
        assertEquals("NewName", updatedUser.name.value)

        val retrieved = userDataMem.getUser(uid)
        assertEquals("NewName", retrieved?.name?.value)
        assertEquals("user@isel.pt", retrieved?.email?.value)
    }

    @Test
    fun `updateUser returns null when user does not exist in storage`() {
        val nonExistentUser = User(999, UserName("Ghost"), Email("ghost@isel.pt"), Password("pwdpwd"))

        val result = userDataMem.updateUser(nonExistentUser, "NewName")

        assertNull(result)
    }

    @Test
    fun `deleteUser removes user from storage successfully`() {
        // Setup
        val uid =
            userDataMem.createUser(
                User(0, UserName("ToDelete"), Email("delete@isel.pt"), Password("pwd123")),
            )
        assertNotNull(userDataMem.getUser(uid))

        userDataMem.deleteUser(uid)

        assertNull(userDataMem.getUser(uid), "User should be removed from by-ID index")
        assertNull(userDataMem.getUser("delete@isel.pt"), "User should be removed from by-Email index")
    }

    @Test
    fun `deleteUser does not fail if user does not exist`() {
        userDataMem.deleteUser(888)
    }

    @Test
    fun `updateUser does not affect other users`() {
        val uid1 = userDataMem.createUser(User(0, UserName("User1"), Email("1@isel.pt"), Password("pwdpwd")))
        val uid2 = userDataMem.createUser(User(0, UserName("User2"), Email("2@isel.pt"), Password("pwdpwd")))

        val user1 = userDataMem.getUser(uid1)!!
        userDataMem.updateUser(user1, "NewName1")

        assertEquals("User2", userDataMem.getUser(uid2)?.name?.value)
    }
}

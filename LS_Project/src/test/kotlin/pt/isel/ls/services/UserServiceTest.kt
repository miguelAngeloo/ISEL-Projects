package pt.isel.ls.services

import pt.isel.ls.createTestToken
import pt.isel.ls.createTestUser
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.exceptions.NotFoundException
import pt.isel.ls.exceptions.UnauthorizedException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserServiceTest {
    private lateinit var memManager: MemManager
    private lateinit var userService: UserService

    @BeforeTest
    fun setup() {
        memManager = MemManager()
        memManager.clear()

        userService = UserService(dataManager = memManager)
    }

    @Test
    fun `get user details successfully`() {
        val uid = memManager.createTestUser("Alice", "alice@isel.pt")

        val fetchedUser = userService.getUserDetails(uid)

        assertEquals(uid, fetchedUser.uid)
        assertEquals("Alice", fetchedUser.name.value)
    }

    @Test
    fun `get user details returns correct email`() {
        val uid = memManager.createTestUser("Alice", "alice@isel.pt")

        val fetchedUser = userService.getUserDetails(uid)

        assertEquals("alice@isel.pt", fetchedUser.email.value)
    }

    @Test
    fun `get user details allows reading another user`() {
        val uidAlice = memManager.createTestUser("Alice", "alice@isel.pt")
        val uidBob = memManager.createTestUser("Bob", "bob@isel.pt")

        val fetchedUser = userService.getUserDetails(uidAlice)

        assertEquals(uidAlice, fetchedUser.uid)
        assertEquals("Alice", fetchedUser.name.value)
        assertEquals(uidBob, memManager.userData.getUser(uidBob)?.uid)
    }

    @Test
    fun `get user details throws NotFoundException when user does not exist`() {
        val ex =
            assertFailsWith<NotFoundException> {
                userService.getUserDetails(999)
            }
        assertEquals("User with id 999 not found", ex.errorCause)
    }

    @Test
    fun `getUserDetails after update shows the new name`() {
        val uid = memManager.createTestUser("Original", "orig@isel.pt")
        val token = memManager.createTestToken(uid)
        userService.updateUser(token, "Updated")

        val fetchedUser = userService.getUserDetails(uid)

        assertEquals("Updated", fetchedUser.name.value)
    }

    @Test
    fun `updateUser name successfully`() {
        val uid = memManager.createTestUser("OldName")
        val token = memManager.createTestToken(uid)

        val updatedUser = userService.updateUser(token, "NewName")

        assertEquals("NewName", updatedUser.name.value)
        assertEquals("NewName", memManager.userData.getUser(uid)?.name?.value)
    }

    @Test
    fun `updateUser preserves email`() {
        val uid = memManager.createTestUser("Alice", "alice@isel.pt")
        val token = memManager.createTestToken(uid)

        userService.updateUser(token, "NewAlice")

        val stored = memManager.userData.getUser(uid)
        assertNotNull(stored)
        assertEquals("alice@isel.pt", stored.email.value)
    }

    @Test
    fun `updateUser name with 3 characters (min limit) successfully`() {
        val token = memManager.createTestToken(memManager.createTestUser())

        val result = userService.updateUser(token, "ABC")

        assertEquals("ABC", result.name.value)
    }

    @Test
    fun `updateUser name with 20 characters (max limit) successfully`() {
        val token = memManager.createTestToken(memManager.createTestUser())
        val longName = "A".repeat(20)

        val result = userService.updateUser(token, longName)

        assertEquals(longName, result.name.value)
    }

    @Test
    fun `updateUser with invalid token throws UnauthorizedException`() {
        assertFailsWith<UnauthorizedException> { userService.updateUser("invalid-token", "NewName") }
    }

    @Test
    fun `updateUser with non-existent user for valid token throws NotFoundException`() {
        val token999 = memManager.createTestToken(999)
        assertFailsWith<NotFoundException> { userService.updateUser(token999, "NewName") }
    }

    @Test
    fun `updateUser with empty name throws IllegalArgumentException`() {
        val token = memManager.createTestToken(memManager.createTestUser())
        val ex = assertFailsWith<IllegalArgumentException> { userService.updateUser(token, "") }
        assertEquals("Username cannot be empty", ex.message)
    }

    @Test
    fun `updateUser with too short name throws IllegalArgumentException`() {
        val token = memManager.createTestToken(memManager.createTestUser())
        val ex = assertFailsWith<IllegalArgumentException> { userService.updateUser(token, "Li") }
        assertEquals("Invalid username length", ex.message)
    }

    @Test
    fun `updateUser with too long name throws IllegalArgumentException`() {
        val token = memManager.createTestToken(memManager.createTestUser())
        val ex = assertFailsWith<IllegalArgumentException> { userService.updateUser(token, "A".repeat(21)) }
        assertEquals("Invalid username length", ex.message)
    }

    @Test
    fun `updateUser with name containing many spaces that exceed limit throws IllegalArgumentException`() {
        val token = memManager.createTestToken(memManager.createTestUser())
        assertFailsWith<IllegalArgumentException> { userService.updateUser(token, "User" + " ".repeat(13) + "Name") }
    }

    @Test
    fun `deleteUser successfully removes user and their tokens`() {
        val uid = memManager.createTestUser("ToDelete")
        val token = memManager.createTestToken(uid)

        userService.deleteUser(token)

        assertNull(memManager.userData.getUser(uid))
        assertNull(memManager.tokenData.getUserByToken(token))
    }

    @Test
    fun `deleteUser with invalid token throws UnauthorizedException`() {
        assertFailsWith<UnauthorizedException> { userService.deleteUser("token-que-nao-existe") }
    }

    @Test
    fun `deleteUser when user is already gone but token exists throws NotFoundException`() {
        val token999 = memManager.createTestToken(999)

        assertFailsWith<NotFoundException> { userService.deleteUser(token999) }
    }

    @Test
    fun `after deleteUser, getUserDetails returns NotFoundException`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        userService.deleteUser(token)

        assertFailsWith<NotFoundException> { userService.getUserDetails(uid) }
    }

    @Test
    fun `deleteUser then updateUser throws UnauthorizedException`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        userService.deleteUser(token)

        assertFailsWith<UnauthorizedException> { userService.updateUser(token, "NewName") }
    }

    @Test
    fun `deleting one user does not affect other users`() {
        val uid1 = memManager.createTestUser("UserOne", "one@isel.pt")
        val uid2 = memManager.createTestUser("UserTwo", "two@isel.pt")
        val token1 = memManager.createTestToken(uid1)

        userService.deleteUser(token1)

        assertNull(memManager.userData.getUser(uid1))
        assertNotNull(memManager.userData.getUser(uid2))
    }
}

package pt.isel.ls.services

import org.mindrot.jbcrypt.BCrypt
import pt.isel.ls.createTestToken
import pt.isel.ls.createTestUser
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName
import pt.isel.ls.exceptions.AlreadyExistsException
import pt.isel.ls.exceptions.UnauthorizedException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthServiceTest {
    private lateinit var memManager: MemManager
    private lateinit var authService: AuthService

    @BeforeTest
    fun setup() {
        memManager = MemManager()
        memManager.clear()

        authService = AuthService(dataManager = memManager)
    }

    @Test
    fun `register a new user successfully`() {
        val name = UserName("Alice")
        val email = Email("alice@isel.pt")
        val password = Password("secure123")

        val (newUserId, token) = authService.register(name, email, password)

        assertTrue(newUserId > 0)
        assertNotNull(token)
        val savedUser = memManager.userData.getUser("alice@isel.pt")
        assertNotNull(savedUser)
        assertEquals(name.value, savedUser.name.value)
        assertTrue(savedUser.password.value != password.value)
        assertTrue(BCrypt.checkpw(password.value, savedUser.password.value))
    }

    @Test
    fun `register returns token whose uid matches the created user`() {
        val name = UserName("Carlos")
        val email = Email("carlos@isel.pt")
        val password = Password("secure123")

        val (newUserId, token) = authService.register(name, email, password)

        assertEquals(newUserId, token.uid)
    }

    @Test
    fun `register with an already existing email throws AlreadyExistsException`() {
        val email = "bob@isel.pt"
        memManager.createTestUser(name = "Bob", email = email)

        val exception =
            assertFailsWith<AlreadyExistsException> {
                authService.register(UserName("Bob Clone"), Email(email), Password("newpwd"))
            }
        assertEquals("User with email $email already exists", exception.message)
    }

    @Test
    fun `login successfully returns a valid token`() {
        val email = "charlie@isel.pt"
        val pass = "charliePwd"
        val uid = memManager.createTestUser(name = "Charlie", email = email, pass = pass)

        val token = authService.login(email, pass)

        assertNotNull(token)
        assertEquals(uid, token.uid)
        assertNotNull(memManager.tokenData.getUserByToken(token.token.toString()))
    }

    @Test
    fun `login creates a token that is retrievable from storage`() {
        val email = "eve@isel.pt"
        val pass = "evePwd12"
        memManager.createTestUser(name = "Eve", email = email, pass = pass)

        val token = authService.login(email, pass)

        val stored = memManager.tokenData.getUserByToken(token.token.toString())
        assertNotNull(stored)
        assertEquals(token.uid, stored.uid)
    }

    @Test
    fun `login with non-existent email throws UnauthorizedException`() {
        assertFailsWith<UnauthorizedException> {
            authService.login("ghost@isel.pt", "ghostPwd")
        }
    }

    @Test
    fun `login with wrong password throws UnauthorizedException`() {
        val email = "dave@isel.pt"
        memManager.createTestUser(name = "Dave", email = email, pass = "realPwd")

        assertFailsWith<UnauthorizedException> {
            authService.login(email, "WRONG_PWD")
        }
    }

    @Test
    fun `register then login both work for the same user`() {
        val name = UserName("Frank")
        val email = Email("frank@isel.pt")
        val password = Password("frankpw")

        authService.register(name, email, password)
        val loginToken = authService.login("frank@isel.pt", "frankpw")

        assertNotNull(loginToken)
        assertNotNull(memManager.tokenData.getUserByToken(loginToken.token.toString()))
    }

    @Test
    fun `logout successfully removes token from storage`() {
        val uid = memManager.createTestUser()
        val tokenString = memManager.createTestToken(uid)

        authService.logout(tokenString)

        assertNull(memManager.tokenData.getUserByToken(tokenString))
    }

    @Test
    fun `logout with invalid or already deleted token throws UnauthorizedException`() {
        assertFailsWith<UnauthorizedException> {
            authService.logout("fake-or-deleted-token-string")
        }
    }

    @Test
    fun `double logout throws UnauthorizedException on the second call`() {
        // Arrange
        val uid = memManager.createTestUser()
        val tokenString = memManager.createTestToken(uid)

        authService.logout(tokenString)

        assertFailsWith<UnauthorizedException> {
            authService.logout(tokenString)
        }
    }

    @Test
    fun `UserName with invalid lengths throws IllegalArgumentException`() {
        val names = listOf("us" + " ".repeat(20) + "er1", "Ab")

        names.forEach { name ->
            val ex = assertFailsWith<IllegalArgumentException> { UserName(name) }
            assertEquals("Invalid username length", ex.message)
        }
    }

    @Test
    fun `Email with invalid format throws IllegalArgumentException`() {
        val invalidEmails = listOf("email-sem-arroba", "email@sem-ponto", "@sem-prefixo.com")

        invalidEmails.forEach { emailStr ->
            assertFailsWith<IllegalArgumentException>(message = "Failed for $emailStr") {
                Email(emailStr)
            }
        }
    }

    @Test
    fun `Password too short throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            Password("12345")
        }
    }
}

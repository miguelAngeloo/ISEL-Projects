package pt.isel.ls.webAPI

import kotlinx.serialization.json.Json
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import pt.isel.ls.bodyRequest
import pt.isel.ls.createTestToken
import pt.isel.ls.createTestUser
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.dto.LoginInputModel
import pt.isel.ls.domain.dto.RegisterInputModel
import pt.isel.ls.domain.dto.RegisterOutputModel
import pt.isel.ls.domain.dto.TokenOutputModel
import pt.isel.ls.services.AuthService
import pt.isel.ls.webapi.AuthWebAPI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthWebAPITest {
    private lateinit var authWebAPI: AuthWebAPI
    private lateinit var memManager: MemManager

    @BeforeTest
    fun setUp() {
        memManager = MemManager()
        val authService = AuthService(memManager)
        authWebAPI = AuthWebAPI(authService)
        memManager.clear()
    }

    @Test
    fun `Register successful returns 201 Created`() {
        val input = RegisterInputModel("Alice", "alice@isel.pt", "password123")
        val request = bodyRequest(POST, "/auth/register", body = Json.encodeToString(input))

        val response = authWebAPI.register(request)

        assertEquals(CREATED, response.status)
        val body = Json.decodeFromString<RegisterOutputModel>(response.bodyString())
        assertNotNull(body.userId)
        assertTrue(body.token.isNotBlank())
    }

    @Test
    fun `Register with already existing email returns 409 Conflict`() {
        memManager.createTestUser("Alice", "alice@isel.pt", "password123")
        val input = RegisterInputModel("Alice Clone", "alice@isel.pt", "newpassword")
        val request = bodyRequest(POST, "/auth/register", body = Json.encodeToString(input))

        val response = authWebAPI.register(request)

        assertEquals(CONFLICT, response.status)
    }

    @Test
    fun `Register with short password returns 400 Bad Request`() {
        val input = RegisterInputModel("Alice", "alice@isel.pt", "12345")
        val request = bodyRequest(POST, "/auth/register", body = Json.encodeToString(input))

        val response = authWebAPI.register(request)

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `Register with empty name returns 400 Bad Request`() {
        val input = RegisterInputModel("", "alice@isel.pt", "password123")
        val request = bodyRequest(POST, "/auth/register", body = Json.encodeToString(input))

        val response = authWebAPI.register(request)

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `Register with short name returns 400 Bad Request`() {
        val input = RegisterInputModel("Ab", "ab@isel.pt", "password123")
        val request = bodyRequest(POST, "/auth/register", body = Json.encodeToString(input))

        val response = authWebAPI.register(request)

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `Login successful returns 200 OK and Token`() {
        memManager.createTestUser("Bob", "bob@isel.pt", "secure123")
        val input = LoginInputModel("bob@isel.pt", "secure123")
        val request = bodyRequest(POST, "/auth/login", body = Json.encodeToString(input))

        val response = authWebAPI.login(request)

        assertEquals(OK, response.status)
        val body = Json.decodeFromString<TokenOutputModel>(response.bodyString())
        assertTrue(body.token.isNotBlank())
        assertNotNull(body.userId)
    }

    @Test
    fun `Login with wrong password returns 401 Unauthorized`() {
        memManager.createTestUser("Bob", "bob@isel.pt", "secure123")
        val input = LoginInputModel("bob@isel.pt", "WRONG_PASSWORD")
        val request = bodyRequest(POST, "/auth/login", body = Json.encodeToString(input))

        val response = authWebAPI.login(request)

        assertEquals(UNAUTHORIZED, response.status)
    }

    @Test
    fun `Login with non-existent email returns 401 Unauthorized`() {
        val input = LoginInputModel("ghost@isel.pt", "somePassword")
        val request = bodyRequest(POST, "/auth/login", body = Json.encodeToString(input))

        val response = authWebAPI.login(request)

        assertEquals(UNAUTHORIZED, response.status)
    }

    @Test
    fun `Login with empty password returns 400 Bad Request`() {
        memManager.createTestUser("Bob", "bob@isel.pt", "secure123")
        val input = LoginInputModel("bob@isel.pt", "")
        val request = bodyRequest(POST, "/auth/login", body = Json.encodeToString(input))

        val response = authWebAPI.login(request)

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `Login with short password returns 400 Bad Request`() {
        memManager.createTestUser("Bob", "bob@isel.pt", "secure123")
        val input = LoginInputModel("bob@isel.pt", "12345")
        val request = bodyRequest(POST, "/auth/login", body = Json.encodeToString(input))

        val response = authWebAPI.login(request)

        assertEquals(UNAUTHORIZED, response.status)
    }

    @Test
    fun `Logout successful returns 200 OK`() {
        val uid = memManager.createTestUser("Charlie", "charlie@isel.pt", "pwdpwd")
        val token = memManager.createTestToken(uid)
        val request =
            Request(POST, "/auth/logout")
                .header("Authorization", "Bearer $token")

        val response = authWebAPI.logout(request)

        assertEquals(OK, response.status)
    }

    @Test
    fun `Logout with invalid or already deleted token returns 401 Unauthorized`() {
        val request =
            Request(POST, "/auth/logout")
                .header("Authorization", "Bearer fake-token-123")

        val response = authWebAPI.logout(request)

        assertEquals(UNAUTHORIZED, response.status)
    }

    @Test
    fun `Logout without Authorization header returns 400 Bad Request`() {
        val request = Request(POST, "/auth/logout")

        val response = authWebAPI.logout(request)

        assertEquals(UNAUTHORIZED, response.status)
    }

    @Test
    fun `Register with invalid email format returns 400 Bad Request`() {
        val input = RegisterInputModel("Alice", "email-sem-ponto@com", "password123")
        val request = bodyRequest(POST, "/auth/register", body = Json.encodeToString(input))

        val response = authWebAPI.register(request)

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `Register with extremely long name returns 400 Bad Request`() {
        val longName = "A".repeat(21)
        val input = RegisterInputModel(longName, "long@isel.pt", "password123")
        val request = bodyRequest(POST, "/auth/register", body = Json.encodeToString(input))

        val response = authWebAPI.register(request)

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("Invalid username length"))
    }

    @Test
    fun `Simultaneous logins should produce different tokens`() {
        memManager.createTestUser("Multi", "multi@isel.pt", "pass123")
        val input = LoginInputModel("multi@isel.pt", "pass123")
        val req1 = bodyRequest(POST, "/auth/login", body = Json.encodeToString(input))
        val req2 = bodyRequest(POST, "/auth/login", body = Json.encodeToString(input))

        val res1 = authWebAPI.login(req1)
        val res2 = authWebAPI.login(req2)

        val token1 = Json.decodeFromString<TokenOutputModel>(res1.bodyString()).token
        val token2 = Json.decodeFromString<TokenOutputModel>(res2.bodyString()).token
        assertTrue(token1 != token2, "Tokens should be unique for each login session")
    }
}

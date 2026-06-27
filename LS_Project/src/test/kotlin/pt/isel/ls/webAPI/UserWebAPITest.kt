package pt.isel.ls.webAPI

import kotlinx.serialization.json.Json
import org.http4k.core.HttpHandler
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.PUT
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import pt.isel.ls.createTestToken
import pt.isel.ls.createTestUser
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.dto.UpdateUserInputModel
import pt.isel.ls.domain.dto.UserModels
import pt.isel.ls.services.UserService
import pt.isel.ls.simpleRequest
import pt.isel.ls.webapi.UserWebAPI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserWebAPITest {
    private lateinit var memManager: MemManager
    private lateinit var userWebAPI: UserWebAPI
    private lateinit var userRoutes: HttpHandler

    @BeforeTest
    fun setup() {
        memManager = MemManager()
        memManager.clear()

        userWebAPI = UserWebAPI(userServices = UserService(memManager))

        userRoutes =
            routes(
                "/users/{id}" bind GET to userWebAPI::getUserDetails,
                "/" bind PUT to userWebAPI::updateUser,
                "/" bind DELETE to userWebAPI::deleteUser,
            )
    }

    @Test
    fun `get user details successfully`() {
        val uid = memManager.createTestUser()

        val response = simpleRequest(GET, "/users/$uid").let(userRoutes)

        assertEquals(Status.OK, response.status)
        val userResponse = Json.decodeFromString<UserModels>(response.bodyString())
        assertEquals(uid, userResponse.userId)
        assertEquals("Alice", userResponse.name)
    }

    @Test
    fun `get user without token returns 200 OK`() {
        val uid = memManager.createTestUser()

        val response = simpleRequest(GET, "/users/$uid").let(userRoutes)

        assertEquals(Status.OK, response.status)
    }

    @Test
    fun `update user name and verify via API`() {
        val uid = memManager.createTestUser(name = "OldName")
        val token = memManager.createTestToken(uid)
        val newName = "NewName"
        val updateInput = Json.encodeToString(UpdateUserInputModel(name = newName))

        val updateResponse =
            simpleRequest(PUT, "/", token)
                .header("content-type", "application/json")
                .body(updateInput)
                .let(userRoutes)

        assertEquals(Status.OK, updateResponse.status)
        val getResponse = simpleRequest(GET, "/users/$uid").let(userRoutes)
        val userDetails = Json.decodeFromString<UserModels>(getResponse.bodyString())
        assertEquals(newName, userDetails.name)
    }

    @Test
    fun `update user without token returns 401 Unauthorized`() {
        val updateInput = Json.encodeToString(UpdateUserInputModel(name = "NewName"))

        val response =
            simpleRequest(PUT, "/")
                .header("content-type", "application/json")
                .body(updateInput)
                .let(userRoutes)

        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `update with too long name returns 400 Bad Request`() {
        val uid = memManager.createTestUser(name = "ValidName")
        val token = memManager.createTestToken(uid)
        val longName = "A".repeat(21)
        val updateInput = Json.encodeToString(UpdateUserInputModel(name = longName))

        val response =
            simpleRequest(PUT, "/", token)
                .header("content-type", "application/json")
                .body(updateInput)
                .let(userRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `delete user and verify unavailability`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)

        val deleteResponse = simpleRequest(DELETE, "/", token).let(userRoutes)

        assertEquals(Status.OK, deleteResponse.status)
        val getResponse = simpleRequest(GET, "/users/$uid").let(userRoutes)
        assertEquals(Status.NOT_FOUND, getResponse.status)
    }

    @Test
    fun `delete user without token returns 401 Unauthorized`() {
        val response = simpleRequest(DELETE, "/").let(userRoutes)

        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `delete then get returns 404 Not Found`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        simpleRequest(DELETE, "/", token).let(userRoutes)

        val getResponse = simpleRequest(GET, "/users/$uid").let(userRoutes)

        assertEquals(Status.NOT_FOUND, getResponse.status)
    }

    @Test
    fun `update with invalid name returns 400 and does not change data`() {
        val uid = memManager.createTestUser(name = "ValidName")
        val token = memManager.createTestToken(uid)
        val invalidInput = Json.encodeToString(UpdateUserInputModel(name = "Ab"))

        val response =
            simpleRequest(PUT, "/", token)
                .header("content-type", "application/json")
                .body(invalidInput)
                .let(userRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
        val getResponse = simpleRequest(GET, "/users/$uid").let(userRoutes)
        val userDetails = Json.decodeFromString<UserModels>(getResponse.bodyString())
        assertEquals("ValidName", userDetails.name)
    }

    @Test
    fun `can get details of another user`() {
        val uid1 = memManager.createTestUser(email = "user1@isel.pt")
        val uid2 = memManager.createTestUser(email = "user2@isel.pt")
        val token1 = memManager.createTestToken(uid1)

        val response = simpleRequest(GET, "/users/$uid2", token1).let(userRoutes)

        assertEquals(Status.OK, response.status)
    }
}

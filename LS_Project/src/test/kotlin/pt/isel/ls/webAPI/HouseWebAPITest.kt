package pt.isel.ls.webAPI

import kotlinx.serialization.json.Json
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import pt.isel.ls.bodyRequest
import pt.isel.ls.createTestToken
import pt.isel.ls.createTestUser
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.House
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationType
import pt.isel.ls.domain.dto.CreateHouseInput
import pt.isel.ls.domain.dto.CreateHouseOutput
import pt.isel.ls.domain.dto.GetAllHousesOutput
import pt.isel.ls.domain.dto.GetAvailableHousesOutput
import pt.isel.ls.services.HouseService
import pt.isel.ls.simpleRequest
import pt.isel.ls.webapi.HouseWebAPI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HouseWebAPITest {
    private lateinit var memManager: MemManager
    private lateinit var houseWebAPI: HouseWebAPI
    private lateinit var houseRoutes: HttpHandler
    private var locationId: Int = 0

    @BeforeTest
    fun setup() {
        memManager = MemManager()
        memManager.clear()

        houseWebAPI = HouseWebAPI(houseService = HouseService(memManager))

        houseRoutes =
            routes(
                "/" bind POST to houseWebAPI::createHouse,
                "/" bind GET to houseWebAPI::getAllHouses,
                "/available" bind GET to houseWebAPI::getAvailableHouses,
                "/{id}" bind GET to houseWebAPI::getHouseDetails,
            )

        locationId =
            memManager.locationData.createLocation(
                Location(0, "Lisboa", LocationType.DISTRICT, null),
            )
    }

    @Test
    fun `create house successfully`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val input =
            CreateHouseInput(
                title = "Casa de Teste",
                location = locationId,
                areaSqMt = 100,
                pricePerNight = 50.0,
                description = "Uma casa de teste bem legal",
            )
        val body = Json.encodeToString(input)

        val response = bodyRequest(POST, "/", token, body).let(houseRoutes)

        assertEquals(Status.CREATED, response.status)
        val output = Json.decodeFromString<CreateHouseOutput>(response.bodyString())
        assertTrue(output.id > 0)
    }

    @Test
    fun `create house without token returns 401 Unauthorized`() {
        val input =
            CreateHouseInput(
                title = "Casa de Teste",
                location = locationId,
                areaSqMt = 100,
                pricePerNight = 50.0,
                description = "Uma casa de teste bem legal",
            )
        val body = Json.encodeToString(input)

        val response = bodyRequest(POST, "/", null, body).let(houseRoutes)

        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `get house details successfully`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val input = CreateHouseInput("Título", locationId, 80, 45.0, "Desc")
        val createResponse = bodyRequest(POST, "/", token, Json.encodeToString(input)).let(houseRoutes)
        val hid = Json.decodeFromString<CreateHouseOutput>(createResponse.bodyString()).id

        val response =
            simpleRequest(GET, "/$hid")
                .header("content-type", "application/json")
                .let(houseRoutes)

        assertEquals(Status.OK, response.status)
        val house = Json.decodeFromString<House>(response.bodyString())
        assertEquals(hid, house.id)
        assertEquals("Título", house.title)
    }

    @Test
    fun `get all houses returns list`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val input = CreateHouseInput("Casa 1", locationId, 80, 45.0, "Desc")
        bodyRequest(POST, "/", token, Json.encodeToString(input)).let(houseRoutes)

        val response =
            simpleRequest(GET, "/")
                .header("content-type", "application/json")
                .let(houseRoutes)

        assertEquals(Status.OK, response.status)
        val output = Json.decodeFromString<GetAllHousesOutput>(response.bodyString())
        assertTrue(output.houses.isNotEmpty())
    }

    @Test
    fun `get available houses with valid dates`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val input = CreateHouseInput("Disponível", locationId, 80, 45.0, "Desc")
        bodyRequest(POST, "/", token, Json.encodeToString(input)).let(houseRoutes)

        val response =
            simpleRequest(GET, "/available?startDate=2024-01-01&endDate=2024-01-05")
                .header("content-type", "application/json")
                .let(houseRoutes)

        assertEquals(Status.OK, response.status)
        val output = Json.decodeFromString<GetAvailableHousesOutput>(response.bodyString())
        assertTrue(output.houses.isNotEmpty())
    }

    @Test
    fun `get available houses with invalid dates returns 400 Bad Request`() {
        val response = simpleRequest(GET, "/available?startDate=invalid&endDate=2024-01-05").let(houseRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }
}

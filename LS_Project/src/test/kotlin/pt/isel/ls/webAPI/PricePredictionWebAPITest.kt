package pt.isel.ls.webAPI

import kotlinx.serialization.json.Json
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import pt.isel.ls.createTestToken
import pt.isel.ls.createTestUser
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.dto.PricePredictionOutputModel
import pt.isel.ls.domain.requests.HouseCreationRequest
import pt.isel.ls.services.HouseService
import pt.isel.ls.services.PricePredictionService
import pt.isel.ls.simpleRequest
import pt.isel.ls.webapi.PricePredictionWebAPI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PricePredictionWebAPITest {
    private lateinit var memManager: MemManager
    private lateinit var predictionRoutes: HttpHandler

    @BeforeTest
    fun setup() {
        memManager = MemManager()
        memManager.clear()
        memManager.populateMemLocation()
    }

    private fun setupHousesInLocation(
        lid: Int,
        token: String,
    ) {
        val houseService = HouseService(memManager)
        houseService.createHouse(HouseCreationRequest("H1", lid, 50, 50.0, "D1"), token)
        houseService.createHouse(HouseCreationRequest("H2", lid, 100, 100.0, "D2"), token)
    }

    private fun buildPredictionRoutes(): HttpHandler {
        val pricePredictionWebAPI =
            PricePredictionWebAPI(
                pricePredictionService = PricePredictionService(memManager),
            )

        return routes(
            "/price" bind GET to pricePredictionWebAPI::getPricePrediction,
        )
    }

    @Test
    fun `get price prediction successfully`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val lid = 2 // Lisboa
        setupHousesInLocation(lid, token)
        predictionRoutes = buildPredictionRoutes()

        val response =
            simpleRequest(GET, "/price?areaSqMt=75&lid=$lid&nights=2", token)
                .let(predictionRoutes)

        assertEquals(Status.OK, response.status)
        val prediction = Json.decodeFromString<PricePredictionOutputModel>(response.bodyString())
        assertEquals(75.0, prediction.predictedPricePerNight)
        assertEquals(150.0, prediction.predictedTotalPrice)
    }

    @Test
    fun `get prediction without token succeeds`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        setupHousesInLocation(2, token)
        predictionRoutes = buildPredictionRoutes()

        val response =
            simpleRequest(GET, "/price?areaSqMt=75&lid=2&nights=2")
                .let(predictionRoutes)

        assertEquals(Status.OK, response.status)
    }

    @Test
    fun `get prediction for location with insufficient data returns 400 Bad Request`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val lid = 3
        predictionRoutes = buildPredictionRoutes()
        val response =
            simpleRequest(GET, "/price?areaSqMt=75&lid=$lid&nights=2", token)
                .let(predictionRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `get prediction with missing parameters returns 400 Bad Request`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        predictionRoutes = buildPredictionRoutes()
        val response =
            simpleRequest(GET, "/price?areaSqMt=75&nights=2", token)
                .let(predictionRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `get prediction with invalid area returns 400 Bad Request`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        predictionRoutes = buildPredictionRoutes()

        val response =
            simpleRequest(GET, "/price?areaSqMt=invalid&lid=2&nights=2", token)
                .let(predictionRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }
}

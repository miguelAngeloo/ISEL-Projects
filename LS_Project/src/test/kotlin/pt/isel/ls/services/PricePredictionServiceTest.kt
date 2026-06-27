package pt.isel.ls.services

import org.junit.jupiter.api.Test
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.requests.HouseCreationRequest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PricePredictionServiceTest {
    private lateinit var memManager: MemManager
    private lateinit var predictionService: PricePredictionService
    private lateinit var houseService: HouseService

    @BeforeTest
    fun setup() {
        memManager = MemManager()
        memManager.clear()
        memManager.populateMemData()

        predictionService = PricePredictionService(dataManager = memManager)
        houseService = HouseService(memManager)
    }

    @Test
    fun `get price prediction successfully`() {
        val result =
            predictionService.getPricePrediction(
                areaSqMt = 100.0,
                locationId = 2,
                nights = 5,
            )

        assertTrue(result.predictedPricePerNight > 0)
        assertEquals(result.predictedPricePerNight * 5, result.predictedTotalPrice)
    }

    @Test
    fun `get price prediction with location having less than 2 houses should throw IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            predictionService.getPricePrediction(
                areaSqMt = 100.0,
                locationId = 3,
                nights = 1,
            )
        }
    }

    @Test
    fun `get price prediction with very large area should extrapolate correctly`() {
        val result =
            predictionService.getPricePrediction(
                areaSqMt = 7000.0,
                locationId = 1,
                nights = 1,
            )

        assertTrue(result.predictedPricePerNight > 1000.0)
    }

    @Test
    fun `get price prediction should not change after new houses are added`() {
        val initialPrediction =
            predictionService.getPricePrediction(
                areaSqMt = 100.0,
                locationId = 2,
                nights = 2,
            )

        houseService.createHouse(
            HouseCreationRequest(
                title = "Outlier",
                locationId = 2,
                areaSqMt = 100,
                pricePerNight = 1000.0,
                description = "This house should not affect the frozen model.",
            ),
            tokenString = memManager.tokenData.getUserToken(1)?.token.toString(),
        )

        val predictionAfterNewHouse =
            predictionService.getPricePrediction(
                areaSqMt = 100.0,
                locationId = 2,
                nights = 2,
            )

        assertEquals(initialPrediction, predictionAfterNewHouse)
    }
}

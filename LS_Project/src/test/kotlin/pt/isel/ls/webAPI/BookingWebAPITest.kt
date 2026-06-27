package pt.isel.ls.webAPI

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.http4k.core.HttpHandler
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import pt.isel.ls.bodyRequest
import pt.isel.ls.createTestToken
import pt.isel.ls.createTestUser
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.Booking
import pt.isel.ls.domain.House
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationType
import pt.isel.ls.domain.dto.CreateBookingInput
import pt.isel.ls.domain.dto.CreateBookingOutput
import pt.isel.ls.domain.dto.GetBookingsOutput
import pt.isel.ls.services.BookingService
import pt.isel.ls.simpleRequest
import pt.isel.ls.webapi.BookingWebAPI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookingWebAPITest {
    private lateinit var memManager: MemManager
    private lateinit var bookingWebAPI: BookingWebAPI
    private lateinit var bookingRoutes: HttpHandler
    private var houseId: Int = 0

    @BeforeTest
    fun setup() {
        memManager = MemManager()
        memManager.clear()

        bookingWebAPI = BookingWebAPI(bookingService = BookingService(memManager))

        bookingRoutes =
            routes(
                "/" bind POST to bookingWebAPI::createBooking,
                "/" bind GET to bookingWebAPI::getBookings,
                "/me" bind GET to bookingWebAPI::getBookingsByUser,
                "/users/{id}/bookings" bind GET to bookingWebAPI::getBookingsByUserId,
                "/{id}" bind GET to bookingWebAPI::getBookingDetails,
                "/{id}" bind DELETE to bookingWebAPI::deleteBooking,
                "/{id}" bind PUT to bookingWebAPI::updateBooking,
            )

        val lid =
            memManager.locationData.createLocation(
                Location(0, "Lisboa", LocationType.DISTRICT, null),
            )

        houseId =
            memManager.houseData.createHouse(
                House(0, 999, "Casa Teste", lid, 100, 50.0, "Desc"),
            )
    }

    @Test
    fun `create booking successfully`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val input =
            CreateBookingInput(
                hid = houseId,
                startDate = LocalDate.parse("2024-01-01"),
                endDate = LocalDate.parse("2024-01-05"),
            )
        val body = Json.encodeToString(input)

        val response = bodyRequest(POST, "/", token, body).let(bookingRoutes)

        assertEquals(Status.CREATED, response.status)
        val output = Json.decodeFromString<CreateBookingOutput>(response.bodyString())
        assertTrue(output.id > 0)
    }

    @Test
    fun `create booking without token returns 401 Unauthorized`() {
        val input =
            CreateBookingInput(
                hid = houseId,
                startDate = LocalDate.parse("2024-01-01"),
                endDate = LocalDate.parse("2024-01-05"),
            )
        val body = Json.encodeToString(input)

        val response = bodyRequest(POST, "/", null, body).let(bookingRoutes)

        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `get booking details successfully`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val input = CreateBookingInput(houseId, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-05"))
        val createResponse = bodyRequest(POST, "/", token, Json.encodeToString(input)).let(bookingRoutes)
        val bid = Json.decodeFromString<CreateBookingOutput>(createResponse.bodyString()).id

        val response =
            simpleRequest(GET, "/$bid")
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.OK, response.status)
        val booking = Json.decodeFromString<Booking>(response.bodyString())
        assertEquals(bid, booking.id)
        assertEquals(houseId, booking.houseId)
    }

    @Test
    fun `get bookings with filter`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val input = CreateBookingInput(houseId, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-05"))
        bodyRequest(POST, "/", token, Json.encodeToString(input)).let(bookingRoutes)

        val response =
            simpleRequest(GET, "/?hid=$houseId")
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.OK, response.status)
        val output = Json.decodeFromString<GetBookingsOutput>(response.bodyString())
        assertTrue(output.bookings.isNotEmpty())
        assertEquals(houseId, output.bookings[0].houseId)
    }

    @Test
    fun `get bookings without house id returns 400 Bad Request`() {
        val response = simpleRequest(GET, "/").let(bookingRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `get user bookings returns empty when no bookings`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)

        val response =
            simpleRequest(GET, "/me", token)
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.OK, response.status)
        val output = Json.decodeFromString<GetBookingsOutput>(response.bodyString())
        assertEquals(0, output.bookings.size)
    }

    @Test
    fun `get user bookings without token returns 401 Unauthorized`() {
        val response =
            simpleRequest(GET, "/me")
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `get user bookings with invalid skip returns 400 Bad Request`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)

        val response =
            simpleRequest(GET, "/me?skip=-1", token)
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `get user bookings with invalid limit returns 400 Bad Request`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)

        val response =
            simpleRequest(GET, "/me?limit=0", token)
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `get bookings by user id returns that user's bookings`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val input = CreateBookingInput(houseId, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-05"))
        bodyRequest(POST, "/", token, Json.encodeToString(input)).let(bookingRoutes)

        val response =
            simpleRequest(GET, "/users/$uid/bookings")
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.OK, response.status)
        val output = Json.decodeFromString<GetBookingsOutput>(response.bodyString())
        assertEquals(1, output.bookings.size)
        assertEquals(uid, output.bookings.first().userId)
    }

    @Test
    fun `delete booking successfully`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val input = CreateBookingInput(houseId, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-05"))
        val createResponse = bodyRequest(POST, "/", token, Json.encodeToString(input)).let(bookingRoutes)
        val bid = Json.decodeFromString<CreateBookingOutput>(createResponse.bodyString()).id

        val response =
            simpleRequest(DELETE, "/$bid", token)
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.OK, response.status)

        // Verify booking is deleted
        val getResponse =
            simpleRequest(GET, "/$bid", token)
                .header("content-type", "application/json")
                .let(bookingRoutes)
        assertEquals(Status.NOT_FOUND, getResponse.status)
    }

    @Test
    fun `delete non-existent booking returns 404 Not Found`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)

        val response =
            simpleRequest(DELETE, "/999", token)
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `delete booking without token returns 401 Unauthorized`() {
        val response =
            simpleRequest(DELETE, "/1")
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `delete booking from different user returns 401 Unauthorized`() {
        val uid1 = memManager.createTestUser()
        val token1 = memManager.createTestToken(uid1)
        val input = CreateBookingInput(houseId, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-05"))
        val createResponse = bodyRequest(POST, "/", token1, Json.encodeToString(input)).let(bookingRoutes)
        val bid = Json.decodeFromString<CreateBookingOutput>(createResponse.bodyString()).id

        val uid2 = memManager.createTestUser()
        val token2 = memManager.createTestToken(uid2)

        val response =
            simpleRequest(DELETE, "/$bid", token2)
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `delete booking with invalid id returns 400 Bad Request`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)

        val response =
            simpleRequest(DELETE, "/invalid", token)
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `update booking successfully`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val createInput = CreateBookingInput(houseId, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-05"))
        val createResponse = bodyRequest(POST, "/", token, Json.encodeToString(createInput)).let(bookingRoutes)
        val bid = Json.decodeFromString<CreateBookingOutput>(createResponse.bodyString()).id

        val updateInput = CreateBookingInput(houseId, LocalDate.parse("2024-02-01"), LocalDate.parse("2024-02-05"))
        val response =
            bodyRequest(PUT, "/$bid", token, Json.encodeToString(updateInput))
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.OK, response.status)
        val booking = Json.decodeFromString<Booking>(response.bodyString())
        assertEquals(LocalDate.parse("2024-02-01"), booking.startDate)
        assertEquals(LocalDate.parse("2024-02-05"), booking.endDate)
    }

    @Test
    fun `update non-existent booking returns 404 Not Found`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val updateInput = CreateBookingInput(houseId, LocalDate.parse("2024-02-01"), LocalDate.parse("2024-02-05"))

        val response =
            bodyRequest(PUT, "/999", token, Json.encodeToString(updateInput))
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `update booking without token returns 401 Unauthorized`() {
        val updateInput = CreateBookingInput(houseId, LocalDate.parse("2024-02-01"), LocalDate.parse("2024-02-05"))

        val response =
            bodyRequest(PUT, "/1", null, Json.encodeToString(updateInput))
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `update booking from different user returns 401 Unauthorized`() {
        val uid1 = memManager.createTestUser()
        val token1 = memManager.createTestToken(uid1)
        val createInput = CreateBookingInput(houseId, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-05"))
        val createResponse = bodyRequest(POST, "/", token1, Json.encodeToString(createInput)).let(bookingRoutes)
        val bid = Json.decodeFromString<CreateBookingOutput>(createResponse.bodyString()).id

        val uid2 = memManager.createTestUser()
        val token2 = memManager.createTestToken(uid2)

        val updateInput = CreateBookingInput(houseId, LocalDate.parse("2024-02-01"), LocalDate.parse("2024-02-05"))
        val response =
            bodyRequest(PUT, "/$bid", token2, Json.encodeToString(updateInput))
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `update booking with invalid dates returns 400 Bad Request`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val createInput = CreateBookingInput(houseId, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-05"))
        val createResponse = bodyRequest(POST, "/", token, Json.encodeToString(createInput)).let(bookingRoutes)
        val bid = Json.decodeFromString<CreateBookingOutput>(createResponse.bodyString()).id

        val updateInput = CreateBookingInput(houseId, LocalDate.parse("2024-02-05"), LocalDate.parse("2024-02-01"))
        val response =
            bodyRequest(PUT, "/$bid", token, Json.encodeToString(updateInput))
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `update booking with conflicting dates returns 400 Bad Request`() {
        val uid1 = memManager.createTestUser()
        val token1 = memManager.createTestToken(uid1)
        val createInput1 = CreateBookingInput(houseId, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-05"))
        val createResponse1 = bodyRequest(POST, "/", token1, Json.encodeToString(createInput1)).let(bookingRoutes)
        val bid1 = Json.decodeFromString<CreateBookingOutput>(createResponse1.bodyString()).id

        val uid2 = memManager.createTestUser()
        val token2 = memManager.createTestToken(uid2)
        val createInput2 = CreateBookingInput(houseId, LocalDate.parse("2024-02-10"), LocalDate.parse("2024-02-15"))
        bodyRequest(POST, "/", token2, Json.encodeToString(createInput2)).let(bookingRoutes)

        val updateInput = CreateBookingInput(houseId, LocalDate.parse("2024-02-12"), LocalDate.parse("2024-02-18"))
        val response =
            bodyRequest(PUT, "/$bid1", token1, Json.encodeToString(updateInput))
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `update booking with invalid id returns 400 Bad Request`() {
        val uid = memManager.createTestUser()
        val token = memManager.createTestToken(uid)
        val updateInput = CreateBookingInput(houseId, LocalDate.parse("2024-02-01"), LocalDate.parse("2024-02-05"))

        val response =
            bodyRequest(PUT, "/invalid", token, Json.encodeToString(updateInput))
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `update booking to available dates succeeds`() {
        val uid1 = memManager.createTestUser()
        val token1 = memManager.createTestToken(uid1)
        val createInput1 = CreateBookingInput(houseId, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-05"))
        val createResponse1 = bodyRequest(POST, "/", token1, Json.encodeToString(createInput1)).let(bookingRoutes)
        val bid1 = Json.decodeFromString<CreateBookingOutput>(createResponse1.bodyString()).id

        val uid2 = memManager.createTestUser()
        val token2 = memManager.createTestToken(uid2)
        val createInput2 = CreateBookingInput(houseId, LocalDate.parse("2024-02-10"), LocalDate.parse("2024-02-15"))
        bodyRequest(POST, "/", token2, Json.encodeToString(createInput2)).let(bookingRoutes)

        val updateInput = CreateBookingInput(houseId, LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-05"))
        val response =
            bodyRequest(PUT, "/$bid1", token1, Json.encodeToString(updateInput))
                .header("content-type", "application/json")
                .let(bookingRoutes)

        assertEquals(Status.OK, response.status)
        val booking = Json.decodeFromString<Booking>(response.bodyString())
        assertEquals(LocalDate.parse("2024-03-01"), booking.startDate)
        assertEquals(LocalDate.parse("2024-03-05"), booking.endDate)
    }
}

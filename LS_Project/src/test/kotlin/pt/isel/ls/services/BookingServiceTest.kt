package pt.isel.ls.services

import kotlinx.datetime.LocalDate
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.Booking
import pt.isel.ls.domain.House
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationType
import pt.isel.ls.domain.User
import pt.isel.ls.domain.requests.BookingCreationRequest
import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName
import pt.isel.ls.exceptions.BadRequestException
import pt.isel.ls.exceptions.UnauthorizedException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class BookingServiceTest {
    private lateinit var memManager: MemManager
    private lateinit var bookingService: BookingService

    @BeforeTest
    fun setup() {
        memManager = MemManager()
        memManager.clear()
        bookingService = BookingService(memManager)
    }

    private fun createLocation(): Int {
        val locationInput =
            Location(
                lid = 0,
                name = "Lisboa",
                type = LocationType.DISTRICT,
                parentId = null,
            )
        return memManager.locationData.createLocation(locationInput)
    }

    private fun createHouse(
        lid: Int,
        ownerId: Int = 2,
    ): Int {
        val house =
            House(
                id = 0,
                ownerId = ownerId,
                title = "Nice House",
                locationId = lid,
                areaSqMt = 100,
                pricePerNight = 80.0,
                description = "A very nice house",
            )
        return memManager.houseData.createHouse(house)
    }

    private fun createUserWithToken(): Pair<Int, String> {
        val uid =
            memManager.userData.createUser(
                User(
                    uid = 0,
                    name = UserName("Alice"),
                    email = Email("alice@isel.pt"),
                    password = Password("secure123"),
                ),
            )
        val tokenObj = memManager.tokenData.createToken(uid)
        return Pair(uid, tokenObj.token.toString())
    }

    @Test
    fun `create booking successfully when house is available`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid, tokenString) = createUserWithToken()

        val newBid =
            bookingService.createBooking(
                request =
                    BookingCreationRequest(
                        hid = hid,
                        startDate = LocalDate.parse("2026-02-01"),
                        endDate = LocalDate.parse("2026-02-05"),
                    ),
                tokenString = tokenString,
            )

        val savedBooking = memManager.bookingData.getBooking(newBid)
        assertNotNull(savedBooking)
        assertEquals(uid, savedBooking.userId)
        assertEquals(hid, savedBooking.houseId)
    }

    @Test
    fun `create booking on unavailable house throws BadRequestException`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (_, tokenString) = createUserWithToken()

        val existingBooking =
            Booking(
                id = 0,
                userId = 1,
                houseId = hid,
                startDate = LocalDate.parse("2026-03-10"),
                endDate = LocalDate.parse("2026-03-15"),
            )
        memManager.bookingData.createBooking(existingBooking)

        assertFailsWith<BadRequestException> {
            bookingService.createBooking(
                request =
                    BookingCreationRequest(
                        hid = hid,
                        startDate = LocalDate.parse("2026-03-12"),
                        endDate = LocalDate.parse("2026-03-18"),
                    ),
                tokenString = tokenString,
            )
        }
    }

    @Test
    fun `get bookings filters by house and date interval`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, _) = createUserWithToken()

        val uid2 =
            memManager.userData.createUser(
                User(
                    uid = 0,
                    name = UserName("Bob"),
                    email = Email("bob@isel.pt"),
                    password = Password("pwdpwd"),
                ),
            )

        memManager.bookingData.createBooking(
            Booking(
                id = 0,
                userId = uid1,
                houseId = hid,
                startDate = LocalDate.parse("2026-04-01"),
                endDate = LocalDate.parse("2026-04-10"),
            ),
        )
        memManager.bookingData.createBooking(
            Booking(
                id = 0,
                userId = uid2,
                houseId = hid,
                startDate = LocalDate.parse("2026-04-03"),
                endDate = LocalDate.parse("2026-04-08"),
            ),
        )

        val startDate = LocalDate.parse("2026-04-05")
        val endDate = LocalDate.parse("2026-04-05")

        val bookings =
            bookingService.getBookings(
                hid = hid,
                startDate = startDate,
                endDate = endDate,
                skip = 0,
                limit = 10,
            )

        assertEquals(2, bookings.size)
        assertEquals(true, bookings.all { it.houseId == hid })
        assertEquals(true, bookings.all { startDate >= it.startDate && endDate <= it.endDate })
    }

    @Test
    fun `get booking details is available for reading`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, _) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-05-01"),
                    endDate = LocalDate.parse("2026-05-05"),
                ),
            )

        val booking = bookingService.getBookingDetails(bookingId)

        assertEquals(bookingId, booking.id)
        assertEquals(uid1, booking.userId)
    }

    @Test
    fun `get bookings by user returns all user bookings`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()

        repeat(3) { index ->
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-06-0${index + 1}"),
                    endDate = LocalDate.parse("2026-06-0${index + 2}"),
                ),
            )
        }

        val bookings = bookingService.getBookingsByUser(token1)

        assertEquals(3, bookings.size)
        assertEquals(true, bookings.all { it.userId == uid1 })
    }

    @Test
    fun `get bookings by user with pagination`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()

        repeat(10) { _ ->
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-06-01"),
                    endDate = LocalDate.parse("2026-06-02"),
                ),
            )
        }

        val bookings = bookingService.getBookingsByUser(token1, skip = 0, limit = 5)

        assertEquals(5, bookings.size)
    }

    @Test
    fun `get bookings by user with skip parameter`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()

        repeat(5) { _ ->
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-06-01"),
                    endDate = LocalDate.parse("2026-06-02"),
                ),
            )
        }

        val bookings = bookingService.getBookingsByUser(token1, skip = 2, limit = 10)

        assertEquals(3, bookings.size)
    }

    @Test
    fun `get bookings by user returns only user's bookings not other users`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()
        val (uid2, _) = createUserWithToken()

        memManager.bookingData.createBooking(
            Booking(
                id = 0,
                userId = uid1,
                houseId = hid,
                startDate = LocalDate.parse("2026-06-01"),
                endDate = LocalDate.parse("2026-06-05"),
            ),
        )
        memManager.bookingData.createBooking(
            Booking(
                id = 0,
                userId = uid2,
                houseId = hid,
                startDate = LocalDate.parse("2026-06-10"),
                endDate = LocalDate.parse("2026-06-15"),
            ),
        )

        val bookings = bookingService.getBookingsByUser(token1)

        assertEquals(1, bookings.size)
        assertEquals(uid1, bookings.first().userId)
    }

    @Test
    fun `get bookings by user id returns that user's bookings`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, _) = createUserWithToken()
        val (uid2, _) = createUserWithToken()

        memManager.bookingData.createBooking(
            Booking(
                id = 0,
                userId = uid1,
                houseId = hid,
                startDate = LocalDate.parse("2026-06-01"),
                endDate = LocalDate.parse("2026-06-05"),
            ),
        )
        memManager.bookingData.createBooking(
            Booking(
                id = 0,
                userId = uid2,
                houseId = hid,
                startDate = LocalDate.parse("2026-06-10"),
                endDate = LocalDate.parse("2026-06-15"),
            ),
        )

        val bookings = bookingService.getBookingsByUserId(uid2)

        assertEquals(1, bookings.size)
        assertEquals(uid2, bookings.first().userId)
    }

    @Test
    fun `get bookings by user returns empty list when no bookings`() {
        val (_, token1) = createUserWithToken()

        val bookings = bookingService.getBookingsByUser(token1)

        assertEquals(0, bookings.size)
    }

    @Test
    fun `get bookings by user with invalid token throws UnauthorizedException`() {
        assertFailsWith<UnauthorizedException> {
            bookingService.getBookingsByUser("invalid_token")
        }
    }

    @Test
    fun `get bookings by user with negative skip throws BadRequestException`() {
        val (_, token1) = createUserWithToken()

        assertFailsWith<BadRequestException> {
            bookingService.getBookingsByUser(token1, skip = -1)
        }
    }

    @Test
    fun `get bookings by user with invalid limit throws BadRequestException`() {
        val (_, token1) = createUserWithToken()

        assertFailsWith<BadRequestException> {
            bookingService.getBookingsByUser(token1, limit = 0)
        }
    }

    @Test
    fun `delete booking successfully`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-07-01"),
                    endDate = LocalDate.parse("2026-07-05"),
                ),
            )

        bookingService.deleteBooking(bookingId, token1)

        val deletedBooking = memManager.bookingData.getBooking(bookingId)
        assertEquals(null, deletedBooking)
    }

    @Test
    fun `delete booking with non-existent id throws NotFoundException`() {
        val (_, token1) = createUserWithToken()

        assertFailsWith<pt.isel.ls.exceptions.NotFoundException> {
            bookingService.deleteBooking(999, token1)
        }
    }

    @Test
    fun `delete booking from different user throws UnauthorizedException`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, _) = createUserWithToken()
        val (_, token2) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-07-01"),
                    endDate = LocalDate.parse("2026-07-05"),
                ),
            )

        assertFailsWith<UnauthorizedException> {
            bookingService.deleteBooking(bookingId, token2)
        }
    }

    @Test
    fun `delete booking with invalid token throws UnauthorizedException`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, _) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-07-01"),
                    endDate = LocalDate.parse("2026-07-05"),
                ),
            )

        assertFailsWith<UnauthorizedException> {
            bookingService.deleteBooking(bookingId, "invalid_token")
        }
    }

    @Test
    fun `update booking successfully with new dates`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-08-01"),
                    endDate = LocalDate.parse("2026-08-05"),
                ),
            )

        val updatedBooking =
            bookingService.updateBooking(
                bookingId,
                LocalDate.parse("2026-08-10"),
                LocalDate.parse("2026-08-15"),
                token1,
            )

        assertEquals(bookingId, updatedBooking.id)
        assertEquals(LocalDate.parse("2026-08-10"), updatedBooking.startDate)
        assertEquals(LocalDate.parse("2026-08-15"), updatedBooking.endDate)

        val savedBooking = memManager.bookingData.getBooking(bookingId)
        assertNotNull(savedBooking)
        assertEquals(LocalDate.parse("2026-08-10"), savedBooking.startDate)
        assertEquals(LocalDate.parse("2026-08-15"), savedBooking.endDate)
    }

    @Test
    fun `update booking with same start and end date`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-08-01"),
                    endDate = LocalDate.parse("2026-08-05"),
                ),
            )

        val updatedBooking =
            bookingService.updateBooking(
                bookingId,
                LocalDate.parse("2026-08-10"),
                LocalDate.parse("2026-08-10"),
                token1,
            )

        assertEquals(LocalDate.parse("2026-08-10"), updatedBooking.startDate)
        assertEquals(LocalDate.parse("2026-08-10"), updatedBooking.endDate)
    }

    @Test
    fun `update booking with start date after end date throws BadRequestException`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-08-01"),
                    endDate = LocalDate.parse("2026-08-05"),
                ),
            )

        assertFailsWith<BadRequestException> {
            bookingService.updateBooking(
                bookingId,
                LocalDate.parse("2026-08-20"),
                LocalDate.parse("2026-08-10"),
                token1,
            )
        }
    }

    @Test
    fun `update booking with non-existent id throws NotFoundException`() {
        val (_, token1) = createUserWithToken()

        assertFailsWith<pt.isel.ls.exceptions.NotFoundException> {
            bookingService.updateBooking(
                999,
                LocalDate.parse("2026-08-10"),
                LocalDate.parse("2026-08-15"),
                token1,
            )
        }
    }

    @Test
    fun `update booking from different user throws UnauthorizedException`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, _) = createUserWithToken()
        val (_, token2) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-08-01"),
                    endDate = LocalDate.parse("2026-08-05"),
                ),
            )

        assertFailsWith<UnauthorizedException> {
            bookingService.updateBooking(
                bookingId,
                LocalDate.parse("2026-08-10"),
                LocalDate.parse("2026-08-15"),
                token2,
            )
        }
    }

    @Test
    fun `update booking with invalid token throws UnauthorizedException`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, _) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-08-01"),
                    endDate = LocalDate.parse("2026-08-05"),
                ),
            )

        assertFailsWith<UnauthorizedException> {
            bookingService.updateBooking(
                bookingId,
                LocalDate.parse("2026-08-10"),
                LocalDate.parse("2026-08-15"),
                "invalid_token",
            )
        }
    }

    @Test
    fun `update booking with conflicting dates throws BadRequestException`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()
        val (uid2, _) = createUserWithToken()

        val bookingId1 =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-09-01"),
                    endDate = LocalDate.parse("2026-09-05"),
                ),
            )

        // Another user's booking on the same house
        memManager.bookingData.createBooking(
            Booking(
                id = 0,
                userId = uid2,
                houseId = hid,
                startDate = LocalDate.parse("2026-09-10"),
                endDate = LocalDate.parse("2026-09-15"),
            ),
        )

        assertFailsWith<BadRequestException> {
            bookingService.updateBooking(
                bookingId1,
                LocalDate.parse("2026-09-12"),
                LocalDate.parse("2026-09-18"),
                token1,
            )
        }
    }

    @Test
    fun `update booking excludes own booking from conflict check`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-09-01"),
                    endDate = LocalDate.parse("2026-09-05"),
                ),
            )

        val updatedBooking =
            bookingService.updateBooking(
                bookingId,
                LocalDate.parse("2026-09-01"),
                LocalDate.parse("2026-09-05"),
                token1,
            )

        assertNotNull(updatedBooking)
        assertEquals(bookingId, updatedBooking.id)
    }

    @Test
    fun `update booking to available period succeeds`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()
        val (uid2, _) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = hid,
                    startDate = LocalDate.parse("2026-09-01"),
                    endDate = LocalDate.parse("2026-09-05"),
                ),
            )

        memManager.bookingData.createBooking(
            Booking(
                id = 0,
                userId = uid2,
                houseId = hid,
                startDate = LocalDate.parse("2026-09-10"),
                endDate = LocalDate.parse("2026-09-15"),
            ),
        )

        val updatedBooking =
            bookingService.updateBooking(
                bookingId,
                LocalDate.parse("2026-09-20"),
                LocalDate.parse("2026-09-25"),
                token1,
            )

        assertNotNull(updatedBooking)
        assertEquals(LocalDate.parse("2026-09-20"), updatedBooking.startDate)
        assertEquals(LocalDate.parse("2026-09-25"), updatedBooking.endDate)
    }

    @Test
    fun `update booking with non-existent house throws NotFoundException`() {
        val lid = createLocation()
        val hid = createHouse(lid)
        val (uid1, token1) = createUserWithToken()

        val bookingId =
            memManager.bookingData.createBooking(
                Booking(
                    id = 0,
                    userId = uid1,
                    houseId = 9999,
                    startDate = LocalDate.parse("2026-08-01"),
                    endDate = LocalDate.parse("2026-08-05"),
                ),
            )

        assertFailsWith<pt.isel.ls.exceptions.NotFoundException> {
            bookingService.updateBooking(
                bookingId,
                LocalDate.parse("2026-08-10"),
                LocalDate.parse("2026-08-15"),
                token1,
            )
        }
    }

    @Test
    fun `create booking by house owner throws BadRequestException`() {
        val lid = createLocation()
        val (uid, tokenString) = createUserWithToken()
        val hid = createHouse(lid, ownerId = uid)

        assertFailsWith<BadRequestException> {
            bookingService.createBooking(
                request =
                    BookingCreationRequest(
                        hid = hid,
                        startDate = LocalDate.parse("2026-02-01"),
                        endDate = LocalDate.parse("2026-02-05"),
                    ),
                tokenString = tokenString,
            )
        }
    }
}

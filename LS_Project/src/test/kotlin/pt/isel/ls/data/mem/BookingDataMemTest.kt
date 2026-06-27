package pt.isel.ls.data.mem

import kotlinx.datetime.LocalDate
import pt.isel.ls.domain.Booking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BookingDataMemTest {
    private lateinit var bookingData: BookingDataMem

    @BeforeTest
    fun setup() {
        bookingData = BookingDataMem()
        bookingData.clear()
    }

    @Test
    fun `create booking should generate new id and store booking`() {
        val booking =
            Booking(
                id = 0,
                userId = 1,
                houseId = 1,
                startDate = LocalDate(2026, 6, 10),
                endDate = LocalDate(2026, 6, 15),
            )

        val newBid = bookingData.createBooking(booking)

        assertEquals(1, newBid)
        val savedBooking = bookingData.getBooking(newBid)
        assertNotNull(savedBooking)
        assertEquals(newBid, savedBooking?.id)
        assertEquals(1, savedBooking?.userId)
        assertEquals(1, savedBooking?.houseId)
    }

    @Test
    fun `get booking should return correct booking`() {
        val booking =
            Booking(
                id = 0,
                userId = 1,
                houseId = 1,
                startDate = LocalDate(2026, 6, 10),
                endDate = LocalDate(2026, 6, 15),
            )

        val createdBid = bookingData.createBooking(booking)
        val retrievedBooking = bookingData.getBooking(createdBid)

        assertNotNull(retrievedBooking)
        assertEquals(createdBid, retrievedBooking?.id)
        assertEquals(1, retrievedBooking?.userId)
        assertEquals(1, retrievedBooking?.houseId)
    }

    @Test
    fun `get booking with invalid id should return null`() {
        val result = bookingData.getBooking(999)
        assertEquals(null, result)
    }

    @Test
    fun `get bookings by house should return bookings for specific house`() {
        val booking1 = Booking(0, 1, 1, LocalDate(2026, 6, 10), LocalDate(2026, 6, 15))
        val booking2 = Booking(0, 2, 2, LocalDate(2026, 6, 12), LocalDate(2026, 6, 18))
        val booking3 = Booking(0, 3, 1, LocalDate(2026, 6, 20), LocalDate(2026, 6, 25))

        bookingData.createBooking(booking1)
        bookingData.createBooking(booking2)
        bookingData.createBooking(booking3)

        val house1Bookings = bookingData.getBookingsByHouse(1, 0, 10)

        assertEquals(2, house1Bookings.size)
        assertEquals(listOf(1, 3), house1Bookings.map { it.userId })
    }

    @Test
    fun `get bookings by date should return bookings for specific date`() {
        val booking1 = Booking(0, 1, 1, LocalDate(2026, 6, 10), LocalDate(2026, 6, 15))
        val booking2 = Booking(0, 2, 2, LocalDate(2026, 6, 12), LocalDate(2026, 6, 18))
        val booking3 = Booking(0, 3, 1, LocalDate(2026, 6, 20), LocalDate(2026, 6, 25))

        bookingData.createBooking(booking1)
        bookingData.createBooking(booking2)
        bookingData.createBooking(booking3)

        val dateBookings = bookingData.getBookingsByDate(LocalDate(2026, 6, 12), 0, 10)

        assertEquals(2, dateBookings.size)
        assertEquals(listOf(1, 2), dateBookings.map { it.userId })
    }

    @Test
    fun `get all bookings should return all stored bookings`() {
        val booking1 = Booking(0, 1, 1, LocalDate(2026, 6, 10), LocalDate(2026, 6, 15))
        val booking2 = Booking(0, 2, 2, LocalDate(2026, 6, 12), LocalDate(2026, 6, 18))
        val booking3 = Booking(0, 3, 1, LocalDate(2026, 6, 20), LocalDate(2026, 6, 25))

        bookingData.createBooking(booking1)
        bookingData.createBooking(booking2)
        bookingData.createBooking(booking3)

        val allBookings = bookingData.getAllBookings(0, 10)

        assertEquals(3, allBookings.size)
        assertEquals(listOf(1, 2, 3), allBookings.map { it.userId })
    }

    @Test
    fun `get all bookings should return empty list when no bookings exist`() {
        val allBookings = bookingData.getAllBookings(0, 10)
        assertEquals(0, allBookings.size)
        assertTrue(allBookings.isEmpty())
    }

    @Test
    fun `clear should remove all bookings`() {
        val booking = Booking(0, 1, 1, LocalDate(2026, 6, 10), LocalDate(2026, 6, 15))
        bookingData.createBooking(booking)

        assertEquals(1, bookingData.getAllBookings(0, 10).size)

        bookingData.clear()

        assertEquals(0, bookingData.getAllBookings(0, 10).size)
        assertTrue(bookingData.getAllBookings(0, 10).isEmpty())
    }
}

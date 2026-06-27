package pt.isel.ls.domain

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class BookingTest {
    @Test
    fun `Create a valid booking`() {
        val start = LocalDate.parse("2026-07-15")
        val end = LocalDate.parse("2026-07-22")

        val booking =
            Booking(
                id = 505,
                userId = 12,
                houseId = 101,
                startDate = start,
                endDate = end,
            )
        assertEquals(505, booking.id)
        assertEquals(12, booking.userId)
        assertEquals(101, booking.houseId)
        assertEquals(start, booking.startDate)
        assertEquals(end, booking.endDate)
        assertEquals(2026, booking.startDate.year)
        assertEquals(7, booking.startDate.monthNumber)
        assertEquals(15, booking.startDate.dayOfMonth)
    }
}

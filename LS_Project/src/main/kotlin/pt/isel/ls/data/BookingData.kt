package pt.isel.ls.data

import kotlinx.datetime.LocalDate
import pt.isel.ls.domain.Booking

interface BookingData {
    fun createBooking(booking: Booking): Int

    fun getBooking(bid: Int): Booking?

    fun getBookingsByHouse(
        hid: Int,
        skip: Int,
        limit: Int,
    ): List<Booking>

    fun getBookingsByDate(
        date: LocalDate,
        skip: Int,
        limit: Int,
    ): List<Booking>

    fun getBookingsByUser(
        uid: Int,
        skip: Int,
        limit: Int,
    ): List<Booking>

    fun getAllBookings(
        skip: Int,
        limit: Int,
    ): List<Booking>

    fun getConflictingBookings(
        hid: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<Booking>

    fun deleteBooking(bid: Int): Boolean

    fun updateBooking(booking: Booking): Boolean
}

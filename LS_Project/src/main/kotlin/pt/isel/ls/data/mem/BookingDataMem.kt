package pt.isel.ls.data.mem

import kotlinx.datetime.LocalDate
import pt.isel.ls.data.BookingData
import pt.isel.ls.domain.Booking
import pt.isel.ls.utils.datesOverlap

class BookingDataMem : MemStorage(), BookingData {
    override fun createBooking(booking: Booking): Int {
        val newBid = bid
        val newBooking = booking.copy(id = newBid)
        bookingsDB.add(newBooking)
        return newBid
    }

    override fun getBooking(bid: Int): Booking? {
        return bookingsDB.find { it.id == bid }
    }

    override fun getBookingsByHouse(
        hid: Int,
        skip: Int,
        limit: Int,
    ): List<Booking> {
        return bookingsDB.filter { it.houseId == hid }.drop(skip).take(limit)
    }

    override fun getBookingsByDate(
        date: LocalDate,
        skip: Int,
        limit: Int,
    ): List<Booking> {
        return bookingsDB.filter { booking ->
            val startsBeforeDate = booking.startDate <= date
            val endsAfterDate = booking.endDate >= date

            startsBeforeDate && endsAfterDate
        }.drop(skip).take(limit)
    }

    override fun getBookingsByUser(
        uid: Int,
        skip: Int,
        limit: Int,
    ): List<Booking> {
        return bookingsDB.filter { it.userId == uid }.drop(skip).take(limit)
    }

    override fun getAllBookings(
        skip: Int,
        limit: Int,
    ): List<Booking> {
        return bookingsDB.drop(skip).take(limit)
    }

    override fun getConflictingBookings(
        hid: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<Booking> {
        return bookingsDB.filter { booking ->
            if (booking.houseId != hid) {
                false
            } else {
                datesOverlap(
                    booking.startDate,
                    booking.endDate,
                    startDate,
                    endDate,
                )
            }
        }.drop(skip).take(limit)
    }

    override fun deleteBooking(bid: Int): Boolean {
        return bookingsDB.removeIf { it.id == bid }
    }

    override fun updateBooking(booking: Booking): Boolean {
        val index = bookingsDB.indexOfFirst { it.id == booking.id }
        return if (index != -1) {
            bookingsDB[index] = booking
            true
        } else {
            false
        }
    }
}

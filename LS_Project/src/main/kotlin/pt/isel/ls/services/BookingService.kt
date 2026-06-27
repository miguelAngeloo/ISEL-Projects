package pt.isel.ls.services

import kotlinx.datetime.LocalDate
import pt.isel.ls.data.StorageDataManager
import pt.isel.ls.domain.Booking
import pt.isel.ls.domain.requests.BookingCreationRequest
import pt.isel.ls.exceptions.BadRequestException
import pt.isel.ls.exceptions.NotFoundException
import pt.isel.ls.exceptions.UnauthorizedException
import pt.isel.ls.utils.validatePaging

class BookingService(
    dataManager: StorageDataManager,
) {
    private val bookingStorage = dataManager.bookingData
    private val houseStorage = dataManager.houseData
    private val userStorage = dataManager.userData
    private val tokenStorage = dataManager.tokenData

    fun createBooking(
        request: BookingCreationRequest,
        tokenString: String,
    ): Int {
        if (request.startDate > request.endDate) {
            throw BadRequestException("startDate must be before or equal to endDate")
        }

        val token =
            tokenStorage.getUserByToken(tokenString)
                ?: throw UnauthorizedException("Invalid or expired token")

        val house =
            houseStorage.getHouse(request.hid)
                ?: throw NotFoundException("House with id ${request.hid} not found")

        if (house.ownerId == token.uid) {
            throw BadRequestException("You cannot book your own house")
        }

        val conflicting =
            bookingStorage.getConflictingBookings(
                house.id,
                request.startDate,
                request.endDate,
                skip = 0,
                limit = 1,
            )
        if (conflicting.isNotEmpty()) {
            throw BadRequestException("House is not available for the given period")
        }

        val booking =
            Booking(
                id = 0,
                userId = token.uid,
                houseId = house.id,
                startDate = request.startDate,
                endDate = request.endDate,
            )

        return bookingStorage.createBooking(booking)
    }

    fun getBookingDetails(bid: Int): Booking =
        bookingStorage.getBooking(bid)
            ?: throw NotFoundException("Booking with id $bid not found")

    fun getBookings(
        hid: Int?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        skip: Int,
        limit: Int,
    ): List<Booking> {
        validatePaging(skip, limit)

        val hasStartDate = startDate != null
        val hasEndDate = endDate != null
        if (hasStartDate != hasEndDate) {
            throw BadRequestException("startDate and endDate must be used together")
        }

        if (startDate != null) {
            if (endDate != null && startDate > endDate) {
                throw BadRequestException("startDate must be before or equal to endDate")
            }
        }

        if (hid == null) {
            throw BadRequestException("hid is required")
        }

        if (startDate != null) {
            if (endDate != null) {
                return bookingStorage.getConflictingBookings(hid, startDate, endDate, skip, limit)
            }
        }

        return bookingStorage.getBookingsByHouse(hid, skip, limit)
    }

    fun getBookingsByUser(
        tokenString: String,
        skip: Int = 0,
        limit: Int = 10,
    ): List<Booking> {
        validatePaging(skip, limit)

        val token =
            tokenStorage.getUserByToken(tokenString)
                ?: throw UnauthorizedException("Invalid or expired token")

        return bookingStorage.getBookingsByUser(token.uid, skip, limit)
    }

    fun getBookingsByUserId(
        uid: Int,
        skip: Int = 0,
        limit: Int = 10,
    ): List<Booking> {
        validatePaging(skip, limit)

        userStorage.getUser(uid)
            ?: throw NotFoundException("User with id $uid not found")

        return bookingStorage.getBookingsByUser(uid, skip, limit)
    }

    fun deleteBooking(
        bid: Int,
        tokenString: String,
    ) {
        val token =
            tokenStorage.getUserByToken(tokenString)
                ?: throw UnauthorizedException("Invalid or expired token")

        val booking =
            bookingStorage.getBooking(bid)
                ?: throw NotFoundException("Booking with id $bid not found")

        if (booking.userId != token.uid) {
            throw UnauthorizedException("You can only delete your own bookings")
        }

        bookingStorage.deleteBooking(bid)
    }

    fun updateBooking(
        bid: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        tokenString: String,
    ): Booking {
        if (startDate > endDate) {
            throw BadRequestException("startDate must be before or equal to endDate")
        }

        val token =
            tokenStorage.getUserByToken(tokenString)
                ?: throw UnauthorizedException("Invalid or expired token")

        val booking =
            bookingStorage.getBooking(bid)
                ?: throw NotFoundException("Booking with id $bid not found")

        if (booking.userId != token.uid) {
            throw UnauthorizedException("You can only update your own bookings")
        }

        val house =
            houseStorage.getHouse(booking.houseId)
                ?: throw NotFoundException("House with id ${booking.houseId} not found")

        val bookingsInNewInterval =
            bookingStorage.getConflictingBookings(
                house.id,
                startDate,
                endDate,
                skip = 0,
                limit = 2,
            )

        val conflicting = bookingsInNewInterval.filter { it.id != bid }

        if (conflicting.isNotEmpty()) {
            throw BadRequestException("House is not available for the given period")
        }

        val updatedBooking =
            booking.copy(
                startDate = startDate,
                endDate = endDate,
            )

        bookingStorage.updateBooking(updatedBooking)
        return updatedBooking
    }
}

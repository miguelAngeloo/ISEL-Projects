package pt.isel.ls.webapi

import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.path
import pt.isel.ls.domain.dto.CreateBookingInput
import pt.isel.ls.domain.dto.CreateBookingOutput
import pt.isel.ls.domain.dto.GetBookingsOutput
import pt.isel.ls.domain.requests.BookingCreationRequest
import pt.isel.ls.exceptions.BadRequestException
import pt.isel.ls.exceptions.UnauthorizedException
import pt.isel.ls.services.BookingService
import pt.isel.ls.utils.getToken
import pt.isel.ls.utils.handleRequest
import pt.isel.ls.utils.parseDate

class BookingWebAPI(
    private val bookingService: BookingService,
) {
    fun createBooking(request: Request) =
        handleRequest(request) {
            val tokenString =
                getToken(request)
                    ?: throw UnauthorizedException("Missing authentication token")

            val input = Json.decodeFromString<CreateBookingInput>(request.bodyString())
            val bookingRequest =
                BookingCreationRequest(
                    hid = input.hid,
                    startDate = input.startDate,
                    endDate = input.endDate,
                )

            val newId = bookingService.createBooking(bookingRequest, tokenString)
            val output = CreateBookingOutput(newId)

            Response(CREATED)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun getBookingDetails(request: Request) =
        handleRequest(request) {
            val bid =
                request.path("id")?.toIntOrNull()
                    ?: throw BadRequestException("Invalid booking ID")

            val booking = bookingService.getBookingDetails(bid)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(booking))
        }

    fun getBookings(request: Request) =
        handleRequest(request) {
            val hid = request.query("hid")?.toIntOrNull()
            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull() ?: 10

            val startDate = request.query("startDate")?.let { parseDate(it, "startDate") }
            val endDate = request.query("endDate")?.let { parseDate(it, "endDate") }

            val bookings = bookingService.getBookings(hid, startDate, endDate, skip, limit)

            val output =
                GetBookingsOutput(
                    bookings = bookings,
                )

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun getBookingsByUserId(request: Request) =
        handleRequest(request) {
            val uid =
                request.path("id")?.toIntOrNull()
                    ?: throw BadRequestException("Invalid user ID")

            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull() ?: 10

            val bookings = bookingService.getBookingsByUserId(uid, skip, limit)

            val output =
                GetBookingsOutput(
                    bookings = bookings,
                )

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun getBookingsByUser(request: Request) =
        handleRequest(request) {
            val tokenString =
                getToken(request)
                    ?: throw UnauthorizedException("Missing authentication token")

            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull() ?: 10

            val bookings = bookingService.getBookingsByUser(tokenString, skip, limit)

            val output =
                GetBookingsOutput(
                    bookings = bookings,
                )

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun deleteBooking(request: Request) =
        handleRequest(request) {
            val tokenString =
                getToken(request)
                    ?: throw UnauthorizedException("Missing authentication token")

            val bid =
                request.path("id")?.toIntOrNull()
                    ?: throw BadRequestException("Invalid booking ID")

            bookingService.deleteBooking(bid, tokenString)

            Response(OK)
                .header("content-type", "application/json")
                .body("{}")
        }

    fun updateBooking(request: Request) =
        handleRequest(request) {
            val tokenString =
                getToken(request)
                    ?: throw UnauthorizedException("Missing authentication token")

            val bid =
                request.path("id")?.toIntOrNull()
                    ?: throw BadRequestException("Invalid booking ID")

            val input = Json.decodeFromString<CreateBookingInput>(request.bodyString())

            val updatedBooking =
                bookingService.updateBooking(bid, input.startDate, input.endDate, tokenString)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(updatedBooking))
        }
}

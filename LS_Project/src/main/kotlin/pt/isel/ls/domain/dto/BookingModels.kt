package pt.isel.ls.domain.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import pt.isel.ls.domain.Booking

@Serializable
data class CreateBookingInput(
    val hid: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

@Serializable
data class CreateBookingOutput(
    val id: Int,
)

@Serializable
data class GetBookingsOutput(
    val bookings: List<Booking>,
)

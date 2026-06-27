package pt.isel.ls.domain.requests

import kotlinx.datetime.LocalDate

data class BookingCreationRequest(
    val hid: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

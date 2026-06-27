package pt.isel.ls.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: Int,
    val userId: Int,
    val houseId: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

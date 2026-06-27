package pt.isel.ls.domain.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import pt.isel.ls.domain.House

@Serializable
data class CreateHouseInput(
    val title: String,
    val location: Int,
    val areaSqMt: Int,
    val pricePerNight: Double,
    val description: String,
)

@Serializable
data class CreateHouseOutput(
    val id: Int,
)

@Serializable
data class GetAllHousesOutput(
    val houses: List<House>,
)

@Serializable
data class HouseCountOutput(
    val count: Int,
)

@Serializable
data class AvailableHouse(
    val house: House,
    val available: Boolean,
)

@Serializable
data class GetAvailableHousesOutput(
    val houses: List<AvailableHouse>,
)

@Serializable
data class GetAvailableDaysOutput(
    val days: List<LocalDate>,
)

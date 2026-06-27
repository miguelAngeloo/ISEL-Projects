package pt.isel.ls.domain.requests

data class HouseCreationRequest(
    val title: String,
    val locationId: Int,
    val areaSqMt: Int,
    val pricePerNight: Double,
    val description: String,
)

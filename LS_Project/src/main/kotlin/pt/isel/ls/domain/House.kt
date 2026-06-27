package pt.isel.ls.domain

import kotlinx.serialization.Serializable

@Serializable
data class House(
    val id: Int,
    val ownerId: Int,
    val title: String,
    val locationId: Int,
    val areaSqMt: Int,
    val pricePerNight: Double,
    val description: String,
)

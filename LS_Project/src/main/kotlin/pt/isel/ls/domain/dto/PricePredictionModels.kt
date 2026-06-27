package pt.isel.ls.domain.dto

import kotlinx.serialization.Serializable

data class PricePredictionInputModel(
    val areaSqMt: Double,
    val lid: Int,
    val nights: Int,
)

@Serializable
data class PricePredictionOutputModel(
    val predictedPricePerNight: Double,
    val predictedTotalPrice: Double,
)

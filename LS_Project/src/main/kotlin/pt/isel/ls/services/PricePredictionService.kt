package pt.isel.ls.services

import pt.isel.ls.data.StorageDataManager
import pt.isel.ls.domain.House
import pt.isel.ls.domain.dto.PricePredictionOutputModel
import pt.isel.ls.domain.houses.HouseLinReg
import pt.isel.ls.domain.houses.Params
import pt.isel.ls.domain.houses.Scale
import pt.isel.ls.domain.houses.normalize
import pt.isel.ls.domain.houses.predict
import pt.isel.ls.domain.houses.train
import kotlin.collections.map
import kotlin.math.round

class PricePredictionService(
    dataManager: StorageDataManager,
) {
    private val houseStorage = dataManager.houseData
    private val trainedModelsByLocation = buildTrainedModels(houseStorage.getAllHouses(0, Int.MAX_VALUE))

    fun getPricePrediction(
        areaSqMt: Double,
        locationId: Int,
        nights: Int,
    ): PricePredictionOutputModel {
        val trainedModel =
            trainedModelsByLocation[locationId]
                ?: throw IllegalArgumentException(
                    "Not enough historical data in this location to perform a prediction. At least 2 houses are required.",
                )

        val areaNorm = trainedModel.areasScale.normalize(areaSqMt)
        val priceNorm = predict(areaNorm, trainedModel.params)
        val finalPrediction = trainedModel.pricesScale.denormalize(priceNorm)

        val roundedPrice = round(finalPrediction * 100) / 100.0
        val roundedTotal = round((roundedPrice * nights) * 100) / 100.0

        return PricePredictionOutputModel(
            predictedPricePerNight = roundedPrice,
            predictedTotalPrice = roundedTotal,
        )
    }

    private fun buildTrainedModels(houses: List<House>): Map<Int, TrainedLocationModel> =
        houses
            .groupBy { it.locationId }
            .mapNotNull { (locationId, housesInLocation) ->
                if (housesInLocation.size < 2) {
                    null
                } else {
                    val trainingData =
                        housesInLocation.map {
                            HouseLinReg(area = it.areaSqMt.toDouble(), price = it.pricePerNight)
                        }
                    val (areasScale, pricesScale, normalizedData) = trainingData.normalize()
                    locationId to TrainedLocationModel(areasScale, pricesScale, train(normalizedData))
                }
            }.toMap()

    private data class TrainedLocationModel(
        val areasScale: Scale,
        val pricesScale: Scale,
        val params: Params,
    )
}

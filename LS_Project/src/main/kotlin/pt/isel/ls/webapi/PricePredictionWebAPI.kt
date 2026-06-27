package pt.isel.ls.webapi

import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import pt.isel.ls.exceptions.BadRequestException
import pt.isel.ls.services.PricePredictionService
import pt.isel.ls.utils.handleRequest

class PricePredictionWebAPI(
    private val pricePredictionService: PricePredictionService,
) {
    fun getPricePrediction(request: Request) =
        handleRequest(request) {
            val areaSqMt =
                request.query("areaSqMt")?.toDoubleOrNull()
                    ?: throw BadRequestException("Missing or invalid areaSqMt")

            val lid =
                request.query("lid")?.toIntOrNull()
                    ?: throw BadRequestException("Missing or invalid lid")

            val nights =
                request.query("nights")?.toIntOrNull()
                    ?: throw BadRequestException("Missing or invalid nights")

            val result =
                pricePredictionService.getPricePrediction(
                    areaSqMt,
                    lid,
                    nights,
                )

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(result))
        }
}

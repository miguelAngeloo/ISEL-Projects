package pt.isel.ls.webapi

import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.path
import pt.isel.ls.domain.dto.AvailableHouse
import pt.isel.ls.domain.dto.CreateHouseInput
import pt.isel.ls.domain.dto.CreateHouseOutput
import pt.isel.ls.domain.dto.GetAllHousesOutput
import pt.isel.ls.domain.dto.GetAvailableDaysOutput
import pt.isel.ls.domain.dto.GetAvailableHousesOutput
import pt.isel.ls.domain.dto.HouseCountOutput
import pt.isel.ls.domain.requests.HouseCreationRequest
import pt.isel.ls.exceptions.BadRequestException
import pt.isel.ls.exceptions.UnauthorizedException
import pt.isel.ls.services.HouseService
import pt.isel.ls.utils.getToken
import pt.isel.ls.utils.handleRequest
import pt.isel.ls.utils.parseDate

class HouseWebAPI(
    private val houseService: HouseService,
) {
    fun createHouse(request: Request) =
        handleRequest(request) {
            val tokenString =
                getToken(request)
                    ?: throw UnauthorizedException("Missing Authorization header")

            val input = Json.decodeFromString<CreateHouseInput>(request.bodyString())
            val houseRequest =
                HouseCreationRequest(
                    title = input.title,
                    locationId = input.location,
                    areaSqMt = input.areaSqMt,
                    pricePerNight = input.pricePerNight,
                    description = input.description,
                )
            val newId = houseService.createHouse(houseRequest, tokenString)
            val output = CreateHouseOutput(newId)

            Response(CREATED)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun getHouseDetails(request: Request) =
        handleRequest(request) {
            val hid =
                request.path("id")?.toIntOrNull()
                    ?: throw BadRequestException("Invalid house ID")

            val house = houseService.getHouseDetails(hid)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(house))
        }

    fun getAllHouses(request: Request) =
        handleRequest(request) {
            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull() ?: 10
            val locationId = request.query("locationId")?.toIntOrNull()

            val houses = houseService.getAllHouses(skip, limit, locationId)

            val output =
                GetAllHousesOutput(
                    houses = houses,
                )

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun countHouses(request: Request) =
        handleRequest(request) {
            val output = HouseCountOutput(houseService.countHouses())

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun searchHouses(request: Request) =
        handleRequest(request) {
            val locationId =
                request.query("locationId")?.toIntOrNull()
                    ?: throw BadRequestException("Missing or invalid locationId")

            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull() ?: 10

            val houses =
                houseService.searchHouses(
                    locationId = locationId,
                    minPrice = request.query("minPrice")?.toDoubleOrNull(),
                    maxPrice = request.query("maxPrice")?.toDoubleOrNull(),
                    skip = skip,
                    limit = limit,
                )

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(GetAllHousesOutput(houses)))
        }

    fun getAvailableHouses(request: Request) =
        handleRequest(request) {
            val startDateParam =
                request.query("startDate")
                    ?: throw BadRequestException("Missing startDate query parameter")
            val endDateParam =
                request.query("endDate")
                    ?: throw BadRequestException("Missing endDate query parameter")

            val startDate = parseDate(startDateParam, "startDate")
            val endDate = parseDate(endDateParam, "endDate")

            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull() ?: 10
            val locationId = request.query("locationId")?.toIntOrNull()

            val houses = houseService.getAvailableHouses(startDate, endDate, skip, limit, locationId)

            val output =
                GetAvailableHousesOutput(
                    houses = houses.map { AvailableHouse(it, available = true) },
                )

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun getAvailableDays(request: Request) =
        handleRequest(request) {
            val hid =
                request.path("id")?.toIntOrNull()
                    ?: throw BadRequestException("Invalid house ID")
            val year =
                request.query("year")?.toIntOrNull()
                    ?: throw BadRequestException("Missing or invalid year")
            val month =
                request.query("month")?.toIntOrNull()
                    ?: throw BadRequestException("Missing or invalid month")

            val days = houseService.getAvailableDays(hid, year, month)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(GetAvailableDaysOutput(days)))
        }
}

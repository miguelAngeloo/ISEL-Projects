package pt.isel.ls.webapi

import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.path
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.dto.CreateLocationInput
import pt.isel.ls.domain.dto.CreateLocationOutput
import pt.isel.ls.exceptions.BadRequestException
import pt.isel.ls.exceptions.UnauthorizedException
import pt.isel.ls.services.LocationService
import pt.isel.ls.utils.getToken
import pt.isel.ls.utils.handleRequest

class LocationWebAPI(
    private val locationService: LocationService,
) {
    fun getLocationDetails(request: Request) =
        handleRequest(request) {
            val lid =
                request.path("id")?.toIntOrNull()
                    ?: throw BadRequestException("Invalid Location ID")

            val location = locationService.getLocationDetails(lid)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(location))
        }

    fun createNewLocation(request: Request) =
        handleRequest(request) {
            val tokenString = getToken(request) ?: throw UnauthorizedException("Missing or invalid Bearer token")

            val input = Json.decodeFromString<CreateLocationInput>(request.bodyString())

            val location =
                Location(
                    lid = 0,
                    name = input.name,
                    type = input.type,
                    parentId = input.parentId,
                )

            val newId = locationService.createNewLocation(location, tokenString)

            Response(CREATED)
                .header("content-type", "application/json")
                .body(Json.encodeToString(CreateLocationOutput(newId)))
        }

    fun getChildrenLocations(request: Request) =
        handleRequest(request) {
            val lid =
                request.path("id")?.toIntOrNull()
                    ?: throw BadRequestException("Invalid Location ID")

            val output = locationService.getChildrenLocations(lid)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun getFullPath(request: Request) =
        handleRequest(request) {
            val lid =
                request.path("id")?.toIntOrNull()
                    ?: throw BadRequestException("Invalid Location ID")

            val output = locationService.getFullHierarchicalPath(lid)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun getLocations(request: Request) =
        handleRequest(request) {
            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull() ?: 10

            val locations = locationService.getLocations(skip, limit)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(locations))
        }
}

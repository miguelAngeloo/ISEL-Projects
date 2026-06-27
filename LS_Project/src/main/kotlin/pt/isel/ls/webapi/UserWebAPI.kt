package pt.isel.ls.webapi

import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.path
import pt.isel.ls.domain.dto.UpdateUserInputModel
import pt.isel.ls.domain.dto.UserModels
import pt.isel.ls.exceptions.BadRequestException
import pt.isel.ls.exceptions.UnauthorizedException
import pt.isel.ls.services.UserService
import pt.isel.ls.utils.getToken
import pt.isel.ls.utils.handleRequest

class UserWebAPI(
    private val userServices: UserService,
) {
    fun getUserDetails(request: Request) =
        handleRequest(request) {
            val uid =
                request.path("id")?.toIntOrNull()
                    ?: throw BadRequestException("Invalid user ID")

            val user = userServices.getUserDetails(uid)

            val output =
                UserModels(
                    userId = user.uid,
                    name = user.name.value,
                    email = user.email.value,
                )

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun updateUser(request: Request) =
        handleRequest(request) {
            val tokenString =
                getToken(request)
                    ?: throw UnauthorizedException("Missing authentication token")

            val input = Json.decodeFromString<UpdateUserInputModel>(request.bodyString())

            if (input.name.isBlank()) {
                throw BadRequestException("Missing name")
            }

            if (input.name.trim().length !in 3..20) {
                throw BadRequestException("Name must be between 3 and 20 characters")
            }

            val user = userServices.updateUser(tokenString, input.name)

            val output =
                UserModels(
                    userId = user.uid,
                    name = input.name,
                    email = user.email.value,
                )

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(output))
        }

    fun deleteUser(request: Request) =
        handleRequest(request) {
            val tokenString =
                getToken(request)
                    ?: throw UnauthorizedException("Missing authentication token")

            userServices.deleteUser(tokenString)

            Response(OK)
                .header("content-type", "application/json")
                .body("{}")
        }
}

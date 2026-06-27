package pt.isel.ls.webapi

import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import pt.isel.ls.domain.dto.LoginInputModel
import pt.isel.ls.domain.dto.RegisterInputModel
import pt.isel.ls.domain.dto.RegisterOutputModel
import pt.isel.ls.domain.dto.TokenOutputModel
import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName
import pt.isel.ls.exceptions.BadRequestException
import pt.isel.ls.exceptions.UnauthorizedException
import pt.isel.ls.services.AuthService
import pt.isel.ls.utils.getToken
import pt.isel.ls.utils.handleRequest

class AuthWebAPI(
    private val authServices: AuthService,
) {
    fun register(request: Request) =
        handleRequest(request) {
            val input = Json.decodeFromString<RegisterInputModel>(request.bodyString())
            val hasMissingField =
                input.email.isBlank() || input.name.isBlank() || input.password.isBlank()

            if (hasMissingField) {
                throw BadRequestException("Invalid email, name, or password")
            }

            val userName = UserName(input.name)
            val email = Email(input.email)
            val password = Password(input.password)

            val result = authServices.register(userName, email, password)

            Response(CREATED)
                .header("content-type", "application/json")
                .body(Json.encodeToString(RegisterOutputModel(result.first, result.second.token.toString())))
        }

    fun login(request: Request) =
        handleRequest(request) {
            val input = Json.decodeFromString<LoginInputModel>(request.bodyString())
            val hasMissingField =
                input.email.isBlank() ||
                    input.password.isBlank()

            if (hasMissingField) {
                throw BadRequestException("Invalid credentials format")
            }

            val token = authServices.login(input.email, input.password)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(TokenOutputModel(token.token.toString(), token.uid)))
        }

    fun logout(request: Request) =
        handleRequest(request) {
            val token = getToken(request) ?: throw UnauthorizedException("Missing authentication token")

            authServices.logout(token)

            Response(OK)
                .header("content-type", "application/json")
                .body("""{"message": "Logged out successfully"}""")
        }
}

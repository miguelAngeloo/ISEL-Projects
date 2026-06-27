package pt.isel.ls.utils

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import pt.isel.ls.exceptions.BadRequestException
import pt.isel.ls.exceptions.HouseRentalsException
import pt.isel.ls.exceptions.InternalServerErrorException
import pt.isel.ls.exceptions.UnsupportedMediaTypeException
import pt.isel.ls.exceptions.toExceptionDTO
import pt.isel.ls.server.logRequest
import pt.isel.ls.server.logger

fun handleRequest(
    request: Request,
    handler: (Request) -> Response,
): Response {
    logRequest(request)

    val hasBody = request.bodyString().isNotBlank()
    val isJsonRequest = request.header("content-type") == "application/json"

    if (hasBody && !isJsonRequest) {
        return Response(BAD_REQUEST)
            .header("content-type", "application/json")
            .body(Json.encodeToString(UnsupportedMediaTypeException().toExceptionDTO()))
    }

    return try {
        handler(request)
    } catch (e: IllegalArgumentException) {
        val message =
            if (e.message?.contains("could not be parsed") == true) {
                "Invalid date format, expected YYYY-MM-DD"
            } else {
                e.message ?: "Invalid input"
            }
        Response(BAD_REQUEST)
            .header("content-type", "application/json")
            .body(Json.encodeToString(mapOf("error" to message)))
    } catch (e: HouseRentalsException) {
        Response(Status(e.status, e.description))
            .header("content-type", "application/json")
            .body(Json.encodeToString(e.toExceptionDTO()))
    } catch (e: Exception) {
        logger.error(e.message, e)
        Response(INTERNAL_SERVER_ERROR)
            .header("content-type", "application/json")
            .body(Json.encodeToString(InternalServerErrorException().toExceptionDTO()))
    }
}

fun datesOverlap(
    start1: LocalDate,
    end1: LocalDate,
    start2: LocalDate,
    end2: LocalDate,
): Boolean {
    val firstStartsBeforeSecondEnds = start1 <= end2
    val secondStartsBeforeFirstEnds = start2 <= end1

    return firstStartsBeforeSecondEnds && secondStartsBeforeFirstEnds
}

fun validatePaging(
    skip: Int,
    limit: Int,
) {
    if (skip < 0) throw BadRequestException("skip must be >= 0")
    if (limit < 1) throw BadRequestException("limit must be >= 1")
}

fun parseDate(
    value: String,
    fieldName: String,
): LocalDate =
    try {
        LocalDate.parse(value)
    } catch (e: Exception) {
        throw BadRequestException("Invalid $fieldName format, expected YYYY-MM-DD")
    }

fun getToken(request: Request): String? {
    val header = request.header("Authorization") ?: return null

    if (!header.startsWith("Bearer ")) {
        return null
    }

    return header.substring(7).trim()
}

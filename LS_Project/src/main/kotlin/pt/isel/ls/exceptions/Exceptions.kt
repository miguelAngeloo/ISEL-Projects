package pt.isel.ls.exceptions

import org.http4k.core.Status
import pt.isel.ls.domain.dto.ExceptionOutputModel

open class HouseRentalsException(
    val status: Int,
    val description: String,
    val errorCause: String? = null,
) : Exception(errorCause)

class BadRequestException(
    cause: String?,
) : HouseRentalsException(Status.BAD_REQUEST.code, "Bad Request", cause)

class NotFoundException(
    item: String?,
) : HouseRentalsException(Status.NOT_FOUND.code, "Not Found", item)

class InternalServerErrorException :
    HouseRentalsException(Status.INTERNAL_SERVER_ERROR.code, "Internal Server Error", null)

class UnsupportedMediaTypeException :
    HouseRentalsException(Status.UNSUPPORTED_MEDIA_TYPE.code, "Unsupported Media Type", null)

class UnauthorizedException(
    cause: String? = "Invalid Auth",
) : HouseRentalsException(Status.UNAUTHORIZED.code, "Unauthorized", cause)

class AlreadyExistsException(
    message: String,
) : HouseRentalsException(Status.CONFLICT.code, "Already Exists", message)

fun HouseRentalsException.toExceptionDTO() = ExceptionOutputModel(status, description, errorCause)

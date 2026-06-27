package pt.isel.ls.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExceptionOutputModel(
    val status: Int,
    val description: String,
    val errorCause: String? = null,
)

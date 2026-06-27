package pt.isel.ls.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterInputModel(
    val name: String,
    val email: String,
    val password: String,
)

@Serializable
data class RegisterOutputModel(
    val userId: Int,
    val token: String,
)

@Serializable
data class LoginInputModel(
    val email: String,
    val password: String,
)

@Serializable
data class TokenOutputModel(
    val token: String,
    val userId: Int,
)

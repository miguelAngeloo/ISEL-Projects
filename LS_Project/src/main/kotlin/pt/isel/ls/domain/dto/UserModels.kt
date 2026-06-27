package pt.isel.ls.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserModels(
    val userId: Int,
    val name: String,
    val email: String,
)

@Serializable
data class UpdateUserInputModel(
    val name: String,
)

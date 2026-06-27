package pt.isel.ls.domain.dto

import kotlinx.serialization.Serializable
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationType

@Serializable
data class CreateLocationInput(
    val name: String,
    val type: LocationType,
    val parentId: Int? = null,
)

@Serializable
data class CreateLocationOutput(
    val lid: Int,
)

@Serializable
data class GetChildLocationsOutput(
    val locations: List<Location>,
)

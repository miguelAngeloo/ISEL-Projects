package pt.isel.ls.domain

import kotlinx.serialization.Serializable

@Serializable
enum class LocationType {
    COUNTRY,
    REGION,
    DISTRICT,
    MUNICIPALITY,
    LOCALITY,
}

@Serializable
data class Location(
    val lid: Int,
    val name: String,
    val type: LocationType,
    val parentId: Int?,
)

@Serializable
data class LocationPath(
    val path: List<Location>,
)

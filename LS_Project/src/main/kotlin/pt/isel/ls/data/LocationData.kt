package pt.isel.ls.data

import pt.isel.ls.domain.Location

interface LocationData {
    fun createLocation(location: Location): Int

    fun getLocation(lid: Int): Location?

    fun getChildLocations(location: Location): List<Location>

    fun getLocationPath(lid: Int): List<Location>

    fun getLocations(
        skip: Int,
        limit: Int,
    ): List<Location>
}

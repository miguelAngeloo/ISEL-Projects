package pt.isel.ls.data.mem

import pt.isel.ls.data.LocationData
import pt.isel.ls.domain.Location

class LocationDataMem : MemStorage(), LocationData {
    override fun createLocation(location: Location): Int {
        val newLid = lid
        val location = location.copy(lid = newLid)
        locationsDB.add(location)
        return newLid
    }

    override fun getLocation(lid: Int): Location? {
        return locationsDB.find { it.lid == lid }
    }

    override fun getChildLocations(location: Location): List<Location> {
        return locationsDB.filter { it.parentId == location.lid }
    }

    override fun getLocationPath(lid: Int): List<Location> {
        val path = mutableListOf<Location>()
        var currentLocation = getLocation(lid)

        while (currentLocation != null) {
            path.add(0, currentLocation)
            val parentId = currentLocation.parentId
            currentLocation =
                if (parentId == null) {
                    null
                } else {
                    getLocation(parentId)
                }
        }
        return path
    }

    override fun getLocations(
        skip: Int,
        limit: Int,
    ): List<Location> {
        return locationsDB.drop(skip).take(limit)
    }
}

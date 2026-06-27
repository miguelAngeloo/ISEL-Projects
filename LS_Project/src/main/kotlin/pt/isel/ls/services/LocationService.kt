package pt.isel.ls.services

import pt.isel.ls.data.StorageDataManager
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationPath
import pt.isel.ls.domain.dto.GetChildLocationsOutput
import pt.isel.ls.exceptions.NotFoundException
import pt.isel.ls.exceptions.UnauthorizedException
import pt.isel.ls.utils.validatePaging

class LocationService(
    dataManager: StorageDataManager,
) {
    private val locationStorage = dataManager.locationData
    private val tokenStorage = dataManager.tokenData

    fun getLocationDetails(lid: Int): Location {
        return locationStorage.getLocation(lid)
            ?: throw NotFoundException("Location with id = $lid not found")
    }

    fun getChildrenLocations(lid: Int): GetChildLocationsOutput {
        val location =
            locationStorage.getLocation(lid)
                ?: throw NotFoundException("Location with id = $lid not found")
        return GetChildLocationsOutput(locationStorage.getChildLocations(location))
    }

    fun getFullHierarchicalPath(lid: Int): LocationPath {
        locationStorage.getLocation(lid)
            ?: throw NotFoundException("Location with id = $lid not found")
        return LocationPath(locationStorage.getLocationPath(lid))
    }

    fun createNewLocation(
        location: Location,
        tokenString: String,
    ): Int {
        tokenStorage.getUserByToken(tokenString)
            ?: throw UnauthorizedException("Invalid or expired token")
        return locationStorage.createLocation(location)
    }

    fun getLocations(
        skip: Int,
        limit: Int,
    ): List<Location> {
        validatePaging(skip, limit)
        return locationStorage.getLocations(skip, limit)
    }
}

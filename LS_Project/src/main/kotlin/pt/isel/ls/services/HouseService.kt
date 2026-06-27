package pt.isel.ls.services

import kotlinx.datetime.LocalDate
import pt.isel.ls.data.StorageDataManager
import pt.isel.ls.data.cache.HouseDetailsCache
import pt.isel.ls.domain.House
import pt.isel.ls.domain.requests.HouseCreationRequest
import pt.isel.ls.exceptions.BadRequestException
import pt.isel.ls.exceptions.NotFoundException
import pt.isel.ls.exceptions.UnauthorizedException
import pt.isel.ls.utils.validatePaging
import java.time.YearMonth

class HouseService(
    dataManager: StorageDataManager,
    houseDetailsCacheSize: Int = 10,
) {
    private val houseStorage = dataManager.houseData
    private val locationStorage = dataManager.locationData
    private val bookingStorage = dataManager.bookingData
    private val tokenStorage = dataManager.tokenData
    private val houseDetailsCache = HouseDetailsCache(houseStorage, houseDetailsCacheSize)

    fun createHouse(
        request: HouseCreationRequest,
        tokenString: String,
    ): Int {
        if (request.title.isBlank()) {
            throw BadRequestException("House title cannot be blank")
        }
        if (request.areaSqMt <= 0) {
            throw BadRequestException("House area must be greater than zero")
        }
        if (request.pricePerNight <= 0) {
            throw BadRequestException("House pricePerNight must be greater than zero")
        }

        val token =
            tokenStorage.getUserByToken(tokenString)
                ?: throw UnauthorizedException("Invalid token")

        val location =
            locationStorage.getLocation(request.locationId)
                ?: throw NotFoundException("Location with id ${request.locationId} not found")

        val house =
            House(
                id = 0,
                ownerId = token.uid,
                title = request.title,
                locationId = location.lid,
                areaSqMt = request.areaSqMt,
                pricePerNight = request.pricePerNight,
                description = request.description,
            )

        return houseStorage.createHouse(house)
    }

    fun getHouseDetails(hid: Int): House {
        return houseDetailsCache.getHouse(hid)
            ?: throw NotFoundException("House with id $hid not found")
    }

    fun getAllHouses(
        skip: Int,
        limit: Int,
        locationId: Int? = null,
    ): List<House> {
        validatePaging(skip, limit)

        return if (locationId != null) {
            houseStorage.getHousesByLocation(locationId, skip, limit)
        } else {
            houseStorage.getAllHouses(skip, limit)
        }
    }

    fun countHouses(): Int {
        return houseStorage.countHouses()
    }

    fun getAvailableHouses(
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
        locationId: Int? = null,
    ): List<House> {
        if (startDate > endDate) {
            throw BadRequestException("startDate must be before or equal to endDate")
        }
        validatePaging(skip, limit)

        return if (locationId != null) {
            houseStorage.getAvailableHousesByLocation(locationId, startDate, endDate, skip, limit)
        } else {
            houseStorage.getAvailableHouses(startDate, endDate, skip, limit)
        }
    }

    fun searchHouses(
        locationId: Int,
        minPrice: Double?,
        maxPrice: Double?,
        skip: Int,
        limit: Int,
    ): List<House> {
        validatePaging(skip, limit)

        if (minPrice != null) {
            if (minPrice < 0) {
                throw BadRequestException("minPrice must be >= 0")
            }
        }

        if (maxPrice != null) {
            if (maxPrice < 0) {
                throw BadRequestException("maxPrice must be >= 0")
            }
        }

        if (minPrice != null) {
            if (maxPrice != null && minPrice > maxPrice) {
                throw BadRequestException("minPrice must be <= maxPrice")
            }
        }

        locationStorage.getLocation(locationId)
            ?: throw NotFoundException("Location with id $locationId not found")

        if (minPrice == null && maxPrice == null) {
            throw BadRequestException("Use location and at least one price criterion")
        }

        return houseStorage.searchHouses(
            locationId,
            minPrice,
            maxPrice,
            skip,
            limit,
        )
    }

    fun getAvailableDays(
        hid: Int,
        year: Int,
        month: Int,
    ): List<LocalDate> {
        if (month !in 1..12) throw BadRequestException("month must be between 1 and 12")

        houseStorage.getHouse(hid)
            ?: throw NotFoundException("House with id $hid not found")

        val yearMonth = YearMonth.of(year, month)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDay = LocalDate(year, month, 1)
        val lastDay = LocalDate(year, month, daysInMonth)
        val allDays = mutableListOf<LocalDate>()
        for (day in 1..daysInMonth) {
            allDays.add(LocalDate(year, month, day))
        }

        val bookedDays = mutableSetOf<LocalDate>()
        val bookingsInMonth = bookingStorage.getConflictingBookings(hid, firstDay, lastDay, 0, daysInMonth)
        for (booking in bookingsInMonth) {
            for (date in allDays) {
                if (date >= booking.startDate && date <= booking.endDate) {
                    bookedDays.add(date)
                }
            }
        }

        return allDays.filter { date -> date !in bookedDays }
    }
}

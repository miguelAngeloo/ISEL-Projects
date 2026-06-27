package pt.isel.ls.data.mem

import kotlinx.datetime.LocalDate
import pt.isel.ls.data.HouseData
import pt.isel.ls.domain.House
import pt.isel.ls.utils.datesOverlap

class HouseDataMem : MemStorage(), HouseData {
    override fun createHouse(house: House): Int {
        val newHid = hid
        val newHouse = house.copy(id = newHid)
        housesDB.add(newHouse)
        return newHid
    }

    override fun getHouse(hid: Int): House? {
        return housesDB.find { it.id == hid }
    }

    override fun getAllHouses(
        skip: Int,
        limit: Int,
    ): List<House> {
        return housesDB.drop(skip).take(limit)
    }

    override fun countHouses(): Int {
        return housesDB.size
    }

    override fun getAvailableHouses(
        startDate: kotlinx.datetime.LocalDate,
        endDate: kotlinx.datetime.LocalDate,
        skip: Int,
        limit: Int,
    ): List<House> {
        if (startDate > endDate) return emptyList()

        return housesDB.filter { house ->
            val conflictingBookings =
                bookingsDB.filter { booking ->
                    if (booking.houseId != house.id) {
                        false
                    } else {
                        datesOverlap(
                            booking.startDate,
                            booking.endDate,
                            startDate,
                            endDate,
                        )
                    }
                }
            conflictingBookings.isEmpty()
        }.drop(skip).take(limit)
    }

    override fun getAvailableHousesByLocation(
        locationId: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<House> {
        if (startDate > endDate) return emptyList()

        return housesDB.filter { house ->
            if (house.locationId != locationId) {
                false
            } else {
                bookingsDB.none { booking ->
                    if (booking.houseId != house.id) {
                        false
                    } else {
                        datesOverlap(
                            booking.startDate,
                            booking.endDate,
                            startDate,
                            endDate,
                        )
                    }
                }
            }
        }.drop(skip).take(limit)
    }

    override fun getHousesByLocation(
        locationId: Int,
        skip: Int,
        limit: Int,
    ): List<House> {
        return housesDB.filter { it.locationId == locationId }.drop(skip).take(limit)
    }

    override fun searchHouses(
        locationId: Int,
        minPrice: Double?,
        maxPrice: Double?,
        skip: Int,
        limit: Int,
    ): List<House> {
        return housesDB
            .asSequence()
            .filter { it.locationId == locationId }
            .filter { house ->
                if (minPrice == null) {
                    true
                } else {
                    house.pricePerNight >= minPrice
                }
            }
            .filter { house ->
                if (maxPrice == null) {
                    true
                } else {
                    house.pricePerNight <= maxPrice
                }
            }
            .drop(skip)
            .take(limit)
            .toList()
    }
}

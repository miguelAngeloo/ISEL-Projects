package pt.isel.ls.data

import kotlinx.datetime.LocalDate
import pt.isel.ls.domain.House

interface HouseData {
    fun createHouse(house: House): Int

    fun getHouse(hid: Int): House?

    fun getAllHouses(
        skip: Int,
        limit: Int,
    ): List<House>

    fun countHouses(): Int

    fun getAvailableHouses(
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<House>

    fun getAvailableHousesByLocation(
        locationId: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<House>

    fun getHousesByLocation(
        locationId: Int,
        skip: Int,
        limit: Int,
    ): List<House>

    fun searchHouses(
        locationId: Int,
        minPrice: Double?,
        maxPrice: Double?,
        skip: Int,
        limit: Int,
    ): List<House>
}

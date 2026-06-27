package pt.isel.ls.data.cache

import pt.isel.ls.data.HouseData
import pt.isel.ls.domain.House

class HouseDetailsCache(
    private val houseData: HouseData,
    private val maxSize: Int,
) {
    init {
        require(maxSize > 0) { "Cache size must be greater than zero" }
    }

    private val houses = mutableMapOf<Int, House>()
    private val accessOrder = mutableListOf<Int>()

    fun getHouse(hid: Int): House? {
        val cachedHouse = houses[hid]
        if (cachedHouse != null) {
            println("House came from cache")
            updateAccessOrder(hid)
            return cachedHouse
        }

        val house = houseData.getHouse(hid) ?: return null
        println("House came from storage")
        houses[hid] = house
        updateAccessOrder(hid)

        if (houses.size > maxSize) {
            val oldestHid = accessOrder.removeFirst()
            houses.remove(oldestHid)
        }

        return house
    }

    private fun updateAccessOrder(hid: Int) {
        accessOrder.remove(hid)
        accessOrder.add(hid)
    }
}

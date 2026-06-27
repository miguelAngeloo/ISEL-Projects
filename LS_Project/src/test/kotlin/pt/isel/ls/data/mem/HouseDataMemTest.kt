package pt.isel.ls.data.mem

import kotlinx.datetime.LocalDate
import pt.isel.ls.domain.Booking
import pt.isel.ls.domain.House
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HouseDataMemTest {
    private lateinit var houseData: HouseDataMem
    private lateinit var bookingData: BookingDataMem

    @BeforeTest
    fun setup() {
        houseData = HouseDataMem()
        bookingData = BookingDataMem()
        houseData.clear()
        bookingData.clear()
    }

    @Test
    fun `create house should generate new id and store house`() {
        val house =
            House(
                id = 0,
                ownerId = 1,
                title = "Test House",
                locationId = 1,
                areaSqMt = 100,
                pricePerNight = 80.0,
                description = "Test description",
            )

        val newId = houseData.createHouse(house)

        assertEquals(1, newId)
        val savedHouse = houseData.getHouse(newId)
        assertNotNull(savedHouse)
        assertEquals(newId, savedHouse.id)
        assertEquals("Test House", savedHouse.title)
    }

    @Test
    fun `get house should return correct house`() {
        val house =
            House(
                id = 0,
                ownerId = 1,
                title = "Test House",
                locationId = 1,
                areaSqMt = 100,
                pricePerNight = 80.0,
                description = "Test description",
            )

        val createdId = houseData.createHouse(house)
        val retrievedHouse = houseData.getHouse(createdId)

        assertNotNull(retrievedHouse)
        assertEquals(createdId, retrievedHouse.id)
        assertEquals("Test House", retrievedHouse.title)
    }

    @Test
    fun `get house with invalid id should return null`() {
        val result = houseData.getHouse(999)
        assertEquals(null, result)
    }

    @Test
    fun `get all houses should return all stored houses`() {
        val house1 = House(0, 1, "House 1", 1, 80, 60.0, "Desc 1")
        val house2 = House(0, 1, "House 2", 1, 90, 70.0, "Desc 2")
        val house3 = House(0, 1, "House 3", 1, 100, 80.0, "Desc 3")

        houseData.createHouse(house1)
        houseData.createHouse(house2)
        houseData.createHouse(house3)

        val allHouses = houseData.getAllHouses(0, 10)

        assertEquals(3, allHouses.size)
        assertEquals(listOf("House 1", "House 2", "House 3"), allHouses.map { it.title })
    }

    @Test
    fun `get all houses should return empty list when no houses exist`() {
        val allHouses = houseData.getAllHouses(0, 10)
        assertEquals(0, allHouses.size)
        assertTrue(allHouses.isEmpty())
    }

    @Test
    fun `get available houses should return all houses when no bookings exist`() {
        val house1 = House(0, 1, "House 1", 1, 80, 60.0, "Desc 1")
        val house2 = House(0, 1, "House 2", 1, 90, 70.0, "Desc 2")

        houseData.createHouse(house1)
        houseData.createHouse(house2)

        val startDate = LocalDate(2026, 6, 10)
        val endDate = LocalDate(2026, 6, 15)

        val availableHouses = houseData.getAvailableHouses(startDate, endDate, 0, 10)

        assertEquals(2, availableHouses.size)
    }

    @Test
    fun `get available houses should exclude houses with conflicting bookings`() {
        val house1 = House(0, 1, "House 1", 1, 80, 60.0, "Desc 1")
        val house2 = House(0, 1, "House 2", 1, 90, 70.0, "Desc 2")

        val house1Id = houseData.createHouse(house1)
        val house2Id = houseData.createHouse(house2)

        // Create a conflicting booking using BookingDataMem
        val conflictingBooking =
            Booking(
                id = 0,
                userId = 1,
                houseId = house1Id,
                startDate = LocalDate(2026, 6, 12),
                endDate = LocalDate(2026, 6, 18),
            )
        bookingData.createBooking(conflictingBooking)

        val searchStart = LocalDate(2026, 6, 10)
        val searchEnd = LocalDate(2026, 6, 15)

        val availableHouses = houseData.getAvailableHouses(searchStart, searchEnd, 0, 10)

        assertEquals(1, availableHouses.size)
        assertEquals(house2Id, availableHouses.first().id)
    }

    @Test
    fun `clear should remove all houses`() {
        val house = House(0, 1, "Test House", 1, 80, 60.0, "Desc")
        houseData.createHouse(house)

        assertEquals(1, houseData.getAllHouses(0, 10).size)

        houseData.clear()

        assertEquals(0, houseData.getAllHouses(0, 10).size)
        assertTrue(houseData.getAllHouses(0, 10).isEmpty())
    }

    @Test
    fun `get available houses with date range should return correct houses`() {
        val house = House(0, 1, "Test House", 1, 100, 80.0, "Test desc")
        val houseId = houseData.createHouse(house)

        val conflictingBooking =
            Booking(
                id = 0,
                userId = 1,
                houseId = houseId,
                startDate = LocalDate(2026, 6, 12),
                endDate = LocalDate(2026, 6, 18),
            )
        bookingData.createBooking(conflictingBooking)

        val startDate = LocalDate(2026, 6, 10)
        val endDate = LocalDate(2026, 6, 15)

        val availableHouses = houseData.getAvailableHouses(startDate, endDate, 0, 10)

        assertEquals(0, availableHouses.size)
        assertTrue(availableHouses.isEmpty())
    }

    @Test
    fun `get available houses with start date after end date should return empty`() {
        val house = House(0, 1, "Test House", 1, 100, 80.0, "Test desc")
        houseData.createHouse(house)

        val startDate = LocalDate(2026, 6, 20)
        val endDate = LocalDate(2026, 6, 15) // Start > End

        val availableHouses = houseData.getAvailableHouses(startDate, endDate, 0, 10)

        assertEquals(0, availableHouses.size)
        assertTrue(availableHouses.isEmpty())
    }

    @Test
    fun `get available houses should handle multiple bookings on same house`() {
        val house = House(0, 1, "Test House", 1, 100, 80.0, "Test desc")
        val houseId = houseData.createHouse(house)

        val booking1 =
            Booking(
                id = 0,
                userId = 1,
                houseId = houseId,
                startDate = LocalDate(2026, 6, 10),
                endDate = LocalDate(2026, 6, 12),
            )
        val booking2 =
            Booking(
                id = 0,
                userId = 2,
                houseId = houseId,
                startDate = LocalDate(2026, 6, 15),
                endDate = LocalDate(2026, 6, 18),
            )
        bookingData.createBooking(booking1)
        bookingData.createBooking(booking2)

        // Check availability in gap between bookings
        val availableHouses = houseData.getAvailableHouses(LocalDate(2026, 6, 13), LocalDate(2026, 6, 14), 0, 10)

        assertEquals(1, availableHouses.size)
        assertEquals(houseId, availableHouses.first().id)
    }

    @Test
    fun `get available houses should not include house when booking touches search period at start`() {
        val house = House(0, 1, "Test House", 1, 100, 80.0, "Test desc")
        val houseId = houseData.createHouse(house)

        val booking =
            Booking(
                id = 0,
                userId = 1,
                houseId = houseId,
                startDate = LocalDate(2026, 6, 15),
                endDate = LocalDate(2026, 6, 20),
            )
        bookingData.createBooking(booking)

        // Search period ends exactly when booking starts
        val availableHouses = houseData.getAvailableHouses(LocalDate(2026, 6, 10), LocalDate(2026, 6, 15), 0, 10)

        assertEquals(0, availableHouses.size)
    }
}

package pt.isel.ls.services

import kotlinx.datetime.LocalDate
import pt.isel.ls.data.HouseData
import pt.isel.ls.data.StorageDataManager
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.Booking
import pt.isel.ls.domain.House
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationType
import pt.isel.ls.domain.User
import pt.isel.ls.domain.requests.HouseCreationRequest
import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName
import pt.isel.ls.exceptions.BadRequestException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class HouseServiceTest {
    private lateinit var memManager: MemManager
    private lateinit var houseService: HouseService

    @BeforeTest
    fun setup() {
        memManager = MemManager()
        memManager.clear()
        houseService = HouseService(memManager)
    }

    private fun createLocation(): Int {
        val locationInput =
            Location(
                lid = 0,
                name = "Lisboa",
                type = LocationType.DISTRICT,
                parentId = null,
            )
        return memManager.locationData.createLocation(locationInput)
    }

    private fun createUserWithToken(): Pair<Int, String> {
        val uid =
            memManager.userData.createUser(
                User(
                    uid = 0,
                    name = UserName("Alice"),
                    email = Email("alice@isel.pt"),
                    password = Password("secure123"),
                ),
            )
        val tokenObj = memManager.tokenData.createToken(uid)
        return Pair(uid, tokenObj.token.toString())
    }

    @Test
    fun `create house successfully`() {
        val lid = createLocation()
        val (_, token) = createUserWithToken()
        val newId =
            houseService.createHouse(
                request =
                    HouseCreationRequest(
                        title = "Nice House",
                        locationId = lid,
                        areaSqMt = 100,
                        pricePerNight = 80.0,
                        description = "A very nice house",
                    ),
                tokenString = token,
            )

        val savedHouse = memManager.houseData.getHouse(newId)
        assertNotNull(savedHouse)
        assertEquals("Nice House", savedHouse.title)
        assertEquals(lid, savedHouse.locationId)
    }

    @Test
    fun `create house with invalid area throws BadRequestException`() {
        val lid = createLocation()

        assertFailsWith<BadRequestException> {
            houseService.createHouse(
                request =
                    HouseCreationRequest(
                        title = "Tiny House",
                        locationId = lid,
                        areaSqMt = 0,
                        pricePerNight = 50.0,
                        description = "Too small",
                    ),
                tokenString = "valid_token",
            )
        }
    }

    @Test
    fun `get all houses with paging`() {
        val lid = createLocation()
        val (_, token) = createUserWithToken()

        repeat(3) { index ->
            houseService.createHouse(
                request =
                    HouseCreationRequest(
                        title = "House $index",
                        locationId = lid,
                        areaSqMt = 80 + index,
                        pricePerNight = 70.0 + index,
                        description = "House number $index",
                    ),
                tokenString = token,
            )
        }

        val houses = houseService.getAllHouses(0, 10)

        assertEquals(3, houses.size)
        assertEquals(listOf("House 0", "House 1", "House 2"), houses.map { it.title })
    }

    @Test
    fun `get house details uses cache with configured size`() {
        val lid = createLocation()
        val (_, token) = createUserWithToken()
        val houseIds =
            (1..3).map { index ->
                houseService.createHouse(
                    request =
                        HouseCreationRequest(
                            title = "Cached House $index",
                            locationId = lid,
                            areaSqMt = 80 + index,
                            pricePerNight = 70.0 + index,
                            description = "House for cache test $index",
                        ),
                    tokenString = token,
                )
            }

        var getHouseCalls = 0
        val dataManagerWithCounter =
            object : StorageDataManager by memManager {
                override val houseData =
                    object : HouseData by memManager.houseData {
                        override fun getHouse(hid: Int): House? {
                            getHouseCalls += 1
                            return memManager.houseData.getHouse(hid)
                        }
                    }
            }
        val serviceWithCache = HouseService(dataManagerWithCounter, houseDetailsCacheSize = 2)

        serviceWithCache.getHouseDetails(houseIds[0])
        serviceWithCache.getHouseDetails(houseIds[0])
        serviceWithCache.getHouseDetails(houseIds[1])
        serviceWithCache.getHouseDetails(houseIds[2])
        serviceWithCache.getHouseDetails(houseIds[0])

        assertEquals(4, getHouseCalls)
    }

    @Test
    fun `get available houses does not include houses with conflicting bookings`() {
        val lid = createLocation()
        createUserWithToken()

        val house1Id =
            memManager.houseData.createHouse(
                House(
                    id = 0,
                    ownerId = 1,
                    title = "House 1",
                    locationId = lid,
                    areaSqMt = 90,
                    pricePerNight = 60.0,
                    description = "First house",
                ),
            )
        val house2Id =
            memManager.houseData.createHouse(
                House(
                    id = 0,
                    ownerId = 1,
                    title = "House 2",
                    locationId = lid,
                    areaSqMt = 100,
                    pricePerNight = 80.0,
                    description = "Second house",
                ),
            )

        val existingBookingStart = LocalDate.parse("2026-01-10")
        val existingBookingEnd = LocalDate.parse("2026-01-15")

        memManager.bookingData.createBooking(
            Booking(
                id = 0,
                userId = 1,
                houseId = house1Id,
                startDate = existingBookingStart,
                endDate = existingBookingEnd,
            ),
        )

        val searchStart = LocalDate.parse("2026-01-12")
        val searchEnd = LocalDate.parse("2026-01-20")

        val available = houseService.getAvailableHouses(searchStart, searchEnd, 0, 10)

        assertEquals(2, memManager.houseData.getAllHouses(0, 10).size)
        assertEquals(1, available.size)
        assertEquals(house2Id, available.first().id)
    }

    @Test
    fun `search houses requires location and another criterion`() {
        val lid = createLocation()

        assertFailsWith<BadRequestException> {
            houseService.searchHouses(
                locationId = lid,
                minPrice = null,
                maxPrice = null,
                skip = 0,
                limit = 10,
            )
        }
    }

    @Test
    fun `search houses filters by location and price`() {
        val lid = createLocation()
        val (_, token) = createUserWithToken()

        houseService.createHouse(
            request = HouseCreationRequest("Central Flat", lid, 80, 90.0, "Near the river"),
            tokenString = token,
        )
        houseService.createHouse(
            request = HouseCreationRequest("Luxury Villa", lid, 200, 250.0, "With garden"),
            tokenString = token,
        )

        val houses =
            houseService.searchHouses(
                locationId = lid,
                minPrice = 50.0,
                maxPrice = 100.0,
                skip = 0,
                limit = 10,
            )

        assertEquals(1, houses.size)
        assertEquals("Central Flat", houses.first().title)
    }

    @Test
    fun `available days excludes booked days`() {
        val lid = createLocation()
        val (_, token) = createUserWithToken()
        val hid =
            houseService.createHouse(
                request = HouseCreationRequest("Calendar House", lid, 100, 100.0, "Calendar test"),
                tokenString = token,
            )

        memManager.bookingData.createBooking(
            Booking(
                id = 0,
                userId = 1,
                houseId = hid,
                startDate = LocalDate(2026, 6, 10),
                endDate = LocalDate(2026, 6, 12),
            ),
        )

        val availableDays = houseService.getAvailableDays(hid, 2026, 6)

        assertEquals(false, LocalDate(2026, 6, 10) in availableDays)
        assertEquals(false, LocalDate(2026, 6, 11) in availableDays)
        assertEquals(false, LocalDate(2026, 6, 12) in availableDays)
        assertEquals(true, LocalDate(2026, 6, 13) in availableDays)
    }
}

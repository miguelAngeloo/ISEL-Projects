package pt.isel.ls.services

import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationType
import pt.isel.ls.domain.User
import pt.isel.ls.domain.dto.CreateLocationInput
import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName
import pt.isel.ls.exceptions.NotFoundException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class LocationServiceTest {
    private lateinit var memManager: MemManager
    private lateinit var locationService: LocationService

    @BeforeTest
    fun setup() {
        memManager = MemManager()
        memManager.clear()
        locationService = LocationService(dataManager = memManager)
    }

    private fun createTestLocation(
        name: String = "Portugal",
        type: LocationType = LocationType.COUNTRY,
        parentId: Int? = null,
    ): Int = memManager.locationData.createLocation(Location(0, name, type, parentId))

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
    fun `getLocationDetails returns correct location`() {
        val lid = createTestLocation("Portugal", LocationType.COUNTRY)

        val location = locationService.getLocationDetails(lid)

        assertNotNull(location)
        assertEquals(lid, location.lid)
        assertEquals("Portugal", location.name)
        assertEquals(LocationType.COUNTRY, location.type)
    }

    @Test
    fun `getLocationDetails throws NotFoundException when location does not exist`() {
        assertFailsWith<NotFoundException> {
            locationService.getLocationDetails(999)
        }
    }

    @Test
    fun `getChildrenLocations returns only direct children`() {
        val portugal = createTestLocation("Portugal", LocationType.COUNTRY)
        val lisboa = createTestLocation("Lisboa", LocationType.DISTRICT, portugal)
        createTestLocation("Porto", LocationType.DISTRICT, portugal)
        createTestLocation("Oeiras", LocationType.MUNICIPALITY, lisboa)

        val output = locationService.getChildrenLocations(portugal)

        assertEquals(2, output.locations.size)
        assertEquals("Lisboa", output.locations[0].name)
        assertEquals("Porto", output.locations[1].name)
    }

    @Test
    fun `getChildrenLocations throws NotFoundException when location does not exist`() {
        assertFailsWith<NotFoundException> {
            locationService.getChildrenLocations(999)
        }
    }

    @Test
    fun `getFullHierarchicalPath returns correct order from root to location`() {
        val portugal = createTestLocation("Portugal", LocationType.COUNTRY)
        val lisboa = createTestLocation("Lisboa", LocationType.DISTRICT, portugal)
        val oeiras = createTestLocation("Oeiras", LocationType.MUNICIPALITY, lisboa)

        val path = locationService.getFullHierarchicalPath(oeiras)

        assertEquals(3, path.path.size)
        assertEquals("Portugal", path.path[0].name)
        assertEquals("Lisboa", path.path[1].name)
        assertEquals("Oeiras", path.path[2].name)
    }

    @Test
    fun `getFullHierarchicalPath throws NotFoundException when location does not exist`() {
        assertFailsWith<NotFoundException> {
            locationService.getFullHierarchicalPath(999)
        }
    }

    @Test
    fun `createNewLocation stores location and returns valid ID`() {
        val input = CreateLocationInput("Portugal", LocationType.COUNTRY, null)
        val (_, token) = createUserWithToken()

        val location =
            Location(
                lid = 0,
                name = input.name,
                type = input.type,
                parentId = input.parentId,
            )

        val lid = locationService.createNewLocation(location, token)

        val stored = memManager.locationData.getLocation(lid)
        assertNotNull(stored)
        assertEquals("Portugal", stored.name)
    }

    @Test
    fun `getLocations returns correct page`() {
        repeat(5) { i ->
            createTestLocation("Location $i", LocationType.LOCALITY)
        }

        val locations = locationService.getLocations(skip = 1, limit = 2)

        assertEquals(2, locations.size)
        assertEquals("Location 1", locations[0].name)
        assertEquals("Location 2", locations[1].name)
    }
}

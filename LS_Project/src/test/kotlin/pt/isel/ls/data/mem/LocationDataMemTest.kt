package pt.isel.ls.data.mem

import org.junit.jupiter.api.Assertions.assertEquals
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationType
import pt.isel.ls.domain.dto.CreateLocationInput
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LocationDataMemTest {
    private lateinit var locationDataMem: LocationDataMem

    @BeforeTest
    fun setup() {
        val memStorage = MemStorage()
        memStorage.clear()
        locationDataMem = LocationDataMem()
    }

    @Test
    fun `createLocation assigns auto-incremented ID and stores location`() {
        val input = CreateLocationInput("Portugal", LocationType.COUNTRY, null)

        val location =
            Location(
                lid = 0,
                name = input.name,
                type = input.type,
                parentId = input.parentId,
            )

        val newLid = locationDataMem.createLocation(location)

        assertEquals(1, newLid)

        val stored = locationDataMem.getLocation(newLid)
        assertNotNull(stored)
        assertEquals(newLid, stored.lid)
        assertEquals("Portugal", stored.name)
        assertEquals(LocationType.COUNTRY, stored.type)
        assertNull(stored.parentId)
    }

    @Test
    fun `createLocation assigns sequential IDs for multiple locations`() {
        val lid1 = locationDataMem.createLocation(Location(0, "Portugal", LocationType.COUNTRY, null))
        val lid2 = locationDataMem.createLocation(Location(0, "Lisboa", LocationType.DISTRICT, lid1))

        assertEquals(1, lid1)
        assertEquals(2, lid2)
    }

    @Test
    fun `getLocation returns correct location by ID`() {
        val lid = locationDataMem.createLocation(Location(0, "Portugal", LocationType.COUNTRY, null))

        val retrieved = locationDataMem.getLocation(lid)

        assertNotNull(retrieved)
        assertEquals(lid, retrieved.lid)
        assertEquals("Portugal", retrieved.name)
    }

    @Test
    fun `getLocation returns null when location does not exist`() {
        val retrieved = locationDataMem.getLocation(999)
        assertNull(retrieved)
    }

    @Test
    fun `getChildLocations returns direct children only`() {
        val portugal = locationDataMem.createLocation(Location(0, "Portugal", LocationType.COUNTRY, null))
        val lisboa = locationDataMem.createLocation(Location(0, "Lisboa", LocationType.DISTRICT, portugal))
        val porto = locationDataMem.createLocation(Location(0, "Porto", LocationType.DISTRICT, portugal))
        val oeiras = locationDataMem.createLocation(Location(0, "Oeiras", LocationType.MUNICIPALITY, lisboa))

        val children = locationDataMem.getChildLocations(locationDataMem.getLocation(portugal)!!)

        assertEquals(2, children.size)
        assertEquals(lisboa, children[0].lid)
        assertEquals(porto, children[1].lid)
    }

    @Test
    fun `getChildLocations returns empty list when location has no children`() {
        val lid = locationDataMem.createLocation(Location(0, "Paço de Arcos", LocationType.LOCALITY, null))

        val children = locationDataMem.getChildLocations(locationDataMem.getLocation(lid)!!)

        assertEquals(0, children.size)
    }

    @Test
    fun `getLocationPath returns full path from root to location`() {
        val portugal = locationDataMem.createLocation(Location(0, "Portugal", LocationType.COUNTRY, null))
        val lisboa = locationDataMem.createLocation(Location(0, "Lisboa", LocationType.DISTRICT, portugal))
        val oeiras = locationDataMem.createLocation(Location(0, "Oeiras", LocationType.MUNICIPALITY, lisboa))

        val path = locationDataMem.getLocationPath(oeiras)

        assertEquals(3, path.size)
        assertEquals("Portugal", path[0].name)
        assertEquals("Lisboa", path[1].name)
        assertEquals("Oeiras", path[2].name)
    }

    @Test
    fun `getLocationPath returns single element for root location`() {
        val portugal = locationDataMem.createLocation(Location(0, "Portugal", LocationType.COUNTRY, null))

        val path = locationDataMem.getLocationPath(portugal)

        assertEquals(1, path.size)
        assertEquals("Portugal", path[0].name)
    }

    @Test
    fun `getLocations returns correct page with skip and limit`() {
        locationDataMem.createLocation(Location(0, "Portugal", LocationType.COUNTRY, null))
        locationDataMem.createLocation(Location(0, "Lisboa", LocationType.DISTRICT, 1))
        locationDataMem.createLocation(Location(0, "Oeiras", LocationType.MUNICIPALITY, 2))
        locationDataMem.createLocation(Location(0, "Paço de Arcos", LocationType.LOCALITY, 3))
        locationDataMem.createLocation(Location(0, "Porto", LocationType.DISTRICT, 1))

        val page = locationDataMem.getLocations(skip = 1, limit = 2)

        assertEquals(2, page.size)
        assertEquals("Lisboa", page[0].name)
        assertEquals("Oeiras", page[1].name)
    }

    @Test
    fun `getLocations returns empty list when skip exceeds total`() {
        locationDataMem.createLocation(Location(0, "Portugal", LocationType.COUNTRY, null))

        val page = locationDataMem.getLocations(skip = 10, limit = 5)

        assertEquals(0, page.size)
    }
}

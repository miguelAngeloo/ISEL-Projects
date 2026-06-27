package pt.isel.ls.data.jdbc

import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationType
import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocationDataJDBCTest {
    private val dbManager = DBManager()

    private fun getConnection(): Connection {
        val connection = dbManager.getConnection()
        connection.autoCommit = false
        return connection
    }

    @Test
    fun `can create and retrieve a location`() {
        val connection = getConnection()
        try {
            val locationData = LocationDataJDBC(dbManager, connection)
            val location = Location(lid = 0, name = "Portugal", type = LocationType.COUNTRY, parentId = null)

            val generatedId = locationData.createLocation(location)
            val retrieved = locationData.getLocation(generatedId)

            assertNotNull(retrieved)
            assertEquals("Portugal", retrieved.name)
            assertEquals(LocationType.COUNTRY, retrieved.type)
            assertNull(retrieved.parentId)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `create location returns a valid generated id`() {
        val connection = getConnection()
        try {
            val locationData = LocationDataJDBC(dbManager, connection)
            val location = Location(lid = 0, name = "Portugal", type = LocationType.COUNTRY, parentId = null)

            val id = locationData.createLocation(location)

            assertTrue(id > 0, "O ID gerado deve ser superior a zero")
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `returns null for non-existing location`() {
        val connection = getConnection()
        try {
            val locationData = LocationDataJDBC(dbManager, connection)

            val retrieved = locationData.getLocation(99999)

            assertNull(retrieved)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `can create location with parent`() {
        val connection = getConnection()
        try {
            val locationData = LocationDataJDBC(dbManager, connection)
            val portugal = locationData.createLocation(Location(0, "Portugal", LocationType.COUNTRY, null))
            val lisboa = locationData.createLocation(Location(0, "Lisboa", LocationType.DISTRICT, portugal))

            val retrieved = locationData.getLocation(lisboa)

            assertNotNull(retrieved)
            assertEquals("Lisboa", retrieved.name)
            assertEquals(portugal, retrieved.parentId)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `getChildLocations returns only direct children`() {
        val connection = getConnection()
        try {
            val locationData = LocationDataJDBC(dbManager, connection)
            val portugal = locationData.createLocation(Location(0, "Portugal", LocationType.COUNTRY, null))
            locationData.createLocation(Location(0, "Lisboa", LocationType.DISTRICT, portugal))
            locationData.createLocation(Location(0, "Porto", LocationType.DISTRICT, portugal))
            val lisboa =
                locationData.getLocation(
                    locationData.createLocation(Location(0, "Lisboa", LocationType.DISTRICT, portugal)),
                )!!
            locationData.createLocation(Location(0, "Oeiras", LocationType.MUNICIPALITY, lisboa.lid)) // neto

            val children = locationData.getChildLocations(locationData.getLocation(portugal)!!)

            assertEquals(3, children.size)
            assertTrue(children.all { it.parentId == portugal })
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `getLocationPath returns correct hierarchy from root to location`() {
        val connection = getConnection()
        try {
            val locationData = LocationDataJDBC(dbManager, connection)
            val portugal = locationData.createLocation(Location(0, "Portugal", LocationType.COUNTRY, null))
            val lisboa = locationData.createLocation(Location(0, "Lisboa", LocationType.DISTRICT, portugal))
            val oeiras = locationData.createLocation(Location(0, "Oeiras", LocationType.MUNICIPALITY, lisboa))

            val path = locationData.getLocationPath(oeiras)

            assertEquals(3, path.size)
            assertEquals("Portugal", path[0].name)
            assertEquals("Lisboa", path[1].name)
            assertEquals("Oeiras", path[2].name)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `getLocations returns correct page with skip and limit`() {
        val connection = getConnection()
        try {
            val locationData = LocationDataJDBC(dbManager, connection)
            repeat(5) { i ->
                locationData.createLocation(Location(0, "Location $i", LocationType.LOCALITY, null))
            }

            val page = locationData.getLocations(skip = 1, limit = 2)

            assertEquals(2, page.size)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `getLocations returns empty list when skip exceeds total`() {
        val connection = getConnection()
        try {
            val locationData = LocationDataJDBC(dbManager, connection)
            locationData.createLocation(Location(0, "Portugal", LocationType.COUNTRY, null))

            val page = locationData.getLocations(skip = 10, limit = 5)

            assertEquals(0, page.size)
        } finally {
            connection.rollback()
            connection.close()
        }
    }
}

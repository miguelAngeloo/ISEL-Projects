package pt.isel.ls.data.jdbc

import kotlinx.datetime.LocalDate
import pt.isel.ls.domain.House
import java.sql.Connection
import java.sql.Statement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HouseDataJDBCTest {
    private val dbManager = DBManager()

    private fun getConnection(): Connection {
        val connection = dbManager.getConnection()
        connection.autoCommit = false
        return connection
    }

    private fun createDummyLocation(connection: Connection): Int {
        var locationId = -1
        val sql = "INSERT INTO locations (name, type) VALUES (?, 'LOCALITY')"
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { st ->
            st.setString(1, "Test Location")
            st.executeUpdate()
            val rs = st.generatedKeys
            if (rs.next()) locationId = rs.getInt(1)
        }
        return locationId
    }

    private fun createDummyUser(
        connection: Connection,
        email: String = "u@u.pt",
    ): Int {
        var userId = -1
        val userSql = "INSERT INTO users (name, email, password) VALUES ('U', ?, 'p')"
        connection.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS).use { st ->
            st.setString(1, email)
            st.executeUpdate()
            val rs = st.generatedKeys
            if (rs.next()) userId = rs.getInt(1)
        }
        return userId
    }

    @Test
    fun `can create and retrieve a house`() {
        val connection = getConnection()
        try {
            val houseData = HouseDataJDBC(dbManager, connection)
            val locationId = createDummyLocation(connection)
            val ownerId = createDummyUser(connection)
            val house =
                House(
                    id = 0,
                    ownerId = ownerId,
                    title = "Test House",
                    locationId = locationId,
                    areaSqMt = 100,
                    pricePerNight = 50.0,
                    description = "A beautiful test house",
                )

            val generatedId = houseData.createHouse(house)
            val retrievedHouse = houseData.getHouse(generatedId)

            assertNotNull(retrievedHouse)
            assertEquals("Test House", retrievedHouse.title)
            assertEquals(locationId, retrievedHouse.locationId)
            assertEquals(100, retrievedHouse.areaSqMt)
            assertEquals(50.0, retrievedHouse.pricePerNight)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `returns null for non-existing house`() {
        val connection = getConnection()
        try {
            val houseData = HouseDataJDBC(dbManager, connection)
            val house = houseData.getHouse(99999)
            assertNull(house)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `can get all houses`() {
        val connection = getConnection()
        try {
            val houseData = HouseDataJDBC(dbManager, connection)
            val locationId = createDummyLocation(connection)
            val ownerId = createDummyUser(connection)

            val h1 = House(0, ownerId, "H1", locationId, 50, 30.0, "D1")
            val h2 = House(0, ownerId, "H2", locationId, 60, 40.0, "D2")

            houseData.createHouse(h1)
            houseData.createHouse(h2)

            val allHouses = houseData.getAllHouses(0, 200)
            assertTrue(allHouses.size >= 2)
            assertTrue(allHouses.any { it.title == "H1" })
            assertTrue(allHouses.any { it.title == "H2" })
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `can get houses by location`() {
        val connection = getConnection()
        try {
            val houseData = HouseDataJDBC(dbManager, connection)
            val loc1 = createDummyLocation(connection)
            val loc2 = createDummyLocation(connection)
            val ownerId = createDummyUser(connection)

            val h1 = House(0, ownerId, "H1", loc1, 50, 30.0, "D1")
            val h2 = House(0, ownerId, "H2", loc2, 60, 40.0, "D2")

            houseData.createHouse(h1)
            houseData.createHouse(h2)

            val housesInLoc1 = houseData.getHousesByLocation(loc1, 0, 10)
            assertEquals(1, housesInLoc1.size)
            assertEquals("H1", housesInLoc1[0].title)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `can get available houses`() {
        val connection = getConnection()
        try {
            val houseData = HouseDataJDBC(dbManager, connection)
            val locationId = createDummyLocation(connection)
            val ownerId = createDummyUser(connection, "owner@u.pt")
            val h1 = House(0, ownerId, "Available House", locationId, 100, 100.0, "Desc")
            val hid = houseData.createHouse(h1)

            val startDate = LocalDate(2023, 1, 1)
            val endDate = LocalDate(2023, 1, 10)

            val available = houseData.getAvailableHouses(startDate, endDate, 0, 200)
            assertTrue(available.any { it.id == hid })

            val userId = createDummyUser(connection, "booker@u.pt")

            val bookingSql = "INSERT INTO booking (user_id, house_id, start_date, end_date) VALUES (?, ?, ?, ?)"
            connection.prepareStatement(bookingSql).use { st ->
                st.setInt(1, userId)
                st.setInt(2, hid)
                st.setObject(3, java.sql.Date.valueOf("2023-01-05"))
                st.setObject(4, java.sql.Date.valueOf("2023-01-07"))
                st.executeUpdate()
            }

            val availableAfterBooking = houseData.getAvailableHouses(startDate, endDate, 0, 10)
            assertTrue(availableAfterBooking.none { it.id == hid })

            val availableOtherDates = houseData.getAvailableHouses(LocalDate(2023, 1, 11), LocalDate(2023, 1, 15), 0, 200)
            assertTrue(availableOtherDates.any { it.id == hid })
        } finally {
            connection.rollback()
            connection.close()
        }
    }
}

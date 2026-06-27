package pt.isel.ls.data.jdbc

import kotlinx.datetime.LocalDate
import pt.isel.ls.domain.Booking
import java.sql.Connection
import java.sql.Statement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class BookingDataJDBCTest {
    private val dbManager = DBManager()

    private fun getConnection(): Connection {
        val connection = dbManager.getConnection()
        connection.autoCommit = false
        return connection
    }

    private fun createDummyUser(connection: Connection): Int {
        var userId = -1
        val sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)"
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { st ->
            st.setString(1, "Booking Test User")
            st.setString(2, "booking_user_${Uuid.random()}@isel.pt")
            st.setString(3, "password")
            st.executeUpdate()
            val rs = st.generatedKeys
            if (rs.next()) userId = rs.getInt(1)
        }
        return userId
    }

    private fun createDummyHouse(
        connection: Connection,
        ownerId: Int,
    ): Int {
        // First need a location
        var locationId = -1
        val locSql = "INSERT INTO locations (name, type) VALUES ('Booking Loc', 'LOCALITY')"
        connection.prepareStatement(locSql, Statement.RETURN_GENERATED_KEYS).use { st ->
            st.executeUpdate()
            val rs = st.generatedKeys
            if (rs.next()) locationId = rs.getInt(1)
        }

        var houseId = -1
        val sql =
            "INSERT INTO houses (owner_id, title, location_id, area_sqm, price_per_night, description) VALUES (?, ?, ?, ?, ?, ?)"
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { st ->
            st.setInt(1, ownerId)
            st.setString(2, "Booking Test House")
            st.setInt(3, locationId)
            st.setInt(4, 80)
            st.setDouble(5, 60.0)
            st.setString(6, "Desc")
            st.executeUpdate()
            val rs = st.generatedKeys
            if (rs.next()) houseId = rs.getInt(1)
        }
        return houseId
    }

    @Test
    fun `can create and retrieve a booking`() {
        val connection = getConnection()
        try {
            val bookingData = BookingDataJDBC(dbManager, connection)
            val userId = createDummyUser(connection)
            val houseId = createDummyHouse(connection, userId)

            val booking =
                Booking(
                    id = 0,
                    userId = userId,
                    houseId = houseId,
                    startDate = LocalDate(2023, 5, 1),
                    endDate = LocalDate(2023, 5, 5),
                )

            val generatedId = bookingData.createBooking(booking)
            val retrieved = bookingData.getBooking(generatedId)

            assertNotNull(retrieved)
            assertEquals(userId, retrieved.userId)
            assertEquals(houseId, retrieved.houseId)
            assertEquals(LocalDate(2023, 5, 1), retrieved.startDate)
            assertEquals(LocalDate(2023, 5, 5), retrieved.endDate)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `returns null for non-existing booking`() {
        val connection = getConnection()
        try {
            val bookingData = BookingDataJDBC(dbManager, connection)
            assertNull(bookingData.getBooking(99999))
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `can get bookings by house`() {
        val connection = getConnection()
        try {
            val bookingData = BookingDataJDBC(dbManager, connection)
            val userId = createDummyUser(connection)
            val h1 = createDummyHouse(connection, userId)
            val h2 = createDummyHouse(connection, userId)

            val b1 = Booking(0, userId, h1, LocalDate(2023, 1, 1), LocalDate(2023, 1, 5))
            val b2 = Booking(0, userId, h2, LocalDate(2023, 1, 1), LocalDate(2023, 1, 5))

            bookingData.createBooking(b1)
            bookingData.createBooking(b2)

            val house1Bookings = bookingData.getBookingsByHouse(h1, 0, 10)
            assertEquals(1, house1Bookings.size)
            assertTrue(house1Bookings.any { it.houseId == h1 })
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `can get bookings by date`() {
        val connection = getConnection()
        try {
            val bookingData = BookingDataJDBC(dbManager, connection)
            val userId = createDummyUser(connection)
            val hid = createDummyHouse(connection, userId)

            val b1 = Booking(0, userId, hid, LocalDate(2023, 6, 1), LocalDate(2023, 6, 10))
            bookingData.createBooking(b1)

            val bookingsOn6th = bookingData.getBookingsByDate(LocalDate(2023, 6, 6), 0, 10)
            assertTrue(bookingsOn6th.any { it.houseId == hid })

            val bookingsOn11th = bookingData.getBookingsByDate(LocalDate(2023, 6, 11), 0, 10)
            assertTrue(bookingsOn11th.none { it.houseId == hid })
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `can get conflicting bookings`() {
        val connection = getConnection()
        try {
            val bookingData = BookingDataJDBC(dbManager, connection)
            val userId = createDummyUser(connection)
            val hid = createDummyHouse(connection, userId)

            val existing = Booking(0, userId, hid, LocalDate(2023, 7, 10), LocalDate(2023, 7, 20))
            bookingData.createBooking(existing)

            // Overlap at start
            val conflict1 = bookingData.getConflictingBookings(hid, LocalDate(2023, 7, 5), LocalDate(2023, 7, 15), 0, 10)
            assertEquals(1, conflict1.size)

            // Overlap at end
            val conflict2 = bookingData.getConflictingBookings(hid, LocalDate(2023, 7, 15), LocalDate(2023, 7, 25), 0, 10)
            assertEquals(1, conflict2.size)

            // Completely inside
            val conflict3 = bookingData.getConflictingBookings(hid, LocalDate(2023, 7, 12), LocalDate(2023, 7, 18), 0, 10)
            assertEquals(1, conflict3.size)

            // Completely surrounding
            val conflict4 = bookingData.getConflictingBookings(hid, LocalDate(2023, 7, 5), LocalDate(2023, 7, 25), 0, 10)
            assertEquals(1, conflict4.size)

            // No conflict
            val noConflict = bookingData.getConflictingBookings(hid, LocalDate(2023, 7, 21), LocalDate(2023, 7, 30), 0, 10)
            assertEquals(0, noConflict.size)
        } finally {
            connection.rollback()
            connection.close()
        }
    }

    @Test
    fun `can get all bookings`() {
        val connection = getConnection()
        try {
            val bookingData = BookingDataJDBC(dbManager, connection)
            val userId = createDummyUser(connection)
            val hid = createDummyHouse(connection, userId)

            val b1 = Booking(0, userId, hid, LocalDate(2023, 8, 1), LocalDate(2023, 8, 5))
            bookingData.createBooking(b1)

            val all = bookingData.getAllBookings(0, 10)
            assertTrue(all.isNotEmpty())
            assertTrue(all.any { it.houseId == hid && it.userId == userId })
        } finally {
            connection.rollback()
            connection.close()
        }
    }
}

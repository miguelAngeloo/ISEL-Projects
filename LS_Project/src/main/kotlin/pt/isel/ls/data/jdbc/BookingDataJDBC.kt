package pt.isel.ls.data.jdbc

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import pt.isel.ls.data.BookingData
import pt.isel.ls.domain.Booking
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class BookingDataJDBC(
    private val dbManager: DBManager,
    private val connection: Connection? = null,
) : BookingData {
    override fun createBooking(booking: Booking): Int =
        dbManager.execute(connection) { conn ->
            val sql = "INSERT INTO booking (user_id, house_id, start_date, end_date) VALUES (?, ?, ?, ?)"
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.setInt(1, booking.userId)
                statement.setInt(2, booking.houseId)
                statement.setObject(3, booking.startDate.toJavaLocalDate())
                statement.setObject(4, booking.endDate.toJavaLocalDate())

                statement.executeUpdate()

                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    generatedKeys.getInt(1)
                } else {
                    throw IllegalStateException("No Booking ID obtained.")
                }
            }
        }

    override fun getBooking(bid: Int): Booking? =
        dbManager.execute(connection) { conn ->
            val sql = "SELECT id, user_id, house_id, start_date, end_date FROM booking WHERE id = ?"
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, bid)
                val rs = statement.executeQuery()
                if (rs.next()) {
                    rs.toBooking()
                } else {
                    null
                }
            }
        }

    override fun getBookingsByHouse(
        hid: Int,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        dbManager.execute(connection) { conn ->
            val sql =
                "SELECT id, user_id, house_id, start_date, end_date FROM booking WHERE house_id = ? ORDER BY id LIMIT ? OFFSET ?"
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, hid)
                statement.setInt(2, limit)
                statement.setInt(3, skip)
                statement.executeQuery().toBookingList()
            }
        }

    override fun getBookingsByDate(
        date: LocalDate,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        dbManager.execute(connection) { conn ->
            val sql =
                """
                SELECT id, user_id, house_id, start_date, end_date
                FROM booking
                WHERE start_date <= ? AND end_date >= ?
                ORDER BY id LIMIT ? OFFSET ?
                """.trimIndent()
            conn.prepareStatement(sql).use { statement ->
                val javaDate = date.toJavaLocalDate()
                statement.setObject(1, javaDate)
                statement.setObject(2, javaDate)
                statement.setInt(3, limit)
                statement.setInt(4, skip)
                statement.executeQuery().toBookingList()
            }
        }

    override fun getBookingsByUser(
        uid: Int,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        dbManager.execute(connection) { conn ->
            val sql =
                "SELECT id, user_id, house_id, start_date, end_date FROM booking WHERE user_id = ? ORDER BY id LIMIT ? OFFSET ?"
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, uid)
                statement.setInt(2, limit)
                statement.setInt(3, skip)
                statement.executeQuery().toBookingList()
            }
        }

    override fun getAllBookings(
        skip: Int,
        limit: Int,
    ): List<Booking> =
        dbManager.execute(connection) { conn ->
            val sql = "SELECT id, user_id, house_id, start_date, end_date FROM booking ORDER BY id LIMIT ? OFFSET ?"
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, limit)
                statement.setInt(2, skip)
                statement.executeQuery().toBookingList()
            }
        }

    override fun getConflictingBookings(
        hid: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        dbManager.execute(connection) { conn ->
            val sql =
                """
                SELECT id, user_id, house_id, start_date, end_date
                FROM booking
                WHERE house_id = ? AND start_date <= ? AND end_date >= ?
                ORDER BY id LIMIT ? OFFSET ?
                """.trimIndent()
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, hid)
                statement.setObject(2, endDate.toJavaLocalDate())
                statement.setObject(3, startDate.toJavaLocalDate())
                statement.setInt(4, limit)
                statement.setInt(5, skip)
                statement.executeQuery().toBookingList()
            }
        }

    override fun deleteBooking(bid: Int): Boolean =
        dbManager.execute(connection) { conn ->
            val sql = "DELETE FROM booking WHERE id = ?"
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, bid)
                val rowsAffected = statement.executeUpdate()
                rowsAffected > 0
            }
        }

    override fun updateBooking(booking: Booking): Boolean =
        dbManager.execute(connection) { conn ->
            val sql = "UPDATE booking SET user_id = ?, house_id = ?, start_date = ?, end_date = ? WHERE id = ?"
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, booking.userId)
                statement.setInt(2, booking.houseId)
                statement.setObject(3, booking.startDate.toJavaLocalDate())
                statement.setObject(4, booking.endDate.toJavaLocalDate())
                statement.setInt(5, booking.id)
                val rowsAffected = statement.executeUpdate()
                rowsAffected > 0
            }
        }

    private fun ResultSet.toBookingList(): List<Booking> {
        val bookings = mutableListOf<Booking>()
        while (next()) {
            bookings.add(toBooking())
        }
        return bookings
    }

    private fun ResultSet.toBooking() =
        Booking(
            id = getInt("id"),
            userId = getInt("user_id"),
            houseId = getInt("house_id"),
            startDate = getObject("start_date", java.time.LocalDate::class.java).toKotlinLocalDate(),
            endDate = getObject("end_date", java.time.LocalDate::class.java).toKotlinLocalDate(),
        )
}

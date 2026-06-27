package pt.isel.ls.data.jdbc

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import pt.isel.ls.data.HouseData
import pt.isel.ls.domain.House
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class HouseDataJDBC(
    private val dbManager: DBManager,
    private val connection: Connection? = null,
) : HouseData {
    override fun createHouse(house: House): Int =
        dbManager.execute(connection) { conn ->
            val sql =
                "INSERT INTO houses (owner_id, title, location_id, area_sqm, price_per_night, description) VALUES (?, ?, ?, ?, ?, ?)"
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.setInt(1, house.ownerId)
                statement.setString(2, house.title)
                statement.setInt(3, house.locationId)
                statement.setInt(4, house.areaSqMt)
                statement.setDouble(5, house.pricePerNight)
                statement.setString(6, house.description)

                statement.executeUpdate()

                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    generatedKeys.getInt(1)
                } else {
                    throw IllegalStateException("No House ID obtained.")
                }
            }
        }

    override fun getHouse(hid: Int): House? =
        dbManager.execute(connection) { conn ->
            val sql = "SELECT id, owner_id, title, location_id, area_sqm, price_per_night, description FROM houses WHERE id = ?"
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, hid)
                val rs = statement.executeQuery()
                if (rs.next()) {
                    rs.toHouse()
                } else {
                    null
                }
            }
        }

    override fun getAllHouses(
        skip: Int,
        limit: Int,
    ): List<House> =
        dbManager.execute(connection) { conn ->
            val sql =
                "SELECT id, owner_id, title, location_id, area_sqm, price_per_night, description FROM houses ORDER BY id OFFSET ? LIMIT ?"
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, skip)
                statement.setInt(2, limit)
                val rs = statement.executeQuery()
                val houses = mutableListOf<House>()
                while (rs.next()) {
                    houses.add(rs.toHouse())
                }
                houses
            }
        }

    override fun countHouses(): Int =
        dbManager.execute(connection) { conn ->
            val sql = "SELECT COUNT(*) AS total FROM houses"
            conn.prepareStatement(sql).use { statement ->
                val rs = statement.executeQuery()
                rs.next()
                rs.getInt("total")
            }
        }

    override fun getAvailableHouses(
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<House> =
        dbManager.execute(connection) { conn ->
            val sql =
                """
                SELECT h.id, h.owner_id, h.title, h.location_id, h.area_sqm, h.price_per_night, h.description 
                FROM houses h
                WHERE h.id NOT IN (
                    SELECT b.house_id 
                    FROM booking b 
                    WHERE b.start_date <= ? AND b.end_date >= ?
                )
                ORDER BY h.id OFFSET ? LIMIT ?
                """.trimIndent()
            conn.prepareStatement(sql).use { statement ->
                statement.setObject(1, endDate.toJavaLocalDate())
                statement.setObject(2, startDate.toJavaLocalDate())
                statement.setInt(3, skip)
                statement.setInt(4, limit)
                val rs = statement.executeQuery()
                val houses = mutableListOf<House>()
                while (rs.next()) {
                    houses.add(rs.toHouse())
                }
                houses
            }
        }

    override fun getAvailableHousesByLocation(
        locationId: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<House> =
        dbManager.execute(connection) { conn ->
            val sql =
                """
                SELECT h.id, h.owner_id, h.title, h.location_id, h.area_sqm, h.price_per_night, h.description 
                FROM houses h
                WHERE h.location_id = ?
                  AND h.id NOT IN (
                    SELECT b.house_id 
                    FROM booking b 
                    WHERE b.start_date <= ? AND b.end_date >= ?
                )
                ORDER BY h.id OFFSET ? LIMIT ?
                """.trimIndent()
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, locationId)
                statement.setObject(2, endDate.toJavaLocalDate())
                statement.setObject(3, startDate.toJavaLocalDate())
                statement.setInt(4, skip)
                statement.setInt(5, limit)
                val rs = statement.executeQuery()
                val houses = mutableListOf<House>()
                while (rs.next()) {
                    houses.add(rs.toHouse())
                }
                houses
            }
        }

    override fun getHousesByLocation(
        locationId: Int,
        skip: Int,
        limit: Int,
    ): List<House> =
        dbManager.execute(connection) { conn ->
            val sql =
                "SELECT id, owner_id, title, location_id, area_sqm, price_per_night, description" +
                    " FROM houses WHERE location_id = ? ORDER BY id OFFSET ? LIMIT ?"
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, locationId)
                statement.setInt(2, skip)
                statement.setInt(3, limit)
                val rs = statement.executeQuery()
                val houses = mutableListOf<House>()
                while (rs.next()) {
                    houses.add(rs.toHouse())
                }
                houses
            }
        }

    override fun searchHouses(
        locationId: Int,
        minPrice: Double?,
        maxPrice: Double?,
        skip: Int,
        limit: Int,
    ): List<House> =
        dbManager.execute(connection) { conn ->
            val priceFilter =
                when {
                    minPrice != null && maxPrice != null -> "AND h.price_per_night BETWEEN ? AND ?"
                    minPrice != null -> "AND h.price_per_night >= ?"
                    maxPrice != null -> "AND h.price_per_night <= ?"
                    else -> ""
                }

            val sql =
                """
                SELECT h.id, h.owner_id, h.title, h.location_id, h.area_sqm, h.price_per_night, h.description
                FROM houses h
                WHERE h.location_id = ?
                $priceFilter
                ORDER BY h.id OFFSET ? LIMIT ?
                """.trimIndent()

            conn.prepareStatement(sql).use { statement ->
                var paramIndex = 1
                statement.setInt(paramIndex++, locationId)

                if (minPrice != null) {
                    statement.setDouble(paramIndex++, minPrice)
                }
                if (maxPrice != null) {
                    statement.setDouble(paramIndex++, maxPrice)
                }

                statement.setInt(paramIndex++, skip)
                statement.setInt(paramIndex, limit)

                val rs = statement.executeQuery()
                val houses = mutableListOf<House>()
                while (rs.next()) {
                    houses.add(rs.toHouse())
                }
                houses
            }
        }

    private fun ResultSet.toHouse() =
        House(
            id = getInt("id"),
            ownerId = getInt("owner_id"),
            title = getString("title"),
            locationId = getInt("location_id"),
            areaSqMt = getInt("area_sqm"),
            pricePerNight = getDouble("price_per_night"),
            description = getString("description"),
        )
}

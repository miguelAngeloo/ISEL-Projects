package pt.isel.ls.data.jdbc

import pt.isel.ls.data.LocationData
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationType
import java.sql.Connection
import java.sql.Statement
import java.sql.Types

class LocationDataJDBC(
    private val dbManager: DBManager,
    private val connection: Connection? = null,
) : LocationData {
    override fun createLocation(location: Location): Int =
        dbManager.execute(connection) { conn ->
            val sql = "INSERT INTO locations (name, type, parent_id) VALUES (?, ?::location_type, ?)"
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.setString(1, location.name)
                statement.setString(2, location.type.name)
                if (location.parentId != null) {
                    statement.setInt(3, location.parentId)
                } else {
                    statement.setNull(3, Types.INTEGER)
                }

                statement.executeUpdate()

                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    generatedKeys.getInt(1)
                } else {
                    throw IllegalStateException("No Location ID obtained.")
                }
            }
        }

    override fun getLocation(lid: Int): Location? =
        dbManager.execute(connection) { conn ->
            conn.prepareStatement(
                "SELECT id, name, type, parent_id FROM locations WHERE id = ?",
            ).use { statement ->
                statement.setInt(1, lid)
                val rs = statement.executeQuery()
                if (rs.next()) {
                    Location(
                        rs.getInt("id"),
                        rs.getString("name"),
                        enumValueOf<LocationType>(rs.getString("type")),
                        rs.getInt("parent_id").takeIf { !rs.wasNull() },
                    )
                } else {
                    null
                }
            }
        }

    override fun getChildLocations(location: Location): List<Location> =
        dbManager.execute(connection) { conn ->
            val sql = "SELECT id, name, type, parent_id FROM locations WHERE parent_id = ?"
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, location.lid)

                val rs = statement.executeQuery()
                val children = mutableListOf<Location>()

                while (rs.next()) {
                    children.add(
                        Location(
                            lid = rs.getInt("id"),
                            name = rs.getString("name"),
                            type = enumValueOf<LocationType>(rs.getString("type")),
                            parentId = rs.getInt("parent_id").takeIf { !rs.wasNull() },
                        ),
                    )
                }

                children
            }
        }

    override fun getLocationPath(lid: Int): List<Location> =
        dbManager.execute(connection) { conn ->
            val path = mutableListOf<Location>()
            var currentId: Int? = lid

            while (currentId != null) {
                val sql = "SELECT id, name, type, parent_id FROM locations WHERE id = ?"
                conn.prepareStatement(sql).use { statement ->
                    statement.setInt(1, currentId)

                    val rs = statement.executeQuery()
                    if (rs.next()) {
                        val location =
                            Location(
                                lid = rs.getInt("id"),
                                name = rs.getString("name"),
                                type = enumValueOf<LocationType>(rs.getString("type")),
                                parentId = rs.getInt("parent_id").takeIf { !rs.wasNull() },
                            )

                        path.add(0, location)
                        currentId = location.parentId
                    } else {
                        throw IllegalArgumentException("Location not found with id: $currentId")
                    }
                }
            }
            path
        }

    override fun getLocations(
        skip: Int,
        limit: Int,
    ): List<Location> =
        dbManager.execute(connection) { conn ->
            val sql = "SELECT id, name, type, parent_id FROM locations LIMIT ? OFFSET ?"
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, limit)
                statement.setInt(2, skip)

                val resultSet = statement.executeQuery()
                val locations = mutableListOf<Location>()

                while (resultSet.next()) {
                    locations.add(
                        Location(
                            lid = resultSet.getInt("id"),
                            name = resultSet.getString("name"),
                            type = enumValueOf<LocationType>(resultSet.getString("type")),
                            parentId = resultSet.getInt("parent_id").takeIf { !resultSet.wasNull() },
                        ),
                    )
                }
                locations
            }
        }
}

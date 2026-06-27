package pt.isel.ls.data.jdbc

import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ls.data.BookingData
import pt.isel.ls.data.HouseData
import pt.isel.ls.data.LocationData
import pt.isel.ls.data.StorageDataManager
import pt.isel.ls.data.TokenData
import pt.isel.ls.data.UserData
import java.sql.Connection

open class DBManager : StorageDataManager {
    private val dbURL = System.getenv("DB_URL") ?: throw IllegalStateException("DB_URL environment variable is missing")

    private val dataSource =
        PGSimpleDataSource().apply {
            setURL(dbURL)
        }

    override val userData: UserData = UserDataJDBC(this)
    override val tokenData: TokenData = TokenDataJDBC(this)
    override val locationData: LocationData = LocationDataJDBC(this)
    override val houseData: HouseData = HouseDataJDBC(this)
    override val bookingData: BookingData = BookingDataJDBC(this)

    override fun clear() {
    }

    fun getConnection(): Connection = dataSource.connection

    fun <T> execute(
        connection: Connection? = null,
        block: (Connection) -> T,
    ): T {
        val conn = connection ?: getConnection()
        return try {
            block(conn)
        } finally {
            if (connection == null) conn.close()
        }
    }
}

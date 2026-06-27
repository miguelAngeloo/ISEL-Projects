package pt.isel.ls.server

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.slf4j.LoggerFactory
import pt.isel.ls.data.jdbc.DBManager
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.services.AuthService
import pt.isel.ls.services.BookingService
import pt.isel.ls.services.HouseService
import pt.isel.ls.services.LocationService
import pt.isel.ls.services.PricePredictionService
import pt.isel.ls.services.UserService
import pt.isel.ls.webapi.AuthWebAPI
import pt.isel.ls.webapi.BookingWebAPI
import pt.isel.ls.webapi.HouseWebAPI
import pt.isel.ls.webapi.LocationWebAPI
import pt.isel.ls.webapi.PricePredictionWebAPI
import pt.isel.ls.webapi.UserWebAPI
import kotlin.collections.contains

val PORT = System.getenv("PORT")?.toIntOrNull() ?: 8080
const val DEFAULT_HOUSE_DETAILS_CACHE_SIZE = 10

val logger = LoggerFactory.getLogger("HTTPServer")!!

fun logRequest(request: Request) {
    logger.info(
        "incoming request: method={}, uri={}, content-type={} accept={}",
        request.method,
        request.uri,
        request.header("content-type"),
        request.header("accept"),
    )
}

class App(
    private val userWebAPI: UserWebAPI,
    private val authWebAPI: AuthWebAPI,
    private val houseWebAPI: HouseWebAPI,
    private val bookingWebAPI: BookingWebAPI,
    private val locationWebAPI: LocationWebAPI,
    private val pricePredictionWebAPI: PricePredictionWebAPI,
) {
    val authRoutes =
        routes(
            "/register" bind POST to authWebAPI::register,
            "/login" bind POST to authWebAPI::login,
            "/logout" bind POST to authWebAPI::logout,
        )

    val userRoutes =
        routes(
            "/{id}/bookings" bind GET to bookingWebAPI::getBookingsByUserId,
            "/{id}" bind GET to userWebAPI::getUserDetails,
            "/" bind PUT to userWebAPI::updateUser,
            "/" bind DELETE to userWebAPI::deleteUser,
        )

    val houseRoutes =
        routes(
            "/" bind POST to houseWebAPI::createHouse,
            "/" bind GET to houseWebAPI::getAllHouses,
            "/count" bind GET to houseWebAPI::countHouses,
            "/search" bind GET to houseWebAPI::searchHouses,
            "/available" bind GET to houseWebAPI::getAvailableHouses,
            "/{id}/available-days" bind GET to houseWebAPI::getAvailableDays,
            "/{id}" bind GET to houseWebAPI::getHouseDetails,
        )

    val bookingRoutes =
        routes(
            "/" bind POST to bookingWebAPI::createBooking,
            "/" bind GET to bookingWebAPI::getBookings,
            "/me" bind GET to bookingWebAPI::getBookingsByUser,
            "/{id}" bind GET to bookingWebAPI::getBookingDetails,
            "/{id}" bind DELETE to bookingWebAPI::deleteBooking,
            "/{id}" bind PUT to bookingWebAPI::updateBooking,
        )

    val locationRoutes =
        routes(
            "/" bind GET to locationWebAPI::getLocations,
            "/" bind POST to locationWebAPI::createNewLocation,
            "/{id}" bind GET to locationWebAPI::getLocationDetails,
            "/{id}/children" bind GET to locationWebAPI::getChildrenLocations,
            "/{id}/fullpath" bind GET to locationWebAPI::getFullPath,
        )

    val predictionRoutes =
        routes(
            "/price" bind GET to pricePredictionWebAPI::getPricePrediction,
        )

    val app =
        routes(
            "/auth" bind authRoutes,
            "/users" bind userRoutes,
            "/houses" bind houseRoutes,
            "/bookings" bind bookingRoutes,
            "/locations" bind locationRoutes,
            "/predictions" bind predictionRoutes,
            singlePageApp(ResourceLoader.Directory("static-content/sparouter")),
        )

    private val serverInstance = app.asServer(Jetty(PORT))

    fun start() {
        serverInstance.start()
    }

    fun stop() {
        serverInstance.stop()
    }
}

fun main(args: Array<String>) {
    logger.info("Server started listening on port $PORT")

    val dbManager = if (args.contains("--db")) DBManager() else MemManager()
    val houseDetailsCacheSize =
        System.getenv("HOUSE_DETAILS_CACHE_SIZE")?.toIntOrNull()?.takeIf { it > 0 }
            ?: DEFAULT_HOUSE_DETAILS_CACHE_SIZE

    if (dbManager is MemManager) {
        logger.info("Memory database is being used")
        dbManager.populateMemData()
    }

    logger.info("House details cache size: $houseDetailsCacheSize")

    val app =
        App(
            UserWebAPI(
                UserService(
                    dbManager,
                ),
            ),
            AuthWebAPI(
                AuthService(
                    dbManager,
                ),
            ),
            HouseWebAPI(
                HouseService(
                    dbManager,
                    houseDetailsCacheSize,
                ),
            ),
            BookingWebAPI(
                BookingService(
                    dbManager,
                ),
            ),
            LocationWebAPI(
                LocationService(
                    dbManager,
                ),
            ),
            PricePredictionWebAPI(
                PricePredictionService(
                    dbManager,
                ),
            ),
        )
    app.start()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            logger.info("Server is shutting down")
            app.stop()
        },
    )
}

package pt.isel.ls.webAPI

import kotlinx.serialization.json.Json
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import pt.isel.ls.bodyRequest
import pt.isel.ls.createTestToken
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationPath
import pt.isel.ls.domain.LocationType
import pt.isel.ls.domain.dto.CreateLocationInput
import pt.isel.ls.domain.dto.CreateLocationOutput
import pt.isel.ls.domain.dto.GetChildLocationsOutput
import pt.isel.ls.services.LocationService
import pt.isel.ls.simpleRequest
import pt.isel.ls.webapi.LocationWebAPI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LocationWebAPITest {
    private lateinit var memManager: MemManager
    private lateinit var locationWebAPI: LocationWebAPI
    private lateinit var locationRoutes: HttpHandler

    @BeforeTest
    fun setup() {
        memManager = MemManager()
        memManager.clear()

        locationWebAPI = LocationWebAPI(locationService = LocationService(memManager))

        locationRoutes =
            routes(
                "/" bind GET to locationWebAPI::getLocations,
                "/" bind POST to locationWebAPI::createNewLocation,
                "/{id}" bind GET to locationWebAPI::getLocationDetails,
                "/{id}/children" bind GET to locationWebAPI::getChildrenLocations,
                "/{id}/fullpath" bind GET to locationWebAPI::getFullPath,
            )
    }

    private fun createTestLocation(
        name: String = "Portugal",
        type: LocationType = LocationType.COUNTRY,
        parentId: Int? = null,
    ): Int = memManager.locationData.createLocation(Location(0, name, type, parentId))

    @Test
    fun `get location details successfully`() {
        val lid = createTestLocation("Portugal", LocationType.COUNTRY)

        val response = simpleRequest(GET, "/$lid").let(locationRoutes)

        assertEquals(Status.OK, response.status)
        val location = Json.decodeFromString<Location>(response.bodyString())
        assertEquals(lid, location.lid)
        assertEquals("Portugal", location.name)
        assertEquals(LocationType.COUNTRY, location.type)
    }

    @Test
    fun `get location details returns 400 when id is invalid`() {
        val response = simpleRequest(GET, "/abc").let(locationRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `get location details returns 404 when location does not exist`() {
        val response = simpleRequest(GET, "/999").let(locationRoutes)

        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `get children locations returns direct children only`() {
        val portugal = createTestLocation("Portugal", LocationType.COUNTRY)
        createTestLocation("Lisboa", LocationType.DISTRICT, portugal)
        createTestLocation("Porto", LocationType.DISTRICT, portugal)

        val response = simpleRequest(GET, "/$portugal/children").let(locationRoutes)

        assertEquals(Status.OK, response.status)
        val output = Json.decodeFromString<GetChildLocationsOutput>(response.bodyString())
        assertEquals(2, output.locations.size)
    }

    @Test
    fun `get children locations returns empty list when no children`() {
        val lid = createTestLocation("Portugal", LocationType.COUNTRY)

        val response = simpleRequest(GET, "/$lid/children").let(locationRoutes)

        assertEquals(Status.OK, response.status)
        val output = Json.decodeFromString<GetChildLocationsOutput>(response.bodyString())
        assertEquals(0, output.locations.size)
    }

    @Test
    fun `get full path returns correct hierarchy`() {
        val portugal = createTestLocation("Portugal", LocationType.COUNTRY)
        val lisboa = createTestLocation("Lisboa", LocationType.DISTRICT, portugal)
        val oeiras = createTestLocation("Oeiras", LocationType.MUNICIPALITY, lisboa)

        val response = simpleRequest(GET, "/$oeiras/fullpath").let(locationRoutes)

        assertEquals(Status.OK, response.status)
        val output = Json.decodeFromString<LocationPath>(response.bodyString())
        assertEquals(3, output.path.size)
        assertEquals("Portugal", output.path[0].name)
        assertEquals("Lisboa", output.path[1].name)
        assertEquals("Oeiras", output.path[2].name)
    }

    @Test
    fun `create location successfully with valid token`() {
        val token = memManager.createTestToken(1)
        val body = Json.encodeToString(CreateLocationInput("Portugal", LocationType.COUNTRY, null))

        val response = bodyRequest(POST, "/", token, body).let(locationRoutes)

        assertEquals(Status.CREATED, response.status)
        val output = Json.decodeFromString<CreateLocationOutput>(response.bodyString())
        assertEquals(1, output.lid)
    }

    @Test
    fun `create location without token returns 401 Unauthorized`() {
        val body = Json.encodeToString(CreateLocationInput("Portugal", LocationType.COUNTRY, null))

        val response = bodyRequest(POST, "/", null, body).let(locationRoutes)

        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `get locations with paging returns correct page`() {
        repeat(5) { i -> createTestLocation("Location $i", LocationType.LOCALITY) }

        val response = simpleRequest(GET, "/?skip=1&limit=2").let(locationRoutes)

        assertEquals(Status.OK, response.status)
        val locations = Json.decodeFromString<List<Location>>(response.bodyString())
        assertEquals(2, locations.size)
        assertEquals("Location 1", locations[0].name)
        assertEquals("Location 2", locations[1].name)
    }

    @Test
    fun `get locations with negative skip returns 400`() {
        val response = simpleRequest(GET, "/?skip=-1&limit=5").let(locationRoutes)

        assertEquals(Status.BAD_REQUEST, response.status)
    }
}

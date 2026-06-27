package pt.isel.ls.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationTest {
    @Test
    fun `Create root location without parent`() {
        val portugal =
            Location(
                lid = 1,
                name = "Portugal",
                type = LocationType.COUNTRY,
                parentId = null,
            )

        assertEquals(1, portugal.lid)
        assertEquals("Portugal", portugal.name)
        assertEquals(LocationType.COUNTRY, portugal.type)
        assertNull(portugal.parentId)
    }

    @Test
    fun `Create child location with parent`() {
        val lisboa =
            Location(
                lid = 2,
                name = "Lisboa",
                type = LocationType.DISTRICT,
                parentId = 1,
            )

        assertEquals(2, lisboa.lid)
        assertEquals("Lisboa", lisboa.name)
        assertEquals(LocationType.DISTRICT, lisboa.type)
        assertEquals(1, lisboa.parentId)
    }

    @Test
    fun `Create location path`() {
        val portugal = Location(lid = 1, name = "Portugal", type = LocationType.COUNTRY, parentId = null)
        val lisboa = Location(lid = 2, name = "Lisboa", type = LocationType.DISTRICT, parentId = 1)
        val oeiras = Location(lid = 3, name = "Oeiras", type = LocationType.MUNICIPALITY, parentId = 2)

        val locationPath =
            LocationPath(
                path = listOf(portugal, lisboa, oeiras),
            )

        assertEquals(3, locationPath.path.size)
        assertEquals("Portugal", locationPath.path[0].name)
        assertEquals("Lisboa", locationPath.path[1].name)
        assertEquals("Oeiras", locationPath.path[2].name)
    }
}

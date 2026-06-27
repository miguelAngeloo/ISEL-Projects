package pt.isel.ls.domain

import pt.isel.ls.domain.dto.AvailableHouse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HouseTest {
    @Test
    fun `Create a valid house`() {
        val house =
            House(
                id = 101,
                ownerId = 1,
                title = "Casa de Praia",
                locationId = 3,
                areaSqMt = 120,
                pricePerNight = 75.50,
                description = "Excelente vista para o mar.",
            )

        assertEquals(101, house.id)
        assertEquals("Casa de Praia", house.title)
        assertEquals(3, house.locationId)
        assertEquals(120, house.areaSqMt)
        assertEquals(75.50, house.pricePerNight)
        assertEquals("Excelente vista para o mar.", house.description)
    }

    @Test
    fun `Create an available house wrapper`() {
        val house = House(102, 2, "Refúgio na Montanha", 15, 80, 50.0, "Perto da neve.")

        val availableHouse =
            AvailableHouse(
                house = house,
                available = true,
            )

        assertEquals(102, availableHouse.house.id)
        assertTrue(availableHouse.available)
    }

    @Test
    fun `Create an unavailable house wrapper`() {
        val house = House(103, 3, "Apartamento Central", 2, 60, 90.0, "No centro de Lisboa.")

        val unavailableHouse =
            AvailableHouse(
                house = house,
                available = false,
            )

        assertEquals("Apartamento Central", unavailableHouse.house.title)
        assertFalse(unavailableHouse.available)
    }
}

package pt.isel.ls.data.mem

import pt.isel.ls.domain.Booking
import pt.isel.ls.domain.House
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.Token
import pt.isel.ls.domain.User

open class MemStorage {
    val usersDB get() = users
    val tokensDB get() = tokens

    val uid get() = users.size + 1
    val hid get() = houses.size + 1
    val bid get() = bookings.size + 1

    val lid get() = locations.size + 1

    val locationsDB get() = locations
    val housesDB get() = houses
    val bookingsDB get() = bookings

    companion object {
        private val users = mutableListOf<User>()
        private val tokens = mutableListOf<Token>()
        private val locations = mutableListOf<Location>()
        private val houses = mutableListOf<House>()
        private val bookings = mutableListOf<Booking>()
    }

    fun clear() {
        users.clear()
        tokens.clear()
        locations.clear()
        houses.clear()
        bookings.clear()
    }
}

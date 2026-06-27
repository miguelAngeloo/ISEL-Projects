package pt.isel.ls.data.mem

import kotlinx.datetime.LocalDate
import pt.isel.ls.data.StorageDataManager
import pt.isel.ls.domain.Location
import pt.isel.ls.domain.LocationType
import pt.isel.ls.domain.requests.BookingCreationRequest
import pt.isel.ls.domain.requests.HouseCreationRequest
import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName
import pt.isel.ls.services.AuthService
import pt.isel.ls.services.BookingService
import pt.isel.ls.services.HouseService

class MemManager : StorageDataManager {
    override val tokenData = TokenDataMem()
    override val userData = UserDataMem()
    override val locationData = LocationDataMem()
    override val houseData = HouseDataMem()
    override val bookingData = BookingDataMem()

    fun populateMemData() {
        populateMemLocation()
        val authService = AuthService(this)
        val houseService = HouseService(this)
        val bookingService = BookingService(this)

        // Register users
        authService.register(
            UserName("João Silva"),
            email = Email("joao@email.com"),
            password = Password("password"),
        )
        authService.register(
            UserName("Maria Costa"),
            email = Email("maria@email.com"),
            password = Password("password"),
        )
        authService.register(
            UserName("Pedro Santos"),
            email = Email("pedro@email.com"),
            password = Password("password"),
        )
        authService.register(
            UserName("Ana Silva"),
            email = Email("ana@email.com"),
            password = Password("password"),
        )
        val token1 = authService.login("joao@email.com", "password")
        val token2 = authService.login("maria@email.com", "password")
        val token3 = authService.login("pedro@email.com", "password")
        val token4 = authService.login("ana@email.com", "password")

        // Create houses
        houseService.createHouse(
            HouseCreationRequest(
                title = "Casa na Praia",
                locationId = 1,
                areaSqMt = 120,
                pricePerNight = 100.0,
                description = "Uma casa confortável à beira-mar com vista para o oceano.",
            ),
            tokenString = token1.token.toString(),
        )

        houseService.createHouse(
            HouseCreationRequest(
                title = "Apartamento no Centro",
                locationId = 1,
                areaSqMt = 85,
                pricePerNight = 75.0,
                description = "Apartamento moderno no coração da cidade.",
            ),
            tokenString = token2.token.toString(),
        )

        houseService.createHouse(
            HouseCreationRequest(
                title = "Villa de Luxo",
                locationId = 1,
                areaSqMt = 250,
                pricePerNight = 250.0,
                description = "Villa luxuosa com piscina e jardim privado.",
            ),
            tokenString = token3.token.toString(),
        )

        val hLisboa1 =
            houseService.createHouse(
                HouseCreationRequest(
                    title = "Estúdio Baixa",
                    locationId = 2,
                    areaSqMt = 40,
                    pricePerNight = 50.0,
                    description = "Pequeno mas central.",
                ),
                tokenString = token1.token.toString(),
            )
        val hLisboa2 =
            houseService.createHouse(
                HouseCreationRequest(
                    title = "T1 Alfama",
                    locationId = 2,
                    areaSqMt = 60,
                    pricePerNight = 75.0,
                    description = "Com vista rio.",
                ),
                tokenString = token2.token.toString(),
            )
        val hLisboa3 =
            houseService.createHouse(
                HouseCreationRequest(
                    title = "T2 Saldanha",
                    locationId = 2,
                    areaSqMt = 85,
                    pricePerNight = 100.0,
                    description = "Espaçoso e moderno.",
                ),
                tokenString = token3.token.toString(),
            )
        houseService.createHouse(
            HouseCreationRequest(
                title = "T3 Marquês",
                locationId = 2,
                areaSqMt = 110,
                pricePerNight = 130.0,
                description = "Ideal para famílias.",
            ),
            tokenString = token4.token.toString(),
        )
        houseService.createHouse(
            HouseCreationRequest(
                title = "T4 Avenidas Novas",
                locationId = 2,
                areaSqMt = 150,
                pricePerNight = 180.0,
                description = "Luxo no centro.",
            ),
            tokenString = token1.token.toString(),
        )
        houseService.createHouse(
            HouseCreationRequest(
                title = "Penthouse Parque",
                locationId = 2,
                areaSqMt = 200,
                pricePerNight = 240.0,
                description = "O topo da cidade.",
            ),
            tokenString = token2.token.toString(),
        )

        val hOeiras1 =
            houseService.createHouse(
                HouseCreationRequest(
                    title = "Villa Oeiras",
                    locationId = 3,
                    areaSqMt = 100,
                    pricePerNight = 120.0,
                    description = "Perto do parque dos poetas.",
                ),
                tokenString = token3.token.toString(),
            )

        for (i in 1..8) {
            houseService.createHouse(
                HouseCreationRequest(
                    title = "Casa Genérica $i",
                    locationId = if (i % 2 == 0) 1 else 4,
                    areaSqMt = 50 + (i * 10),
                    pricePerNight = 40.0 + (i * 5),
                    description = "Uma casa gerada automaticamente para testes.",
                ),
                tokenString = token4.token.toString(),
            )
        }

        bookingService.createBooking(
            BookingCreationRequest(
                hid = hLisboa1,
                endDate = LocalDate(2026, 3, 27),
                startDate = LocalDate(2026, 3, 20),
            ),
            tokenString = token2.token.toString(),
        )

        bookingService.createBooking(
            BookingCreationRequest(
                hid = hLisboa2,
                endDate = LocalDate(2026, 4, 10),
                startDate = LocalDate(2026, 4, 1),
            ),
            tokenString = token3.token.toString(),
        )

        bookingService.createBooking(
            BookingCreationRequest(
                hid = hLisboa3,
                endDate = LocalDate(2026, 5, 22),
                startDate = LocalDate(2026, 5, 15),
            ),
            tokenString = token1.token.toString(),
        )

        bookingService.createBooking(
            BookingCreationRequest(
                hid = hLisboa2,
                endDate = LocalDate(2026, 6, 8),
                startDate = LocalDate(2026, 6, 1),
            ),
            tokenString = token3.token.toString(),
        )

        bookingService.createBooking(
            BookingCreationRequest(
                hid = hOeiras1,
                endDate = LocalDate(2026, 7, 20),
                startDate = LocalDate(2026, 7, 10),
            ),
            tokenString = token4.token.toString(),
        )
    }

    fun populateMemLocation() {
        locationData.createLocation(
            Location(0, "Portugal", LocationType.COUNTRY, null),
        )
        locationData.createLocation(
            Location(0, "Lisboa", LocationType.DISTRICT, 1),
        )
        locationData.createLocation(
            Location(0, "Oeiras", LocationType.MUNICIPALITY, 2),
        )
        locationData.createLocation(
            Location(0, "Paço de Arcos", LocationType.LOCALITY, 3),
        )
    }

    override fun clear() {
        userData.clear()
        tokenData.clear()
        locationData.clear()
        houseData.clear()
        bookingData.clear()
    }
}

package pt.isel.ls.data

interface StorageDataManager {
    val userData: UserData
    val tokenData: TokenData
    val locationData: LocationData
    val houseData: HouseData
    val bookingData: BookingData

    fun clear()
}

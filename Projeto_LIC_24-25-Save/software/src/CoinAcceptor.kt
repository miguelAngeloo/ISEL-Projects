import CoinAcceptor.isCoinInserted
import isel.leic.utils.Time

const val ACCEPT = 0b0100_0000
const val COIN_ID = 0b0010_0000
const val COIN = 0b0100_0000


enum class Coin(val value: Int) {
    TWO(2),
    FOUR(4);
}

object CoinAcceptor {

    fun init() {
        HAL.init()
    }

    fun isCoinInserted(): Boolean = HAL.isBit(COIN)

    fun isFourCoin(): Boolean = HAL.isBit(COIN_ID)

    fun getCoin(): Coin =
        if (isFourCoin()) Coin.FOUR else Coin.TWO


    fun sendAccept() {
        if (isCoinInserted()) {
            HAL.setBits(ACCEPT)
        }
        while (isCoinInserted()) {}
        HAL.clrBits(ACCEPT)
    }
}


fun main() {
    CoinAcceptor.init()
    var credits = 0
    while (true) {
        if (isCoinInserted()) {
            val coin = CoinAcceptor.getCoin()
            println("Coin ${coin.name} inserted")
            CoinAcceptor.sendAccept()
            credits += when (coin) {
                Coin.TWO -> 2
                Coin.FOUR -> 4
            }
            println("Credits: $credits")
        }
    }
}
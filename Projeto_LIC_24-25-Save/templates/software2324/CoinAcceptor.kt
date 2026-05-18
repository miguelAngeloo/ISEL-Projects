// Object Coin Acceptor
object CoinAcceptor {
    const val COIN_MASK = 0b0100_0000

    fun init() {
        HAL.init()
    }

    fun isCoin(): Boolean = HAL.isBit(COIN_MASK)

    fun acceptCoin(on_off: Boolean) {
        if (on_off) {
            HAL.setBits(COIN_MASK)
        } else {
            HAL.clrBits(COIN_MASK)
        }
    }
}

fun main() {
    CoinAcceptor.init()
    while (true) {
        if (CoinAcceptor.isCoin()) {
            println("Coin inserted")
        }
    }
}
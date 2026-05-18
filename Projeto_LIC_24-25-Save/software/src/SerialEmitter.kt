import isel.leic.utils.Time

object SerialEmitter {
    enum class Destination { LCD, Roulette }

    private const val SDX = 0b0000_0001
    private const val SCLK = 0b0000_0010
    private const val LCD_SEL = 0b0000_0100
    private const val ROULETTE_SEL = 0b0000_1000

    private const val PARITY_IS_ODD = true
    private val PARITY_CONSTANT = if (PARITY_IS_ODD) 1 else 0

    fun init() {
        HAL.init()
        HAL.setBits(LCD_SEL + ROULETTE_SEL)
    }

    fun send(addr: Destination, data: Int, size: Int) {
        val destMask = when (addr) {
            Destination.LCD -> LCD_SEL
            Destination.Roulette -> ROULETTE_SEL
        }
        HAL.clrBits(destMask)
        var parity = 0
        for (i in 0..(size - 1)) {
            val bit = data and (1 shl i)
            if (bit == 0)
                HAL.clrBits(SDX)
            else {
                HAL.setBits(SDX)
                parity++
            }
            clock()
        }
        if (parity % 2 == PARITY_CONSTANT) HAL.clrBits(SDX)
        else HAL.setBits(SDX)
        clock()
        HAL.setBits(destMask)
    }

    private fun clock() {
        HAL.setBits(SCLK)
        HAL.clrBits(SCLK)
    }
}



fun main() {

    SerialEmitter.init()
    while (true) {
        SerialEmitter.send(SerialEmitter.Destination.LCD, 0x14, 5)
        SerialEmitter.send(SerialEmitter.Destination.LCD, 0x15, 5)
    }

}
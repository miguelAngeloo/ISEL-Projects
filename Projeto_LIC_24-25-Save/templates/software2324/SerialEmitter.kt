import isel.leic.utils.Time

// Object Serial Emitter
object SerialEmitter {
    enum class Destination { LCD, SCORE }

    // Original Simulation
    //    private const val DATA_MASK = 0b0000_1000
    //    private const val CLOCK = 0b0001_0000
    //    private const val LCD_SS_MASK = 0b0000_0001
    //    private const val SCORE_SS_MASK = 0b0000_0010

    // Simulation Adapted to our Version
    private const val DATA_MASK = 0b0000_0001
    private const val CLOCK = 0b0000_0010
    private const val LCD_SS_MASK = 0b0000_0100
    private const val SCORE_SS_MASK = 0b0000_1000

    // Inicializa o emissor serial
    fun init() {
        HAL.setBits(0b0000_1100)
    }

    // Envia uma trama para o Serial Reciever identificado em 'addr', os bits de dados
    // em 'data' e em 'size' o número de bits a enviar
    fun send(addr: Destination, data: Int, size: Int) {
        val SS_MASK:Int
        if (addr == Destination.LCD) SS_MASK = LCD_SS_MASK
        else {
            SS_MASK = SCORE_SS_MASK
        }
        HAL.clrBits(SS_MASK)
        var data = data
        var parity = 0
        for (i in 0..(size - 1)) {
            val bit = data and (1 shl i)

            if (bit == 0) {
                HAL.clrBits(DATA_MASK)
            } else {
                HAL.setBits(DATA_MASK)
                parity++
            }
            HAL.setBits(CLOCK)
            HAL.clrBits(CLOCK)
        }

        if (parity % 2 == 0) {
            HAL.clrBits(DATA_MASK)
        } else {
            HAL.setBits(DATA_MASK)
        }
        HAL.setBits(CLOCK)
        HAL.clrBits(CLOCK)

        HAL.setBits(SS_MASK)
    }
}

fun main() {
    HAL.init()
    SerialEmitter.init()
}
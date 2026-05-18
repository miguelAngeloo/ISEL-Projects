import isel.leic.UsbPort
import java.lang.IndexOutOfBoundsException

private var INIT_VALUE = 0x00 //TODO() adicionar sel do LCD e ROLLETE bits a '1'

object HAL {
    // Inicia o objeto
    fun init() {
        UsbPort.write(INIT_VALUE)
    }

    // Retorna 'true' se o bit definido pela mask está com o valor lógico '1' no UsbPort
    fun isBit(mask: Int): Boolean {
        val value = UsbPort.read()
        for (i in 0..7) {
            if (mask == (1 shl i)) {
                return (value and mask) != 0
            }
        }
        return false
    }

    // Retorna os valores dos bits representados por mask presentes no UsbPort
    fun readBits(mask: Int): Int {
        val value = UsbPort.read()
        return (value and mask)
    }

    private var lastValue = 0
    // Escreve nos bits representados por mask os valores dos bits correspondentes em value
    fun writeBits(mask: Int, value: Int) {
        clrBits(mask)
        setBits(value)
    }

    // Coloca os bits representados por mask no valor lógico '1'
    fun setBits(mask: Int) {
        val value = lastValue or mask
        UsbPort.write(value).also { lastValue = value }
    }

    // Coloca os bits representados por mask no valor lógico '0'
    fun clrBits(mask: Int) {
        val value = lastValue and mask.inv()
        UsbPort.write(value).also { lastValue = value }
    }
}


fun main() {
    HAL.init()
    while (true) {
        (readln().trim().split(' ').map { it.toIntOrNull() ?: 0 }).also {
            try {
                HAL.writeBits(it[0], it[1])
            } catch (ex : IndexOutOfBoundsException) {
                return
            }
        }
    }
}
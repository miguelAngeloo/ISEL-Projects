
// Keyboard
object KBD {
    const val NONE = 0
    const val D_VAL = 0x10
    const val ACK = 0x80
    const val DATA = 0x0F
    val LIST = arrayOf('1', '4', '7', '*', '2', '5', '8', '0', '3', '6', '9', '#')
    fun init() {
        HAL.readBits(0xFF)
    }

    fun getKey():Char {
        if (HAL.isBit(D_VAL)) {
            val data = HAL.readBits(DATA)
            HAL.writeBits(ACK, ACK)
            HAL.writeBits(ACK, 0)
            return LIST[data]
        } else {
            HAL.writeBits(ACK, 0)
            return NONE.toChar()
        }
    }

    fun waitKey(timeout:Long):Char {
        var c:Char
        val currTime = System.currentTimeMillis()
        var finalTime = System.currentTimeMillis()
        var time = finalTime - currTime
        while (true) {
            if (time >= timeout) {
                c = NONE.toChar()
                break
            }
            else {
                c = getKey()
                if (c != NONE.toChar()) {
                    break
                }
                finalTime = System.currentTimeMillis()
            }
            time = finalTime - currTime
        }
        return c
    }
}

fun main() {
    print("Insira o tempo de espera: ")
    val timeout = readln().toInt()
    KBD.init()
    var a:Char
    while (true) {
        a = KBD.waitKey(timeout.toLong())
        if (a != KBD.NONE.toChar()) {
            println(a)
        } else {
            println("Timeout")
        }
    }
}
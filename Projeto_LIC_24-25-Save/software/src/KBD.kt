import isel.leic.utils.Time

object KBD {
    const val NONE = 0.toChar()
    private const val D_VAL = 0x10
    private const val DATA = 0x0F
    private const val ACK = 0x80
    private val KEYS = charArrayOf(
        '1', '4', '7', '*',
        '2', '5', '8', '0',
        '3', '6', '9', '#',
        'A', 'B', 'C', 'D'
    )

    // Inicia a classe
    fun init() {
        HAL.init()
    }

    // Retorna de imediato a tecla premida ou NONE se não há tecla premida
    fun getKey(): Char {
        if (!HAL.isBit(D_VAL)) return NONE

        val data = HAL.readBits(DATA)
        HAL.setBits(ACK)

        while (HAL.isBit(D_VAL)) {}
        HAL.clrBits(ACK)
        return KEYS[data]
    }

    // Retorna a tecla premida, caso ocorra antes do 'timeout' (em milissegundos),
    // ou NONE caso contrário
    fun waitKey(timeout: Long): Char {
        var c: Char
        val currTime = System.currentTimeMillis()
        var finalTime = System.currentTimeMillis()
        var time = finalTime - currTime
        while (true) {
            if (time >= timeout) {
                c = NONE
                break
            } else {
                c = getKey()
                if (c != NONE) {
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
        if (a != KBD.NONE) {
            println(a)
        } else {
            println("Timeout")
        }
    }
}
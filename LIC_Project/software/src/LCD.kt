import isel.leic.utils.Time

object LCD {
    private const val LINES = 2
    private const val COLS = 16

    private const val RS_MASK = 0x10
    private const val EN_MASK = 0x20
    private const val DATA_MASK = 0x0F

    private const val SERIAL_INTERFACE = true

    // Escreve um byte de comando/dados no LCD em paralelo
    private fun writeNibbleParallel(rs: Boolean, data: Int) {
        if (rs) HAL.setBits(RS_MASK) else HAL.clrBits(RS_MASK)
        HAL.setBits(EN_MASK)
        HAL.writeBits(DATA_MASK, data)
        HAL.clrBits(EN_MASK)
    }

    // Escreve um byte de comando/dados no LCD em série
    private fun writeNibbleSerial(rs: Boolean, data: Int) {
        val rsValue = if (rs) 1 else 0
        val trama = (data shl 1) or rsValue
        SerialEmitter.send(SerialEmitter.Destination.LCD, trama, 5)
    }

    // Escreve um nibble de comando/dados no LCD
    private fun writeNibble(rs: Boolean, data: Int) {
        if (SERIAL_INTERFACE) writeNibbleSerial(rs, data)
        else writeNibbleParallel(rs, data)
    }

    // Escreve um byte de comando/dados no LCD
    private fun writeByte(rs: Boolean, data: Int) {
        writeNibble(rs, (data shr 4) and DATA_MASK)
        writeNibble(rs, data and DATA_MASK)
    }

    // Escreve um comando no LCD
    private fun writeCMD(data : Int) = writeByte(false, data)

    // Escreve um dado no LCD
    private fun writeDATA(data: Int)  = writeByte(true, data)

    // Envia a sequência de iniciação para comunicação a 4 bits.
    fun init() {
        Time.sleep(60)
        writeNibble(false, 0b0011)
        Time.sleep(6)
        writeNibble(false, 0b0011)
        Time.sleep(2)

        //writeCMD(0b0011_0010)

        writeNibble(false, 0b0011)
        writeNibble(false, 0b0010)


        //writeCMD(0b0010_1000)

        writeNibble(false, 0b0010)
        writeNibble(false, 0b1000)

        //writeCMD(0b0000_1000)

        writeNibble(false, 0b0000)
        writeNibble(false, 0b1000)

        //writeCMD(0b0000_0001)


        writeNibble(false, 0b0000)
        writeNibble(false, 0b0001)

        Time.sleep(10)

        //writeCMD(0b0000_0110)


        writeNibble(false, 0b0000)
        writeNibble(false, 0b0110)

        writeNibble(false, 0b0000)
        writeNibble(false, 0b1100)

        //writeCMD(0b0000_1111)
        //on()
    }

    // Escreve um caractere na posição corrente.
    fun write(c: Char) = writeDATA(c.code)

    // Escreve uma string na posição corrente.
    fun write(text: String) = text.forEach { write(it) }

    // Envia comando para posicionar cursor (’line’: 0..LINES-1 , ’column’: 0..COLS - 1 )
    fun cursor(line: Int, column: Int) {
        val addr = if (line == 1) 0x40 + column else column
        writeCMD(0b10000000 + addr)
    }

    // Envia comando para limpar o ecrã e posicionar o cursor em (0,0)
    fun clear() {
        writeCMD(0b0000_0001)
        Time.sleep(2)
    }

    // Envia comando para ligar/desligar o display
    fun on() = writeCMD(0b0000_1111)
    fun off() = writeCMD(0b0000_1000)
}

fun main() {
    HAL.init()
    SerialEmitter.init()
    LCD.init()
    do {
        while (true) {
            println("1-Mudar cursor; 2- Escrever; 3- Limpar; 4- Desligar; 5- Ligar")
            val ans = readln().toIntOrNull()
            when (ans) {
                1 -> {
                    println("Linha: ")
                    val l = readln().toInt()
                    println("Coluna: ")
                    val c = readln().toInt()
                    LCD.cursor(l, c)
                }
                2 -> {
                    print("Texto: ")
                    val text = readln()
                    LCD.write(text)
                }
                3 -> LCD.clear()
                4 -> LCD.off()
                5 -> LCD.on()
                else -> break
            }
        }
    } while(true)
}
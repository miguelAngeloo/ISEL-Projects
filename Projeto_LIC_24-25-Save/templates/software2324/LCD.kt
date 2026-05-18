import isel.leic.utils.*
// LCD Object
object LCD {
    private const val DATA_MASK = 0x0F      // Máscara para escrita de dados
    private const val RS_MASK = 0x40        // Máscara para o bit RS
    private const val E_MASK = 0x20         // Máscara para o bit de Enable
    private const val CLK_REG_MASK = 0x10   // Máscara para o bit de Clock
    private const val NOT_RW = 0x7F         // Máscara para o bit de Read/Write

    private const val LINES = 2
    private const val COLS = 16
    private const val SERIAL_INTERFACE = true

    private fun writeByteParallel(rs: Boolean, data: Int) {
        val d = data.ushr(4)

        if (rs) {
            HAL.setBits(RS_MASK)     // Bit RS = 1 caso RS seja true
        } else {
            HAL.clrBits(RS_MASK)     // BIt RS = 0 caso RS seja false
        }
        HAL.clrBits(NOT_RW)       // Bit RW = 0

        HAL.writeBits(E_MASK, E_MASK)

        HAL.writeBits(DATA_MASK, d)
        HAL.setBits(CLK_REG_MASK)
        HAL.clrBits(CLK_REG_MASK)

        HAL.writeBits(DATA_MASK, data)
        HAL.setBits(CLK_REG_MASK)
        HAL.clrBits(CLK_REG_MASK)

        HAL.clrBits(E_MASK)
    }

    private fun writeByteSerial(rs:Boolean, data:Int) {
        val size = 9
        val rs = if (rs) 1 else 0
        var result = (data shl 1) or rs
        SerialEmitter.send(SerialEmitter.Destination.LCD, result, size)
    }

    private fun writeByte(rs: Boolean, data: Int) {
        if (SERIAL_INTERFACE)
            writeByteSerial(rs, data)
        else
            writeByteParallel(rs, data)
    }

    private fun writeCMD(data: Int) {
        writeByte(false, data)
    }

    private fun writeDATA(data: Int) {
        writeByte(true, data)
    }

    fun init() {
        writeCMD(0b0011_1111)
        Time.sleep(10)
        writeCMD(0b0011_0000)      // Instruções de iniciação
        Time.sleep(5)
        writeCMD(0b0011_0000)
        Time.sleep(2)

        writeCMD(0b0011_1000)      // Funtion Set

        writeCMD(0b0000_1000)      // Display OFF

        writeCMD(0b0000_0001)      // Display Clear

        writeCMD(0b0000_0110)      // Entry Mode Set

        writeCMD(0b0000_1110)      // Display ON

    }

    fun write(c: Char) = writeDATA(c.code)

    fun write(text: String) {
        text.forEach() {
                write(it)
                Time.sleep(1)
            }
        }


    // Cursor shift by Set DDRAM address LCD command
    fun cursor(line: Int, column: Int) {
        if (line in 1..LINES && column in 1..COLS) {
            val position = if (line == 2) 0x40 + column - 1 else column - 1
            writeCMD(0b1000_0000 + position)
        }
    }

    fun clear() {
        writeCMD(0b0000_0001)
    }

    fun off() {
        writeCMD(0b0000_1000)
    }

    fun on() {
        writeCMD(0b0000_1111)
    }
}

fun main() {
    TUI.init()
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
                TUI.cursor(l, c)
            }
            2 -> {
                print("Texto: ")
                val text = readln()
                TUI.write(text)
            }
            3 -> TUI.clear()
            4 -> TUI.off()
            5 -> TUI.on()
            else -> break
        }
    }
    } while(true)
}
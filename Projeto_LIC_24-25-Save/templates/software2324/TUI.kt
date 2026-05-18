import isel.leic.utils.Time
// Object TUI - Text User Interface
object TUI {
    fun init() {
        HAL.init()
        SerialEmitter.init()
        LCD.init()
        KBD.init()
    }

    fun cursor(l:Int, c:Int) {
        LCD.cursor(l, c)
    }

    fun write(c:Char) {
        LCD.write(c)
    }

    fun write(text:String) {
        LCD.write(text)
    }

    fun writeAt(l:Int, c:Int, text:String) {
        cursor(l, c)
        write(text)
    }

    fun writeMid(l:Int, text:String) {
        writeAt(l, 9 - text.length / 2, text)
    }

    fun writeLeft(line:Int,text:String) {
        Time.sleep(1)
        cursor(line, 1)
        write(text)
    }

    fun writeRight(line:Int, col:Int, text:String) {
        cursor(line, 16 - col + 1)
        Time.sleep(1)
        write(text)
    }

    fun waitKey(time:Long): Char {
        return KBD.waitKey(time)
    }

    fun clearLine(line:Int) {
        for (i in 1..16)
            writeAt(line, i, " ")
    }

    fun clear() {
        LCD.clear()
    }

    fun off() {
        LCD.off()
    }

    fun on() {
        LCD.on()
    }

}

fun main() {
    TUI.init()
    do {
        print("Insira o tempo de espera: ")
        val ans = readln().toLongOrNull()
        var key: Char
        while (true) {
            if (ans == null) break
            key = TUI.waitKey(ans)
            if (key == 0.toChar()) {
                println("No key pressed")
            } else {
                when (key) {
                    '*' -> {
                        print("line: ")
                        val line = readln().toIntOrNull()
                        print("column: ")
                        val column = readln().toIntOrNull()
                        if (line != null && column != null)
                            TUI.cursor(line, column)
                    }
                    else ->
                        TUI.write(key.toString())
                }
            }
        }
    } while (true)
}
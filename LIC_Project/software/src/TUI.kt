import KBD
import LCD
import isel.leic.utils.Time
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

object TUI {

    fun init() {
        LCD.init()
        KBD.init()
    }

    fun waitKey(timeout: Long) = KBD.waitKey(timeout)

    fun getKey() = KBD.getKey()

    fun write(text: String) = LCD.write(text)

    fun write(c: Char) = LCD.write(c)

    fun clear() = LCD.clear()

    fun moveCursor(line: Int, column: Int) = LCD.cursor(line, column)

    fun on() = LCD.on()

    fun off() = LCD.off()

}

fun main() {

    TUI.init()
    var count = 0
    var line = 1

    do {
        val key = KBD.waitKey(10000).toString()
        TUI.write(key)
        count++
        if (count >= 16) {
            TUI.moveCursor(line, 0)
            count = 0
            if (line == 0)
                TUI.clear()
            line = line xor 1
        }

    } while (true)
}
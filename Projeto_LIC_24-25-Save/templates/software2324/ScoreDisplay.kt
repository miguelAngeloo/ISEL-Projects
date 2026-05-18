import isel.leic.utils.Time
// Score Display Object
object ScoreDisplay {
    fun init(){
        off(true)
        // As seguintes instruções colocam todos os dígitos do display a 0
        setScore(0)
        setScore(1)
        setScore(2)
        setScore(3)
        setScore(4)
        setScore(5)
        off(false)
    }

    fun setScore(value:Int) {
        SerialEmitter.send(SerialEmitter.Destination.SCORE, value, 7)
        SerialEmitter.send(SerialEmitter.Destination.SCORE, 0b0000_110, 7)
    }

    fun off(value:Boolean) {
        if (value) {
            SerialEmitter.send(SerialEmitter.Destination.SCORE, 0b1111_111, 7)
        }
        else {
            SerialEmitter.send(SerialEmitter.Destination.SCORE, 0b1110_111, 7)
        }
    }
}

fun main() {
    ScoreDisplay.init()

    Time.sleep(5000)
    while (true) {
        ScoreDisplay.off(true)
        Time.sleep(1000)
        ScoreDisplay.off(false)
        Time.sleep(500)
        print("Valor: ")
        val v = readln().toInt() * 8
        print("Dígito: ")
        val d = readln().toInt() - 1
        ScoreDisplay.setScore(v + d)
        Time.sleep(1000)
    }
}
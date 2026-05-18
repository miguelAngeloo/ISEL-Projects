// Object Maintenance
object M{
    const val MAINTENANCE_MASK = 0b1000_0000

    var prevMode = false

    fun mtCheck():Boolean {
        if (HAL.isBit(MAINTENANCE_MASK) && prevMode == false) {
            prevMode = true
            return true
        }
        else {
            return false
        }
    }
}

fun main() {
    M.mtCheck()
    while (true) {
        if (M.mtCheck()) {
            println("Maintenance mode")
            break
        }
    }
}
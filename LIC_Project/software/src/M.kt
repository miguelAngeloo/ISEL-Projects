
object Maintenance {

    const val MAINTENANCE_MASK = 0b1000_0000

    fun init() {
        HAL.init()
    }


    fun maintenance(): Boolean {
        return HAL.isBit(MAINTENANCE_MASK)
    }

    fun statistics() {
        val timeout = 3000L
        val startTime = System.currentTimeMillis()

        val stats = Statistics.loadGameData()

        while (System.currentTimeMillis() - startTime < timeout) {
            val key = TUI.waitKey(3000)
            if (key == KBD.NONE) return

            val mapKey = when (key) {
                'A' -> 10
                'B' -> 11
                'C' -> 12
                'D' -> 13
                '*' -> return
                else -> key.digitToInt()
            }

            val gameStats = stats.find { it.number == mapKey }
            TUI.clear()
            TUI.moveCursor(0, 0)
            if (gameStats != null) {
                TUI.write("${gameStats.number} -> ${gameStats.times} $:${gameStats.moneyWon}")
            }
        }
    }

    fun count() {
        TUI.clear()
        TUI.moveCursor(0, 0)
        TUI.write("Games: ${App.gameInfo.gamesPlayed}")
        TUI.moveCursor(1, 0)
        TUI.write("Coins: ${App.AMOUNT}")
    }
}






fun main() {
    HAL.init()
    while (true) {
        if (Maintenance.maintenance()) {
            println("M Mode")
            break
        }
        else {
            println("Game Mode")
        }
    }
}

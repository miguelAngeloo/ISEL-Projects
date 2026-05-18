import isel.leic.utils.Time
fun main() {
    APP.init()
    Time.sleep(1000)
    var keyPressed:Char // Tecla premida
    var mtMode = false  // Modo de manutenção
    APP.logotype()
    var prevTime = Time.getTimeInMillis()
    var currTime: Long

    while (true) {
        currTime = Time.getTimeInMillis()
        when {
            M.mtCheck() -> {
                APP.mode = APP.Mode.MAINTENANCE
                APP.mtMode()
            }
            currTime - prevTime > 10000 -> {
                APP.showScore()
                prevTime = currTime
            }
            else -> {
                if (APP.Mode.GAME == APP.mode) {
                    APP.showScore()
                    keyPressed = APP.read(500)
                    APP.updateCoin()
                    if (keyPressed == '#' && APP.coins > 0) {
                        APP.coins--
                        APP.gameMode(true)
                    }
                }
            }
        }

        if (currTime - prevTime > 5000) {
            prevTime = currTime
            APP.showScore()
        }

        Scores.writeScore(Scores.scoreList)
    }
}
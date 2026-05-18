import isel.leic.utils.Time
import java.lang.System.currentTimeMillis
import kotlin.math.abs
import kotlin.math.min
import kotlin.system.exitProcess

object App {

    enum class State { MAINTENANCE, GAME }
    private const val MAX_COINS = 99

    var state = State.GAME
        private set

    var CREDITS = 0
    var AMOUNT = 0

    data class Game(
        val number: Int = 0,
        val times: Int = 0,
        val moneyWon: Int = 0,
        val deposit: Int = AMOUNT,
        val gamesPlayed: Int = 0
    )

    var gameStats: MutableList<Game> = mutableListOf()
    var gameInfo: Game = Game()


    val BETS = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D')

    val betMap = mapOf<Int, Bet>(
        1 to Bet(),
        2 to Bet(),
        3 to Bet(),
        4 to Bet(),
        5 to Bet(),
        6 to Bet(),
        7 to Bet(),
        8 to Bet(),
        9 to Bet(),
        10 to Bet(),
        11 to Bet(),
        12 to Bet(),
        13 to Bet(),
        14 to Bet()
    )

    data class Bet(var counter: Int = 0, var bet: Char = ' ')

    fun init() {
        CoinAcceptor.init()
        Maintenance.init()
        TUI.init()
        RouletteDisplay.init()

        gameInfo = CoinDeposit.loadCoin("deposit.txt") // carrega o número de moedas e o jogos realizados
        gameStats = Statistics.loadGameData("statistics.txt").toMutableList() // carrega as estatísticas anteriores
    }

    fun startScreen() {
        TUI.moveCursor(0, 1)
        TUI.write("Roulette Game")
        TUI.moveCursor(1, 1)

        TUI.write('1')
        TUI.moveCursor(1, 4)
        TUI.write('2')

        TUI.moveCursor(1, 8)
        TUI.write('3')

        TUI.moveCursor(1, 12)
        updateCoin()
        TUI.write("$${CREDITS}")
    }

    fun maintenanceMode() {
        state = State.MAINTENANCE

        var lastActiveTime = 0L
        var showFirstLine = true

        TUI.clear()
        TUI.moveCursor(0, 1)
        TUI.write("On Maintenance")

        while (Maintenance.maintenance()) {

            RouletteDisplay.clear()
            if (currentTimeMillis() - lastActiveTime >= 3000) { // Alterna entre as duas linhas a cada 3 segundos
                TUI.moveCursor(1, 1)
                if (showFirstLine) {
                    TUI.write("C-Stats A-Count")
                } else {
                    TUI.clear()
                    TUI.moveCursor(0, 1)
                    TUI.write("On Maintenance")
                    TUI.moveCursor(1, 1)
                    TUI.write("*-Play D-ShutD")
                }
                showFirstLine = !showFirstLine
                lastActiveTime = currentTimeMillis()
            }

            val key = read(100)

            when (key) {
                'C' -> {
                    TUI.clear()
                    Maintenance.statistics()
                    clearStatistics()
                    TUI.moveCursor(0, 1)
                    lastActiveTime = currentTimeMillis() // reset do tempo
                }

                'A' -> {
                    TUI.clear()
                    Maintenance.count()
                    clearCount()
                    TUI.moveCursor(0, 1)
                    lastActiveTime = currentTimeMillis()
                }

                '*' -> {
                    TUI.clear()
                    playOnMaintenanceMode()
                    TUI.clear()
                    TUI.moveCursor(0, 1)
                    TUI.write("On Maintenance")
                    state = State.GAME
                    //return
                }

                'D' -> {
                    shuttingDown()
                }

                //else -> return
            }
        }
        TUI.clear()
        state = State.GAME
    }

    fun clearStatistics() {
        val resetWindowStart = currentTimeMillis()

        while (currentTimeMillis() - resetWindowStart < 5000) {
            val confirmKey = read(100)

            if (confirmKey == '*') {
                TUI.clear()
                TUI.moveCursor(0, 1)
                TUI.write("Clear Stats?")
                TUI.moveCursor(1, 0)
                TUI.write("5-Yes other-No")

                val userKey = read(5000)

                if (userKey == '5') {
                    Statistics.saveGameData(emptyList())
                    TUI.clear()
                    TUI.moveCursor(0, 0)
                    TUI.write("Reset Done")
                    //Time.sleep(2000)
                    await(2000)
                }

                // Volta ao modo de manutenção
                TUI.clear()
                TUI.moveCursor(0, 1)
                TUI.write("On Maintenance")
                break
            }

            if (confirmKey != 0.toChar()) break
        }
    }

    fun clearCount() {
        val resetWindowStart = currentTimeMillis()

        while (currentTimeMillis() - resetWindowStart < 5000) {
            val confirmKey = read(100)

            if (confirmKey == '*') {
                TUI.clear()
                TUI.moveCursor(0, 1)
                TUI.write("Clear Counters?")
                TUI.moveCursor(1, 0)
                TUI.write("5-Yes other-No")

                // Espera a decisão do utilizador
                val userKey = read(5000)

                if (userKey == '5') {
                    AMOUNT = 0
                    gameInfo = gameInfo.copy(gamesPlayed = 0, deposit = AMOUNT)
                    CoinDeposit.saveGameInfo(gameInfo)

                    TUI.clear()
                    TUI.moveCursor(0, 0)
                    TUI.write("Reset Done")
                    //Time.sleep(2000)
                    await(2000)
                }

                // Volta ao modo de manutenção
                TUI.clear()
                TUI.moveCursor(0, 1)
                TUI.write("On Maintenance")
                break
            }

            if (confirmKey != 0.toChar()) break
        }
    }

    fun playOnMaintenanceMode() {
        var currCoins = CREDITS
        CREDITS = 99
        gameView()
        startBet(false)
        CREDITS = currCoins
    }

    fun shuttingDown() {
        while (true) {
            TUI.clear()
            TUI.moveCursor(0, 4)
            TUI.write("Shutdown")
            TUI.moveCursor(1, 0)
            TUI.write("5-Yes  other-No")
            val key = read(5000) // espera 5 segundos para a tecla ser pressionada, caso contrário, volta ao menu de manutenção

            if (key == '5') {
                TUI.off()
                RouletteDisplay.off(true)
                exitProcess(0)
            }
            else {
                TUI.clear()
                return
            }

        }
    }

    fun gameView() {
        TUI.clear()
        TUI.moveCursor(1, 1)
        TUI.write("0123456789ABCD")
    }

    fun read(time: Long): Char = TUI.waitKey(time)


    fun startBet(updateCoinsAndStatistics: Boolean = true) {
        while (true) {
            val key = read(10)

            when (key) {
                in BETS -> processBet(key)
                '#' -> {
                    val winningBet = showResult()
                    processResult(winningBet, updateCoinsAndStatistics)
                    TUI.clear()
                    return
                }
            }
        }
    }


    fun processBet(key: Char) {
        if (key in BETS && betMap[BETS.indexOf(key) + 1]?.counter in 0..8 && CREDITS >= 1) {
            betMap[BETS.indexOf(key) + 1]?.bet = key
            betMap[BETS.indexOf(key) + 1]?.counter++
            TUI.moveCursor(0, betMap.keys.indexOf(BETS.indexOf(key) + 1) + 1)
            TUI.write("${betMap[BETS.indexOf(key) + 1]?.counter}")
            println("Bet = ${betMap[BETS.indexOf(key) + 1]?.bet} com nº de apostas = ${betMap[BETS.indexOf(key) + 1]?.counter}")
            setCreditsOnDisplay(--CREDITS)
            println("Credits = $CREDITS")
        }
    }

    fun processResult(winningBet: Char, updateStatsAndCoins: Boolean = true) {
        val mapWinningBet = when (winningBet) {
            'A' -> 0x0A
            'B' -> 0x0B
            'C' -> 0x0C
            'D' -> 0x0D
            else -> winningBet.digitToInt()
        }


        val betDuration = 5000
        val animationStart = currentTimeMillis()
        val animationDuration = 10000

        while (currentTimeMillis() - animationStart < animationDuration) {
            val elapsedTime = currentTimeMillis() - animationStart
            if (elapsedTime < betDuration ) {
                val key = read(10)
                processBet(key)
            }
            RouletteDisplay.animation()
        }

        val resultAnimationStart = currentTimeMillis()
        val resultDuration = 3000

        while (currentTimeMillis() - resultAnimationStart < resultDuration) {
            RouletteDisplay.resultAnimation()
        }

        val finalAnimationStart = currentTimeMillis()
        val finalDuration = 2000

        while (currentTimeMillis() - finalAnimationStart < finalDuration) {
            RouletteDisplay.finalAnimation()
        }

        await(1000)

        RouletteDisplay.setValue(RouletteDisplay.DISPLAY.FIVE, mapWinningBet)

        var creditsWon = 0
        val match = betMap.values.any { it.bet == winningBet }
        val betCredits = betMap[BETS.indexOf(winningBet) + 1]?.counter ?: 0
        if (match) {
            creditsWon += betCredits * 2
            CREDITS += creditsWon
            setCreditsOnDisplay(betCredits * 2)
            println("You won = ${betCredits * 2}")
        }
        else {
            setCreditsOnDisplay(0)
            println("You don´t won nothing")
        }

        await(5000)
        //Time.sleep(5000) //tempo de espera para voltar ao ecrã principal
        while (TUI.getKey() != 0.toChar()); // Descarta todas as teclas pressionadas entre o fim dos 5 segundos e o sorteio.
        resetBets()

        if (updateStatsAndCoins) {
            gameStats = Statistics.updateGameStats(gameStats, mapWinningBet, creditsWon).toMutableList()
            Statistics.saveGameData(gameStats)
            gameInfo = CoinDeposit.updateGamesPlayed(gameInfo)
        }
    }

    private fun resetBets() {
        for ((_, bet) in betMap) {
            bet.bet = ' '
            bet.counter = 0
        }
    }

    private fun await(timeout: Long, startTime: Long = currentTimeMillis()) {
        while (currentTimeMillis() - startTime < timeout);
    }

    fun showResult(): Char {
        val result = BETS.shuffled().random()
        return result
    }


    private fun setCreditsOnDisplay(value: Int) {
        if (value > 9) {
            RouletteDisplay.setValue(RouletteDisplay.DISPLAY.ONE, value / 10)
            RouletteDisplay.setValue(RouletteDisplay.DISPLAY.ZERO, value % 10)
        } else {
            RouletteDisplay.setValue(RouletteDisplay.DISPLAY.ONE, 0)
            RouletteDisplay.setValue(RouletteDisplay.DISPLAY.ZERO, value)
        }
    }

    fun updateCoin() {
        CoinAcceptor.isCoinInserted().let { coinWasInserted ->
            if (coinWasInserted) {
                val coinInserted = CoinAcceptor.getCoin()
                CREDITS = min(CREDITS + coinInserted.value, MAX_COINS)

                gameInfo = CoinDeposit.add(1, gameInfo)

                CoinAcceptor.sendAccept()
            }
        }
    }

    fun waitForStart() {
        while (true) {
            if (Maintenance.maintenance()) {
                maintenanceMode()
                continue
            }
            startScreen()
            RouletteDisplay.animation()

            val keyPressed = read(100)

            if (keyPressed == '*' && CREDITS >= 2) {
                RouletteDisplay.clear()
                return
            }

            updateCoin()
        }
    }

    fun startGame() {
        gameView()
        startBet()
    }
}



fun main() {
    App.init()
    while (true) {
        if (Maintenance.maintenance()) {
            App.maintenanceMode()
        }
        else {
            App.waitForStart()
            App.startGame()
        }
    }
}
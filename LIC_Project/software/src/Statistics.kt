

object Statistics {


    fun updateGameStats(gameList: MutableList<App.Game>, bet: Int, creditsWon: Int): List<App.Game> {
        val index = gameList.indexOfFirst { it.number == bet }
        return if (index != -1) {
            val game = gameList[index]
            gameList[index] = game.copy(times = game.times + 1, moneyWon = game.moneyWon + creditsWon)
            gameList
        }
        else gameList + App.Game(bet, times = 1, moneyWon = creditsWon)
    }



    fun saveGameData(gameList: List<App.Game>, file: String = "statistics.txt") {
        val lines = gameList.map { "${it.number};${it.times};${it.moneyWon}" }
        FileAccess.write(file, lines)
    }

    fun loadGameData(file: String = "statistics.txt"): List<App.Game> {
        val lines = FileAccess.read(file)
        return lines.mapNotNull { line ->
            val parts = line.split(";")
            if (parts.size == 3) {
                val number = parts[0].toIntOrNull()
                val times = parts[1].toIntOrNull()
                val moneyWon = parts[2].toIntOrNull()
                if (number != null && times != null && moneyWon != null) {
                    App.Game(number, times, moneyWon)
                } else null
            } else null
        }
    }

}

fun main() {
    var game = App.Game()

    val loadedGame = Statistics.loadGameData("statistics.txt")
    game = CoinDeposit.loadCoin("deposit.txt")

    game = CoinDeposit.add(5, game)
    Statistics.saveGameData(listOf(game))

    println("Estado carregado: $game (AMOUNT=${App.AMOUNT})")
}
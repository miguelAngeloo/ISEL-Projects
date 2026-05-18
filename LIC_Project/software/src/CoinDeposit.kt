import App.AMOUNT

object CoinDeposit {


    fun saveGameInfo(game: App.Game, file: String = "deposit.txt") {
        FileAccess.write(file, listOf("${game.gamesPlayed};${game.deposit}"))
    }


    fun add(coin: Int, game: App.Game): App.Game {
        val newDeposit = game.deposit + coin
        AMOUNT = newDeposit
        val updated = game.copy(deposit = newDeposit)
        saveGameInfo(updated)
        return updated
    }

    fun remove(coin: Int, game: App.Game): App.Game {
        AMOUNT -= coin
        val newDeposit = AMOUNT
        val updated = game.copy(deposit = newDeposit)
        saveGameInfo(updated)
        return updated
    }



    fun updateGamesPlayed(game: App.Game): App.Game {
        val updated = game.copy(gamesPlayed = game.gamesPlayed + 1)
        saveGameInfo(updated)
        return updated
    }




    fun loadCoin(file: String = "deposit.txt"): App.Game {
        val lines = FileAccess.read(file)

        if (lines.isNotEmpty()) {
            val parts = lines.first().split(";")
            if (parts.size == 2) {
                val loadGamesPlayed = parts[0].toIntOrNull()
                val loadDeposit = parts[1].toIntOrNull()
                if (loadGamesPlayed != null && loadDeposit != null) {
                    AMOUNT = loadDeposit
                    return App.Game(gamesPlayed = loadGamesPlayed, deposit = AMOUNT)
                }
            }
        }
        return App.Game()
    }
}



fun main() {
    val game = App.Game(0, 1, 2, 0, 9)
    CoinDeposit.add(2, game)
    CoinDeposit.add(4, game)
    CoinDeposit.remove(2, game)
    println(AMOUNT)
}
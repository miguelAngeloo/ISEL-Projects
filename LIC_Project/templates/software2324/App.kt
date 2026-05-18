import isel.leic.utils.Time
// App Space Invaders
object APP {
    const val INVADER = "0123456789"

    enum class Mode {
        GAME, MAINTENANCE
    }

    var mode = Mode.GAME

    fun init() {
        TUI.init()
        ScoreDisplay.init()
        Scores.start()
    }

    var aim:Char = 0.toChar()
    fun aim(sel: Char, l: Int) {
        aim = sel
        TUI.cursor(l, 1)
        TUI.write(sel)
    }

    fun correctAim(keyPressed: Char, inv: String): Boolean {
        if (keyPressed == '#') {
            if (inv[0] == aim) {
                return true
            }
        }
        return false
    }

    fun switch(l: Int) {
        when (l) {
            1 -> {
                TUI.cursor(2, 1)
                TUI.write("]")
                TUI.cursor(2, 2)
                TUI.write(" ")
                TUI.cursor(1, 2)
                TUI.write(">")
                aim = 0.toChar()
            }

            2 -> {
                TUI.cursor(1, 1)
                TUI.write("]")
                TUI.cursor(1, 2)
                TUI.write(" ")
                TUI.cursor(2, 2)
                TUI.write(">")
                aim = 0.toChar()
            }
        }
    }

    // Corrigir erro:
    // Quando a linha só possui 1 caractere, o programa não consegue remover o invader
    // Out of bounds exception after drop(1)
    fun gameMode(score:Boolean) {
        var l1:String       // Caracteres da linha 1
        var l2:String       // Caracteres da linha 2
        var c1:Int          // Posição atual do cursor na linha 1 (16 - c1)
        var c2:Int          // Posição atual do cursor na linha 2 (16 - c2)

        var inv: Int        // Caracter a inserir
        var invaderLine:Int // Linha do Invader

        var keyPressed:Char // Tecla premida
        var currLine:Int    // Linha atual

        var prevTime = Time.getTimeInMillis()
        var currTime: Long

        l1 = " "
        l2 = " "
        c1 = 16
        c2 = 16
        currLine = 1

        ScoreDisplay.off(!score)
        // Preparação para o Início do Jogo
        TUI.clear()
        setStart()

        // Início do Jogo
        while (c1 > 1 && c2 > 1) {
            // Leitura da tecla premida
            keyPressed = read(500)

            // Alternar entre linhas
            if (keyPressed == '*') {
                if (currLine == 1) {
                    switch(2)
                    currLine = 2
                } else {
                    switch(1)
                    currLine = 1
                }
            }
            else {
                if (keyPressed != 0.toChar() && keyPressed != '#') {
                    aim(keyPressed, currLine)
                }
                // Avaliação da tecla premida
                if (currLine == 1) {
                    if (correctAim(keyPressed, l1)) {
                        deleteInvader(1, c1++, l1)
                        l1 = removeInvader(l1)
                        APP.score++
                    }
                } else {
                    if (correctAim(keyPressed, l2)) {
                        deleteInvader(2, c2++, l2)
                        l2 = removeInvader(l2)
                        APP.score++
                    }
                }
            }

            // Avaliação do tempo entre Invaders
            currTime = Time.getTimeInMillis()
            if (timeToInvade(currTime, prevTime)) {
                // Atualização do Tempo
                prevTime = currTime

                invaderLine = (1..2).random()

                // Inserção de um Invader
                inv = (0..9).random()

                // Adiciona o Invader à linha correspondente e escreve-o
                if (invaderLine == 1) {
                    l1 += INVADER[inv]
                    if (!l1.isEmpty() && l1 != " ")
                        writeInvaders(1, c1--, l1)
                } else {
                    l2 += INVADER[inv]
                    if (!l2.isEmpty() && l2 != " ")
                        writeInvaders(2, c2--, l2)
                }

                // Rever este trecho
                //--------------------------------------------
                updateScreen(1, c1, l1)
                updateScreen(2, c2, l2)
                //--------------------------------------------
                updateScore()
            }
        }
        // Fim do Jogo
        gameOver()
        // Retorno ao menu inicial
        logotype()
    }

    // Funções que operam sobre o ecrã
    //--------------------------------------------
    fun setStart() {
        setBackground()

        TUI.writeAt(1, 2, ">")
        Time.sleep(1)
    }

    fun setBackground() {
        for (i in 1..2) {
            TUI.writeLeft(i, "]")
        }
    }

    fun logotype() {
        TUI.writeAt(1, 2, "Space Invaders")
        TUI.writeAt(2, 2, "Press #")
        showCoins(coins)
    }

    fun updateScreen(l:Int, c:Int, inv:String) {
        TUI.writeAt(l, c, inv)
    }

    fun gameOver() {
        Time.sleep(1000)
        TUI.clear()
        TUI.cursor(1, 4)
        TUI.write("GAME  OVER")
        Time.sleep(5000)
        TUI.clear()
        TUI.writeAt(2, 1, "Score:$score")
        val name = getName()
        Scores.scoreList.add(Pair(score, name))
        Time.sleep(1000)
        TUI.clear()
        logotype()
    }

    fun getName():String {
        TUI.writeAt(1, 1, "Name:")
        var c = 6
        var letter = 'A'
        TUI.writeAt(1, c, "$letter")
        var name = "          "
        var keyPressed: Char
        while (true) {
            keyPressed = read(500)
            val action = analyseKey(keyPressed)
            when (action) {
                0 -> {
                    name += letter
                    break
                }
                -2 -> {
                    if (letter > 'A') {
                        letter = letter - 1
                        TUI.writeAt(1, c, "$letter")
                    }
                }

                2 -> {
                    if (letter < 'Z') {
                        letter = letter + 1
                        TUI.writeAt(1, c, "$letter")
                    }
                }

                -1 -> {
                    if (c > 6) {
                        name = name.dropLast(1)
                        c--
                        letter = 'A'
                        TUI.writeAt(1, c, "$letter ")
                    }
                }

                1 -> {
                    if (c < 17) {
                        name += letter
                        c++
                        letter = 'A'
                        TUI.writeAt(1, c, "$letter")
                    }
                }
                else -> null
            }
        }
        return name
    }
    fun analyseKey(keyPressed:Char):Int {
        when(keyPressed) {
            '2' -> return -2
            '4' -> return -1
            '5' -> return 0
            '6' -> return 1
            '8' -> return 2
            else -> return -5
        }
    }
    //--------------------------------------------

    // Score
    //--------------------------------------------
    var score = 0
    fun updateScore() {
        var score = score
        var digit = 0
        for (i in 0..5) {
            digit = score % 10
            score /= 10
            ScoreDisplay.setScore((digit shl 3) or i )
        }
    }

    var pos = 0
    fun showScore() {
        var scores = Scores.scoreList
        TUI.clearLine(2)
        if (!scores.isEmpty()) {
            val score = scores[pos].first
            val name = scores[pos].second
            val txt = "$score-$name"
            TUI.writeAt(2, 2, txt)
            pos++
        }
        if (pos >= scores.size) pos = 0
        Time.sleep(1000)
        showCoins(coins)
    }
    //--------------------------------------------
    // Moedas
    //--------------------------------------------
    var coins = Statistics.getStats()[1]
    fun showCoins(c: Int) {
        val column = if (c >= 10) 1 else 0
        TUI.writeRight(2, 3 + column, "$" + c.toString())
    }

    fun updateCoin() {
            if (CoinAcceptor.isCoin()) {
                coins += 2
                showCoins(coins)
                while (CoinAcceptor.isCoin()) {
                    CoinAcceptor.acceptCoin(true)
                }
                CoinAcceptor.acceptCoin(false)
            }
            else {
                showCoins(coins)
            }
    }
    //--------------------------------------------

    // Invaders
    //--------------------------------------------
    const val TIME_INVADER = 2000
    fun timeToInvade(curr:Long, prev:Long):Boolean {
        return curr - prev > TIME_INVADER
    }

    fun removeInvader(l:String):String {
        if (l.length > 1) return l.drop(1)
        else return " "
    }

    fun deleteInvader(l: Int, c: Int, inv: String) {
        TUI.writeAt(l, c, " " + inv)
    }

    fun writeInvaders(l:Int, c:Int, inv:String) {
        TUI.writeAt(l, c, inv)
    }
    //--------------------------------------------

    // Leitura de teclas
    fun read(time: Long): Char {
        return TUI.waitKey(time)
    }
    //--------------------------------------------

    // Modo de Manutenção
    fun mtMode():Boolean {
        var mtCheck:Boolean
        var cursorPrev:Int
        var cursor = 0
        var keyPressed: Char

        TUI.clearLine(1)
        mtMainMenu()
        while (true) {
            TUI.clearLine(2)
            showOpt(cursor)

            while (cursor in 0..2) {
                mtCheck = M.mtCheck()
                if (!mtCheck) {
                    M.prevMode = false
                    break
                }
                keyPressed = TUI.waitKey(50)
                cursorPrev = cursor
                cursor += if (analyseKey(keyPressed) > -3 && analyseKey(keyPressed) < 3) analyseKey(keyPressed) else 0
                if (cursorPrev != cursor) {
                    TUI.clearLine(2)
                    showOpt(cursor)
                }
                if (cursor == 2 && keyPressed == '5') {
                    M.prevMode = false
                    break
                }
            }
            if (!M.prevMode) {
                println("Over")
                return false
            }
        }
    }
    val OPTIONS = arrayOf("1. Test Game", "2. Show Scores", "3. Turn Game Off")

    fun mtMainMenu() {
        TUI.writeAt(1, 1, "Maintenance Mode")
    }

    fun showOpt(opt:Int) {
        if (opt < 0 || opt > 2) return
        TUI.writeLeft(2, OPTIONS[opt])
    }

}

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
                mtMode = true
                mtMode = APP.mtMode()
            }
            currTime - prevTime > 5000 -> {
                APP.showScore()
                prevTime = currTime
            }

            else -> {
                if (!mtMode) {
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

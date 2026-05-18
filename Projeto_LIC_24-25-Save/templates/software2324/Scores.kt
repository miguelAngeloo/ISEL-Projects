// Object Scores
object Scores {
    var scoreList = mutableListOf<Pair<Int, String>>()

    fun getScores(): MutableList<Pair<Int, String>>{
        val score = FileAccess.readScores("scores.txt")
        return score
    }

    fun writeScore(scores: MutableList<Pair<Int, String>>){
        FileAccess.writeScores("scores.txt", scores)
    }

    fun start() {
        scoreList = getScores()
    }
}
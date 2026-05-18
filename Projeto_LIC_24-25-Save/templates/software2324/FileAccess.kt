import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
// Object File Access
object FileAccess{
    fun readScores(name:String):MutableList<Pair<Int, String>>{
        val scoreList = mutableListOf<Pair<Int, String>>()
        val reader = BufferedReader(FileReader(name))
        var line = reader.readLine()
        while (line != null) {
            val parts = line.split(";")
            var name = parts[1]
            while (name.first() == ' ') {
                name = name.drop(1)
            }
            val score = parts[0].toIntOrNull()
            if (score != null) {
                scoreList.add(Pair(score, name))
            }
            line = reader.readLine()
        }
        reader.close()
        scoreList.sortBy { it.first }
        return scoreList
    }

    fun writeScores(name:String, scores:MutableList<Pair<Int, String>>){
        val writer = FileWriter(name)
        for (score in scores) {
            val points = score.first
            var name:String = score.second
            while (name.first() == ' ') {
                name = name.drop(1)
            }
            writer.write("${points};${name}\n")
        }
        writer.close()
    }

    fun readStatistics(name:String):MutableList<Int> {
        val statsList = mutableListOf<Int>()
        val reader = BufferedReader(FileReader(name))
        var line = reader.readLine()
        while (line != null) {
            val stat = line.toIntOrNull()
            if (stat != null) {
                statsList.add(stat)
            }
            line = reader.readLine()
        }
        reader.close()

        return statsList
    }

    fun writeStatistics(name:String, stats:MutableList<Int>){
        val writer = FileWriter(name)
        for (stat in stats) {
            writer.write("$stat\n")
        }
        writer.close()
    }
}

fun main() {
    val scores = FileAccess.readScores("scores.txt")
    for (score in scores) {
        println("${score.first} - ${score.second}")
    }
    FileAccess.writeScores("scores.txt", mutableListOf(Pair(100, "Ana"), Pair(200, "João")))
    println("----------------")
    val stats = FileAccess.readStatistics("statistics.txt")
    for (stat in stats) {
        println(stat)
    }
    FileAccess.writeStatistics("statistics.txt", mutableListOf(1, 2, 3, 4, 5))
}
// Object Statistics
object Statistics{
    fun getStats():MutableList<Int> {
        val stats = FileAccess.readStatistics("statistics.txt")
        return stats
    }

    fun setStats(stats:MutableList<Int>) {
        FileAccess.writeStatistics("statistics.txt", stats)
    }
}
import java.io.BufferedReader
import java.io.FileReader
import java.io.PrintWriter

object FileAccess {


    fun createReader(fileName: String): BufferedReader {
        return BufferedReader(FileReader(fileName))
    }

    fun createWriter(fileName: String): PrintWriter {
        return PrintWriter(fileName)
    }


    fun write(file: String, lines: List<String>) {
        val pw = createWriter(file)
        pw.use { writer -> lines.forEach { writer.println(it) } }
        pw.close()
    }
    
    
    fun read(file: String): List<String> {
        val result = mutableListOf<String>()
        val br = createReader(file)
            br.use { reader ->
            var line = br.readLine()

            while (line != null) {
                result.add(line)
                line = reader.readLine()
            }
        }
        br.close()
        return result
    }
}
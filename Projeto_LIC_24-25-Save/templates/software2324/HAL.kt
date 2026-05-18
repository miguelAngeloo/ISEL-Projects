import isel.leic.UsbPort
// Hardware Abstraction Layer
object HAL {
    var l_w : Int = 0

    fun init() {
        UsbPort.write(0)
    }

    fun readBits(mask:Int):Int {
        val comp = UsbPort.read()
        val ans = comp and mask
        return ans
    }

    fun isBit(mask:Int):Boolean {
        val comp = UsbPort.read()
        val ans = comp and mask
        return if (ans != 0) true else false
    }

    fun setBits(mask:Int) {
        val ans = l_w or(mask)
        UsbPort.write(ans)
        l_w = ans
    }

    fun clrBits(mask:Int){
        val ans = l_w and(mask.inv())
        UsbPort.write(ans)
        l_w = ans
    }

    fun writeBits(mask:Int, value:Int){
        clrBits(mask)
        setBits(value)
    }
}

fun main() {
    HAL.init()
    println("Teste isBit")
    val a = 4
    println(HAL.isBit(a))
    println("Teste setBits")
}
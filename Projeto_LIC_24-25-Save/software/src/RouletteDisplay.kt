
import javax.swing.event.InternalFrameAdapter
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit


// controla o mostrador de pontuação
object RouletteDisplay {

    enum class DISPLAY(val cmd: Int) { ZERO(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5) }
    // Toggle ON and OFF and UPDATE Display
    const val ON = 0b0000_0111
    const val OFF = 0b0000_1111
    const val UPDATE = 0b0000_0110
    const val CLEAR = 0x1F
    private const val DISPLAY_SIZE = 6 // Número de dígitos no mostrador da roleta

    // inicia a classe, estabelecendo os valores iniciais
    fun init() {
        SerialEmitter.init()
        off(false)
        setValue(0b0000_0000)
        setValue(0b0000_0001)
        setValue(0b0000_0010)
        setValue(0b0000_0011)
        setValue(0b0000_0100)
        setValue(0b0000_0101)
    }

    private val FRAME_INTERVAL = 87 // Intervalo entre frames da animação em milissegundos (Implica que uma animação completa leva aproximadamente 5 segundos)
    private val UPDATE_TIME = FRAME_INTERVAL.milliseconds.toLong(DurationUnit.MILLISECONDS)

    private val FRAME_RESULT_INTERVAL = 250
    private val UPDATE_RESULT_TIME = FRAME_RESULT_INTERVAL.milliseconds.toLong(DurationUnit.MILLISECONDS)

    private var lastChangedTime: Long = 0

    private data class Frame(
        val Display: DISPLAY,
        val value: Int,
    )

    // 57 Frames de animação
    private val ANIMATION_FRAMES = arrayOf<Frame>(
        Frame(DISPLAY.ZERO, 0x11),
        Frame(DISPLAY.ONE, 0x11),
        Frame(DISPLAY.TWO, 0x11),
        Frame(DISPLAY.THREE, 0x11),
        Frame(DISPLAY.FOUR, 0x11),
        Frame(DISPLAY.FIVE, 0x11),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x12),
        Frame(DISPLAY.ONE, 0x12),
        Frame(DISPLAY.TWO, 0x12),
        Frame(DISPLAY.THREE,0x12),
        Frame(DISPLAY.FOUR, 0x12),
        Frame(DISPLAY.FIVE, 0x12),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x13),
        Frame(DISPLAY.ONE, 0x13),
        Frame(DISPLAY.TWO, 0x13),
        Frame(DISPLAY.THREE,0x13),
        Frame(DISPLAY.FOUR, 0x13),
        Frame(DISPLAY.FIVE, 0x13),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x14),
        Frame(DISPLAY.ONE, 0x14),
        Frame(DISPLAY.TWO, 0x14),
        Frame(DISPLAY.THREE,0x14),
        Frame(DISPLAY.FOUR, 0x14),
        Frame(DISPLAY.FIVE, 0x14),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x15),
        Frame(DISPLAY.ONE, 0x15),
        Frame(DISPLAY.TWO, 0x15),
        Frame(DISPLAY.THREE,0x15),
        Frame(DISPLAY.FOUR, 0x15),
        Frame(DISPLAY.FIVE, 0x15),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x16),
        Frame(DISPLAY.ONE, 0x16),
        Frame(DISPLAY.TWO, 0x16),
        Frame(DISPLAY.THREE,0x16),
        Frame(DISPLAY.FOUR, 0x16),
        Frame(DISPLAY.FIVE, 0x16),
        //------------------------------------  Novos frames
        Frame(DISPLAY.ZERO, 0x11),
        Frame(DISPLAY.ONE, 0x11),
        Frame(DISPLAY.TWO, 0x11),
        Frame(DISPLAY.THREE, 0x11),
        Frame(DISPLAY.FOUR, 0x11),
        Frame(DISPLAY.FIVE, 0x11),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x12),
        Frame(DISPLAY.ONE, 0x12),
        Frame(DISPLAY.TWO, 0x12),
        Frame(DISPLAY.THREE,0x12),
        Frame(DISPLAY.FOUR, 0x12),
        Frame(DISPLAY.FIVE, 0x12),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x13),
        Frame(DISPLAY.ONE, 0x13),
        Frame(DISPLAY.TWO, 0x13),
        Frame(DISPLAY.THREE,0x13),
        Frame(DISPLAY.FOUR, 0x13),
        Frame(DISPLAY.FIVE, 0x13),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x14),
        Frame(DISPLAY.ONE, 0x14),
        Frame(DISPLAY.TWO, 0x14),
        Frame(DISPLAY.THREE,0x14),
        Frame(DISPLAY.FOUR, 0x14),
        Frame(DISPLAY.FIVE, 0x14),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x15),
        Frame(DISPLAY.ONE, 0x15),
        Frame(DISPLAY.TWO, 0x15),
        Frame(DISPLAY.THREE,0x15),
        Frame(DISPLAY.FOUR, 0x15),
        Frame(DISPLAY.FIVE, 0x15),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x16),
        Frame(DISPLAY.ONE, 0x16),
        Frame(DISPLAY.TWO, 0x16),
        Frame(DISPLAY.THREE,0x16),
        Frame(DISPLAY.FOUR, 0x16),
        Frame(DISPLAY.FIVE, 0x16),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x11),
        Frame(DISPLAY.ONE, 0x11),
        Frame(DISPLAY.TWO, 0x11),
        Frame(DISPLAY.THREE, 0x11),
        Frame(DISPLAY.FOUR, 0x11),
        Frame(DISPLAY.FIVE, 0x11),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x12),
        Frame(DISPLAY.ONE, 0x12),
        Frame(DISPLAY.TWO, 0x12),
        Frame(DISPLAY.THREE,0x12),
        Frame(DISPLAY.FOUR, 0x12),
        Frame(DISPLAY.FIVE, 0x12),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x13),
        Frame(DISPLAY.ONE, 0x13),
        Frame(DISPLAY.TWO, 0x13),
        Frame(DISPLAY.THREE,0x13),
        Frame(DISPLAY.FOUR, 0x13),
        Frame(DISPLAY.FIVE, 0x13),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x14),
        Frame(DISPLAY.ONE, 0x14),
        Frame(DISPLAY.TWO, 0x14),
        Frame(DISPLAY.THREE,0x14),
        Frame(DISPLAY.FOUR, 0x14),
        Frame(DISPLAY.FIVE, 0x14),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x15),
        Frame(DISPLAY.ONE, 0x15),
        Frame(DISPLAY.TWO, 0x15),
        Frame(DISPLAY.THREE,0x15),
        Frame(DISPLAY.FOUR, 0x15),
        Frame(DISPLAY.FIVE, 0x15),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x16),
        Frame(DISPLAY.ONE, 0x16),
        Frame(DISPLAY.TWO, 0x16),
        Frame(DISPLAY.THREE,0x16),
        Frame(DISPLAY.FOUR, 0x16),
        Frame(DISPLAY.FIVE, 0x16),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x11),
        Frame(DISPLAY.ONE, 0x11),
        Frame(DISPLAY.TWO, 0x11),
        Frame(DISPLAY.THREE,0x11),
        Frame(DISPLAY.FOUR, 0x11),
        Frame(DISPLAY.FIVE, 0x11),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x19),
        Frame(DISPLAY.ONE, 0x19),
        Frame(DISPLAY.TWO, 0x19),
        Frame(DISPLAY.THREE,0x19),
        Frame(DISPLAY.FOUR, 0x19),
        Frame(DISPLAY.FIVE, 0x19),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x19),
        Frame(DISPLAY.THREE,0x19),
        Frame(DISPLAY.FOUR, 0x19),
        Frame(DISPLAY.FIVE, 0x19),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x19),
        Frame(DISPLAY.FIVE, 0x19),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x12),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x13),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x14),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1c),
        Frame(DISPLAY.FIVE, 0x1c),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1c),
        Frame(DISPLAY.THREE,0x1c),
        Frame(DISPLAY.FOUR, 0x1c),
        Frame(DISPLAY.FIVE, 0x1c),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1c),
        Frame(DISPLAY.ONE, 0x1c),
        Frame(DISPLAY.TWO, 0x1c),
        Frame(DISPLAY.THREE,0x1c),
        Frame(DISPLAY.FOUR, 0x1c),
        Frame(DISPLAY.FIVE, 0x1c),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1c),
        Frame(DISPLAY.ONE, 0x1c),
        Frame(DISPLAY.TWO, 0x1c),
        Frame(DISPLAY.THREE,0x1c),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1c),
        Frame(DISPLAY.ONE, 0x1c),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x15),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x16),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x11),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x19),
        Frame(DISPLAY.ONE, 0x19),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x19),
        Frame(DISPLAY.ONE, 0x19),
        Frame(DISPLAY.TWO, 0x19),
        Frame(DISPLAY.THREE,0x19),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x19),
        Frame(DISPLAY.ONE, 0x19),
        Frame(DISPLAY.TWO, 0x19),
        Frame(DISPLAY.THREE,0x19),
        Frame(DISPLAY.FOUR, 0x19),
        Frame(DISPLAY.FIVE, 0x19),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x19),
        Frame(DISPLAY.THREE,0x19),
        Frame(DISPLAY.FOUR, 0x19),
        Frame(DISPLAY.FIVE, 0x19),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x19),
        Frame(DISPLAY.FIVE, 0x19),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x12),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x13),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x14),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1c),
        Frame(DISPLAY.FIVE, 0x1c),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1f),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1c),
        Frame(DISPLAY.THREE,0x1c),
        Frame(DISPLAY.FOUR, 0x1c),
        Frame(DISPLAY.FIVE, 0x1c),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1c),
        Frame(DISPLAY.ONE, 0x1c),
        Frame(DISPLAY.TWO, 0x1c),
        Frame(DISPLAY.THREE,0x1c),
        Frame(DISPLAY.FOUR, 0x1c),
        Frame(DISPLAY.FIVE, 0x1c),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1c),
        Frame(DISPLAY.ONE, 0x1c),
        Frame(DISPLAY.TWO, 0x1c),
        Frame(DISPLAY.THREE,0x1c),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x1c),
        Frame(DISPLAY.ONE, 0x1c),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x15),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x16),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x11),
        Frame(DISPLAY.ONE, 0x1f),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x19),
        Frame(DISPLAY.ONE, 0x19),
        Frame(DISPLAY.TWO, 0x1f),
        Frame(DISPLAY.THREE,0x1f),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x19),
        Frame(DISPLAY.ONE, 0x19),
        Frame(DISPLAY.TWO, 0x19),
        Frame(DISPLAY.THREE,0x19),
        Frame(DISPLAY.FOUR, 0x1f),
        Frame(DISPLAY.FIVE, 0x1f),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x19),
        Frame(DISPLAY.ONE, 0x19),
        Frame(DISPLAY.TWO, 0x19),
        Frame(DISPLAY.THREE,0x19),
        Frame(DISPLAY.FOUR, 0x19),
        Frame(DISPLAY.FIVE, 0x19),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x12),
        Frame(DISPLAY.ONE, 0x12),
        Frame(DISPLAY.TWO, 0x12),
        Frame(DISPLAY.THREE,0x12),
        Frame(DISPLAY.FOUR, 0x12),
        Frame(DISPLAY.FIVE, 0x12),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x13),
        Frame(DISPLAY.ONE, 0x13),
        Frame(DISPLAY.TWO, 0x13),
        Frame(DISPLAY.THREE,0x13),
        Frame(DISPLAY.FOUR, 0x13),
        Frame(DISPLAY.FIVE, 0x13),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x14),
        Frame(DISPLAY.ONE, 0x14),
        Frame(DISPLAY.TWO, 0x14),
        Frame(DISPLAY.THREE,0x14),
        Frame(DISPLAY.FOUR, 0x14),
        Frame(DISPLAY.FIVE, 0x14),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x15),
        Frame(DISPLAY.ONE, 0x15),
        Frame(DISPLAY.TWO, 0x15),
        Frame(DISPLAY.THREE,0x15),
        Frame(DISPLAY.FOUR, 0x15),
        Frame(DISPLAY.FIVE, 0x15),
        //------------------------------------
        Frame(DISPLAY.ZERO, 0x16),
        Frame(DISPLAY.ONE, 0x16),
        Frame(DISPLAY.TWO, 0x16),
        Frame(DISPLAY.THREE,0x16),
        Frame(DISPLAY.FOUR, 0x16),
        Frame(DISPLAY.FIVE, 0x16),
    )

    private val RESULT_FRAMES = arrayOf<Frame>(
        Frame(DISPLAY.ZERO, 0x00),
        Frame(DISPLAY.ONE, 0x00),
        Frame(DISPLAY.TWO, 0x00),
        Frame(DISPLAY.THREE, 0x00),
        Frame(DISPLAY.FOUR, 0x00),
        Frame(DISPLAY.FIVE, 0x00),
        //------------------------

        Frame(DISPLAY.ZERO, 0x01),
        Frame(DISPLAY.ONE, 0x01),
        Frame(DISPLAY.TWO, 0x01),
        Frame(DISPLAY.THREE, 0x01),
        Frame(DISPLAY.FOUR, 0x01),
        Frame(DISPLAY.FIVE, 0x01),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x02),
        Frame(DISPLAY.ONE, 0x02),
        Frame(DISPLAY.TWO, 0x02),
        Frame(DISPLAY.THREE, 0x02),
        Frame(DISPLAY.FOUR, 0x02),
        Frame(DISPLAY.FIVE, 0x02),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x03),
        Frame(DISPLAY.ONE, 0x03),
        Frame(DISPLAY.TWO, 0x03),
        Frame(DISPLAY.THREE, 0x03),
        Frame(DISPLAY.FOUR, 0x03),
        Frame(DISPLAY.FIVE, 0x03),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x04),
        Frame(DISPLAY.ONE, 0x04),
        Frame(DISPLAY.TWO, 0x04),
        Frame(DISPLAY.THREE, 0x04),
        Frame(DISPLAY.FOUR, 0x04),
        Frame(DISPLAY.FIVE, 0x04),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x05),
        Frame(DISPLAY.ONE, 0x05),
        Frame(DISPLAY.TWO, 0x05),
        Frame(DISPLAY.THREE, 0x05),
        Frame(DISPLAY.FOUR, 0x05),
        Frame(DISPLAY.FIVE, 0x05),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x06),
        Frame(DISPLAY.ONE, 0x06),
        Frame(DISPLAY.TWO, 0x06),
        Frame(DISPLAY.THREE, 0x06),
        Frame(DISPLAY.FOUR, 0x06),
        Frame(DISPLAY.FIVE, 0x06),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x07),
        Frame(DISPLAY.ONE, 0x07),
        Frame(DISPLAY.TWO, 0x07),
        Frame(DISPLAY.THREE, 0x07),
        Frame(DISPLAY.FOUR, 0x07),
        Frame(DISPLAY.FIVE, 0x07),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x08),
        Frame(DISPLAY.ONE, 0x08),
        Frame(DISPLAY.TWO, 0x08),
        Frame(DISPLAY.THREE, 0x08),
        Frame(DISPLAY.FOUR, 0x08),
        Frame(DISPLAY.FIVE, 0x08),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x09),
        Frame(DISPLAY.ONE, 0x09),
        Frame(DISPLAY.TWO, 0x09),
        Frame(DISPLAY.THREE, 0x09),
        Frame(DISPLAY.FOUR, 0x09),
        Frame(DISPLAY.FIVE, 0x09),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x0A),
        Frame(DISPLAY.ONE, 0x0A),
        Frame(DISPLAY.TWO, 0x0A),
        Frame(DISPLAY.THREE, 0x0A),
        Frame(DISPLAY.FOUR, 0x0A),
        Frame(DISPLAY.FIVE, 0x0A),

        //----------------------
        Frame(DISPLAY.ZERO, 0x0B),
        Frame(DISPLAY.ONE, 0x0B),
        Frame(DISPLAY.TWO, 0x0B),
        Frame(DISPLAY.THREE, 0x0B),
        Frame(DISPLAY.FOUR, 0x0B),
        Frame(DISPLAY.FIVE, 0x0B),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x0C),
        Frame(DISPLAY.ONE, 0x0C),
        Frame(DISPLAY.TWO, 0x0C),
        Frame(DISPLAY.THREE, 0x0C),
        Frame(DISPLAY.FOUR, 0x0C),
        Frame(DISPLAY.FIVE, 0x0C),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x0D),
        Frame(DISPLAY.ONE, 0x0D),
        Frame(DISPLAY.TWO, 0x0D),
        Frame(DISPLAY.THREE, 0x0D),
        Frame(DISPLAY.FOUR, 0x0D),
        Frame(DISPLAY.FIVE, 0x0D),
    )

    private val FINAL_FRAMES = arrayOf<Frame>(
        Frame(DISPLAY.ZERO, 0x10),
        Frame(DISPLAY.ONE, 0x10),
        Frame(DISPLAY.TWO, 0x10),
        Frame(DISPLAY.THREE, 0x10),
        Frame(DISPLAY.FOUR, 0x10),
        Frame(DISPLAY.FIVE, 0x10),

        //-----------------------
        Frame(DISPLAY.ZERO, 0x1F),
        Frame(DISPLAY.ONE, 0x1F),
        Frame(DISPLAY.TWO, 0x1F),
        Frame(DISPLAY.THREE, 0x1F),
        Frame(DISPLAY.FOUR, 0x1F),
        Frame(DISPLAY.FIVE, 0x1F),

    )


    private const val FRAME_SIZE = DISPLAY_SIZE
    private var currFrameIdx = 0
    private var idx = 0
    private var finalIdx = 0

    // Realiza a animação do sorteio
    fun animation() {
        val currTime = System.currentTimeMillis()
        if (currTime - lastChangedTime > UPDATE_TIME) {
            lastChangedTime = currTime
            var frame: Frame
            for (i in 0 until FRAME_SIZE) {
                frame = ANIMATION_FRAMES[currFrameIdx++]
                setValue(frame.Display, frame.value)
            }
            currFrameIdx %= ANIMATION_FRAMES.size
            update()
        }
    }

    fun resultAnimation() {
        val currTime = System.currentTimeMillis()
        if (currTime - lastChangedTime > UPDATE_RESULT_TIME) {
            lastChangedTime = currTime
            var frame: Frame
            for (i in 0 until FRAME_SIZE) {
                frame = RESULT_FRAMES[idx++]
                setValue(frame.Display, frame.value)
            }
            idx %= RESULT_FRAMES.size
            update()
        }
    }


    fun finalAnimation() {
        val currTime = System.currentTimeMillis()
        if (currTime - lastChangedTime > UPDATE_RESULT_TIME) {
            lastChangedTime = currTime
            var frame: Frame
            for(i in 0 until FRAME_SIZE) {
                frame = FINAL_FRAMES[finalIdx++]
                setValue(frame.Display, frame.value)
            }
            finalIdx %= FINAL_FRAMES.size
            update()
        }
    }

    fun clear() {
        for (i in DISPLAY.entries) {
            setValue(i, CLEAR)
        }
    }

    fun update() {
        SerialEmitter.send(SerialEmitter.Destination.Roulette, UPDATE, 8)
    }

    fun setValue(value: Int) {
        SerialEmitter.send(SerialEmitter.Destination.Roulette, value, 8)
        update()
    }

    fun setValue(display: DISPLAY, value: Int) =
        setValue((value shl 3) + display.cmd)

    // Envia comando para ativar / desativar a visualização do mostrador da roleta
    fun off(value: Boolean) = if (value) setValue(OFF) else setValue(ON)
}

fun main() {
    RouletteDisplay.init()
    while (true) {
        RouletteDisplay.animation()
        //RouletteDisplay.resultAnimation()
        //RouletteDisplay.finalAnimation()
    }
}
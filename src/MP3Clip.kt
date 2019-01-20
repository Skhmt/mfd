import java.io.File
import javax.sound.sampled.*
import javax.sound.sampled.FloatControl

class MP3Clip {
    private val clip: Clip = AudioSystem.getClip()
    private lateinit var ain: AudioInputStream
    private lateinit var din: AudioInputStream

    init {
        clip.addLineListener {
            if (it.type == LineEvent.Type.STOP) {
                clip.close()
            }
            else if (it.type == LineEvent.Type.CLOSE) {
                din.close()
                ain.close()
            }
        }
    }

    constructor()

    constructor(file: File) {
        play(file)
    }

    fun play(file: File) {
        clip.close()

        ain = AudioSystem.getAudioInputStream(file)
        val baseFormat = ain.format
        val decodedFormat = AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.sampleRate,
                16,
                baseFormat.channels,
                baseFormat.channels * 2,
                baseFormat.sampleRate,
                false)
        din = AudioSystem.getAudioInputStream(decodedFormat, ain)

        clip.open(din)
        clip.start()
    }

    fun stop() { clip.close() }

    var vol: Int
        get() {
            val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            return (Math.pow(10.0, (gainControl.value / 20.0))*100).toInt()
        }
        set(value) {
            val finalVol = when {
                value <= 0 -> 0.0f
                value >= 100 -> 1.0f
                else -> value / 100.0f
            }

            val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            gainControl.value = 20f * Math.log10(finalVol.toDouble()).toFloat()
        }
}
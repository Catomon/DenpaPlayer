package audio

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.thread

class DenpaStream(
    val stream: AudioInputStream,
    private val audioFormat: AudioFormat = stream.format
) {
    private val dataLineInfo = DataLine.Info(SourceDataLine::class.java, audioFormat)
    private val line: SourceDataLine?
        get() =
            if (AudioSystem.isLineSupported(dataLineInfo))
                AudioSystem.getLine(dataLineInfo) as SourceDataLine?
            else
                null

    var stop = false

    init {
        thread {
            while (true) {
                Thread.sleep(1000)
                while (!AudioSystem.isLineSupported(dataLineInfo)) {
                    Thread.sleep(1000)
                }

                val line: SourceDataLine = line ?: continue
                line.open(audioFormat)
                line.start()

                var size: Int
                val buf = ByteArray(audioFormat.channels * 960 * 2)
                while (line.isOpen) {
                    size = stream.read(buf)
                    if (size >= 0) {
                        line.write(buf, 0, size)
                    }

                    if (stop) return@thread
                }
            }
        }.apply {
            name = "PlayerSourceLineWrite"
        }
    }
}

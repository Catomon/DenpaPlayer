package audio

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.thread

class DenpaStream(
    val stream: AudioInputStream,
    val audioFormat: AudioFormat = stream.format
) {
    val dataLineInfo = DataLine.Info(SourceDataLine::class.java, audioFormat)
    val line = AudioSystem.getLine(dataLineInfo) as SourceDataLine

    var stop = false

    init {
        line.open(audioFormat)
        line.start()

        thread {
            var size: Int
            val buf = ByteArray(audioFormat.channels * 960 * 2)
            while (true) {
                size = stream.read(buf)
                if (size >= 0) {
                    line.write(buf, 0, size)
                }

                if (stop) break
            }
        }.apply {
            name = "DenpaPlayer"
        }
    }
}

package audio

import com.goxr3plus.streamplayer.stream.StreamPlayer
import com.goxr3plus.streamplayer.stream.StreamPlayerEvent
import com.goxr3plus.streamplayer.stream.StreamPlayerListener
import java.io.File

class MyPlayer : StreamPlayer(), StreamPlayerListener {

    init {
        open(File("F:\\meowsic\\[Occultic;Nine] ED01 - Open Your Eyes (Asaka).mp3"))
        play()
    }

    override fun opened(dataSource: Any?, properties: MutableMap<String, Any>?) {

    }

    override fun progress(
        nEncodedBytes: Int,
        microsecondPosition: Long,
        pcmData: ByteArray?,
        properties: MutableMap<String, Any>?
    ) {

    }

    override fun statusUpdated(event: StreamPlayerEvent?) {

    }

}
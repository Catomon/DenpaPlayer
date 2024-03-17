package audio

import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import javax.sound.sampled.AudioInputStream

class DenpaLoader(
    var resultHandler: AudioLoadResultHandler
) {

    var outputFormat = StandardAudioDataFormats.COMMON_PCM_S16_BE
    val playerManager = DefaultAudioPlayerManager()
    val player = playerManager.createPlayer()

    val ytManager = YoutubeAudioSourceManager()

    var remoteSourcesRegistered = false

    init {
        playerManager.configuration.outputFormat = outputFormat

        //100 tracks per page
        ytManager.setPlaylistPageCount(20)
        playerManager.registerSourceManager(ytManager)
        AudioSourceManagers.registerLocalSource(playerManager)
        registerRemoteSources()
    }

    fun registerRemoteSources() {
        try {
            AudioSourceManagers.registerRemoteSources(playerManager)
            remoteSourcesRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addAudioEventListener(listener: AudioEventListener) {
        player.addListener(listener)
    }

    fun loadItem(identifier: String) {
        playerManager.loadItemSync(identifier, resultHandler)
    }

    fun createAudioInputStream(): AudioInputStream =
        AudioPlayerInputStream.createStream(player, outputFormat, 0, true)
}

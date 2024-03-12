package audio

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.net.URI
import java.net.URL
import java.util.LinkedList
import java.util.Queue
import java.util.UUID

/** Under construct */
class AndyDenpaTrack(
    override val uri: String,
    override val id: String = UUID.randomUUID().toString(),
    val mediaItem: MediaItem = MediaItem.Builder().setUri(uri).setMediaId(id).build(),
) : DenpaTrack {

    override val author: String get() =  mediaItem.mediaMetadata.artist.toString()
    override val name: String get() =  mediaItem.mediaMetadata.title.toString()

    constructor(mediaItem: MediaItem) : this(
        mediaItem.localConfiguration?.uri.toString(),
        mediaItem.mediaId,
        mediaItem
    )
}

class DenpaPlayerAndy(context: Context) : BaseDenpaPlayer<AndyDenpaTrack>() {
    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    init {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nextTrack()
            }
        })
    }

    override fun create() {
        if (player.isCommandAvailable(Player.COMMAND_PREPARE))
            player.prepare()
    }

    override fun load(uris: List<String>) {
        uris.forEach { uri ->
            addToPlaylist(AndyDenpaTrack(MediaItem.fromUri(uri)))
        }
    }

    override fun play(track: AndyDenpaTrack): Boolean {
        if (!player.isCommandAvailable(Player.COMMAND_STOP)) return false

        player.stop()

        if (!player.isCommandAvailable(Player.COMMAND_CHANGE_MEDIA_ITEMS)) return false
        if (!player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) return false

        player.addMediaItem(track.mediaItem)

        if (player.isCommandAvailable(Player.COMMAND_PREPARE))
            player.prepare()

        //player.play() // called in resume() from super.play()

        return super.play(track)
    }

    override fun prevTrack(): AndyDenpaTrack? {
        val nextDenpaTrack = super.prevTrack()

        player.stop()

        if (nextDenpaTrack != null) {
            player.addMediaItem(nextDenpaTrack.mediaItem)
            player.prepare()
            player.play()
        }

        return nextDenpaTrack
    }

    override fun nextTrack(): AndyDenpaTrack? {
        val nextDenpaTrack = super.nextTrack()

        player.stop()
        if (nextDenpaTrack != null) {
            player.addMediaItem(nextDenpaTrack.mediaItem)
            player.prepare()
            player.play()
        }

        return nextDenpaTrack
    }

    override fun addToPlaylist(track: AndyDenpaTrack) {
        super.addToPlaylist(track)
    }

    override fun removeFromPlaylist(track: AndyDenpaTrack) {
        super.removeFromPlaylist(track)
    }

    override fun queue(track: AndyDenpaTrack) {
        super.queue(track)

        //player.addMediaItem(track.mediaItem)
    }

    override fun freeQueue() {
        super.freeQueue()
        //player.clearMediaItems()
    }

    override fun pause() {
        if (player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE))
            player.pause()

        super.pause()
    }

    override fun resume() {
        if (player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE))
            player.play()

        super.resume()
    }

    override fun stop() {
        super.stop()

        if (player.isCommandAvailable(Player.COMMAND_STOP))
            player.stop()
    }

    override fun shutdown() {
        player.release()
    }
}
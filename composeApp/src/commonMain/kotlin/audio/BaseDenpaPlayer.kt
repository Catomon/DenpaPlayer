package audio

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.util.LinkedList

abstract class BaseDenpaPlayer<T : DenpaTrack> : DenpaPlayer<T> {
    override val playState: MutableState<DenpaPlayer.PlayState> =
        mutableStateOf(DenpaPlayer.PlayState.IDLE)
    override val playMode: MutableState<DenpaPlayer.PlayMode> =
        mutableStateOf(DenpaPlayer.PlayMode.PLAYLIST)
    override val queue: MutableState<LinkedList<T>> = mutableStateOf(LinkedList<T>())
    override val playlist: MutableState<MutableList<T>> = mutableStateOf(mutableListOf())
    override val currentTrack: MutableState<T?> = mutableStateOf(null)
    override val volume: MutableState<Float> = mutableStateOf(0.5f)

    override fun create() {

    }

    /** Sets [track] as [currentTrack]; [resume]s playback.
     * @return Always true. */
    override fun play(track: T): Boolean {
        currentTrack.value = track
        resume()

        return true
    }

    override fun queue(track: T) {
        queue.value = LinkedList(queue.value).apply { add(track) }
    }

    override fun freeQueue() {
        queue.value = LinkedList()
    }

    override fun addToPlaylist(track: T) {
        playlist.value = playlist.value.toMutableList().apply { add(track) }
    }

    override fun removeFromPlaylist(track: T) {
        playlist.value = playlist.value.toMutableList().apply { remove(track) }
    }

    /** Sets previous track in [playlist] as [currentTrack] if [queue] in not empty.
     * Does not start playback */
    override fun prevTrack(): T? {
        val oldTrack = currentTrack.value
        val track = if (queue.value.isEmpty()) {
            if (playlist.value.isEmpty()) return null

            val oldIndex = playlist.value.indexOf(oldTrack)
            playlist.value.getOrNull(
                if (oldIndex > 0 && oldIndex < playlist.value.size)
                    playlist.value.indexOf(oldTrack) - 1 else 0
            )
        } else oldTrack

        currentTrack.value = track

        return track
    }

    /** Depending on [playMode], finds next track and sets to [currentTrack]; does not start playback */
    override fun nextTrack(): T? {
        val oldTrack = currentTrack.value
        val track = if (queue.value.isEmpty()) {
            if (playlist.value.isEmpty()) return null

            when (playMode.value) {
                DenpaPlayer.PlayMode.RANDOM -> playlist.value.random()
                DenpaPlayer.PlayMode.REPEAT_PLAYLIST -> {
                    val oldIndex = playlist.value.indexOf(oldTrack)
                    playlist.value.getOrNull(
                        if (oldIndex < playlist.value.size - 1)
                            playlist.value.indexOf(oldTrack) + 1 else 0
                    )
                }

                DenpaPlayer.PlayMode.PLAYLIST, DenpaPlayer.PlayMode.ONCE ->
                    playlist.value.getOrNull(playlist.value.indexOf(oldTrack) + 1)

                DenpaPlayer.PlayMode.REPEAT_TRACK -> oldTrack
            }
        } else {
            queue.value.poll()
        }

        currentTrack.value = track

        return track
    }

    /** Sets [playState] to [DenpaPlayer.PlayState.PAUSED].
     * Or to [DenpaPlayer.PlayState.IDLE] if [currentTrack] is null */
    override fun pause() {
        if (currentTrack.value != null)
            playState.value = DenpaPlayer.PlayState.PAUSED
        else
            playState.value = DenpaPlayer.PlayState.IDLE
    }

    /** Sets [playState] to [DenpaPlayer.PlayState.PLAYING] if [currentTrack] != null */
    override fun resume() {
        if (currentTrack.value != null)
            playState.value = DenpaPlayer.PlayState.PLAYING
        else
            playState.value = DenpaPlayer.PlayState.IDLE
    }

    /** Sets [playState] to [DenpaPlayer.PlayState.IDLE] and [currentTrack] to null */
    override fun stop() {
        currentTrack.value = null
        playState.value = DenpaPlayer.PlayState.IDLE
    }

//    override fun seek(position: Float) {
//        val currentTrack = currentTrack.value ?: return
//
//        if (currentTrack.duration > 0) {
//            currentTrack.position = position
//        }
//    }

    override fun shutdown() {

    }
}
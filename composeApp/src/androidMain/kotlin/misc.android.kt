import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import audio.DenpaPlayer
import audio.DenpaTrack
import com.darkrockstudios.libraries.mpfilepicker.MultipleFilePicker
import com.github.catomon.denpaplayer.MainActivity
import java.io.File

actual val userDataFolder: File get() = File((playerContext() as MainActivity).filesDir.toURI())

@Composable
actual fun DenpaFilePicker(show: MutableState<Boolean>, denpaPlayer: DenpaPlayer<DenpaTrack>, currentPlaylistName: String) {
    val fileType = listOf("mp3")
    MultipleFilePicker(show = show.value, fileExtensions = fileType) { files ->
        show.value = false
        if (files != null) {
            //it.platformFile desk - File, android - Uri
            denpaPlayer.load(files.map { (it.platformFile as Uri).toString() })
        }
    }
}
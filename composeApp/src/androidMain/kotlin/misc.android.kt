import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import audio.DenpaPlayer
import audio.DenpaTrack
import com.darkrockstudios.libraries.mpfilepicker.MultipleFilePicker

@Composable
actual fun DenpaFilePicker(show: MutableState<Boolean>, denpaPlayer: DenpaPlayer<DenpaTrack>) {
    val fileType = listOf("mp3")
    MultipleFilePicker(show = show.value, fileExtensions = fileType) { files ->
        show.value = false
        if (files != null) {
            //it.platformFile desk - File, android - Uri
            denpaPlayer.load(files.map { (it.platformFile as Uri).toString() })
        }
    }
}
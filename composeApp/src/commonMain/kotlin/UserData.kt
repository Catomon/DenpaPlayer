import audio.DenpaTrack
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

expect val userDataFolder: File

fun saveSettings(settings: UserSettings) {
    try {
    val settingsFolder = File(userDataFolder.path)
    if (!settingsFolder.exists())
        settingsFolder.mkdirs()

    val file = File(userDataFolder.path + "/settings.json")
    if (!file.exists()) {
        file.createNewFile()
    }
    file.writeText(Json.encodeToString(settings))

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadSettings() : UserSettings {
    try {
        val settingsFile = File(userDataFolder.path + "/settings.json")
        if (settingsFile.exists())
            return Json.decodeFromString(settingsFile.readText())
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return UserSettings()
}

@Serializable
class UserSettings(
    var showTrackProgressBar: Boolean = true,
    var discordIntegration: Boolean = true,
    var japaneseTitle: Boolean = false,
    var darkTheme: Boolean = false,
    var alwaysOnTop: Boolean = false
)

//Playlists
val playlistsFolder get() = userDataFolder.path + "/playlists/"

fun removePlaylist(name: String) {
    val playlistsFolder = File(userDataFolder.path + "/playlists")
    if (!playlistsFolder.exists())
        return

    val file = File(userDataFolder.path + "/playlists/$name.pl")
    if (!file.exists())
        return
    file.delete()
}

fun savePlaylist(name: String, tracks: Array<DenpaTrack>) {
    val playlistsFolder = File(userDataFolder.path + "/playlists")
    if (!playlistsFolder.exists())
        playlistsFolder.mkdirs()

    val file = File(userDataFolder.path + "/playlists/$name.pl")
    val playlistData = PlaylistData(tracks.map { TrackData(it.uri, it.name) }.toTypedArray())
    if (!file.exists()) {
        file.createNewFile()
    }
    file.writeText(Json.encodeToString(playlistData))
}

fun loadPlaylists() : List<Pair<String, PlaylistData>> {
    val playliststs = mutableListOf<Pair<String, PlaylistData>>()
    val playlistsFolder = File(playlistsFolder)

    for (file in playlistsFolder.listFiles() ?: return playliststs) {
        if (file.isFile)
            playliststs.add(file.nameWithoutExtension to Json.decodeFromString(file.readText()))
    }

    return playliststs
}

fun loadPlaylist(name: String) : PlaylistData? {
    val file = File("$playlistsFolder$name.pl")
    if (!file.exists())
        return null

    return Json.decodeFromString(file.readText())
}

@Serializable
data class UserData(
    val playliststs: Array<PlaylistData>
)

@Serializable
data class PlaylistData(
    val tracks: Array<TrackData>,
    val isOnline: Boolean = false
)

@Serializable
data class TrackData(
    val uri: String,
    val name: String,
    val isOnline: Boolean = false
)
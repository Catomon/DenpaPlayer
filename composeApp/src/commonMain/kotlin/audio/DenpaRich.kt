package audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import denpaplayer.composeapp.generated.resources.Res
import denpaplayer.composeapp.generated.resources.denpa
import denpaplayer.composeapp.generated.resources.higurashi
import denpaplayer.composeapp.generated.resources.nanahira
import denpaplayer.composeapp.generated.resources.toromi
import net.arikia.dev.drpc.DiscordEventHandlers
import net.arikia.dev.drpc.DiscordRPC
import net.arikia.dev.drpc.DiscordRichPresence
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi

var discordRichDisabled = false

fun startDiscordRich() {
    if (discordRichDisabled) return

    DiscordRPC.discordInitialize("1211665087912484922", DiscordEventHandlers(), true)
}

fun stopDiscordRich() {
    if (discordRichDisabled) return

    DiscordRPC.discordShutdown()
}

enum class Rich {
    LISTENING,
    PAUSED,
    IDLE,
}

@OptIn(ExperimentalResourceApi::class)
val defSingerDrawableRes = Res.drawable.denpa

@OptIn(ExperimentalResourceApi::class)
class Singer(
    val names: Array<String>,
    var iconIds: Array<String> = arrayOf(
        names.first().lowercase()
    ),
    var drawableRes: DrawableResource = defSingerDrawableRes
) {
    constructor(vararg names: String) : this(arrayOf(*names))

    fun icons(vararg icons: String): Singer {
        iconIds = arrayOf(*icons)
        return this
    }

    @OptIn(ExperimentalResourceApi::class)
    fun res(drawableRes: DrawableResource): Singer {
        this.drawableRes = drawableRes
        return this
    }
}

@OptIn(ExperimentalResourceApi::class)
val singers: Array<Singer> = arrayOf(
    Singer(arrayOf("Nanahira", "ななひら")).res(Res.drawable.nanahira),
    Singer(arrayOf("Toromi", "とろ美")).res(Res.drawable.toromi),
    Singer("ひぐらしのなく頃に", "Higurashi").icons("higurashi", "higurashi_satoko")
        .res(Res.drawable.higurashi)
)

@OptIn(ExperimentalResourceApi::class)
val denpaSinger = Singer("Denpa").icons("denpa").res(Res.drawable.denpa)

val AudioTrack.trackName: String get() = info.author + " - " + info.title

fun registeredSingerBySongName(songName: String = "Denpa"): Singer {
    if (songName == "Denpa" || songName == "") return denpaSinger

    singers.forEach { singer ->
        if (singer.names.any { it.containedIn(songName) != "" }) {
            return singer
        }
    }

    return denpaSinger
}

fun discordRich(rich: Rich, track: AudioTrack?) {
    if (discordRichDisabled) return

    val singer = registeredSingerBySongName(track?.trackName ?: "Denpa")
    val singerName = singer.names.first()
    val singerIconId = singer.iconIds.random()

    val presence = DiscordRichPresence().apply {
        when (rich) {
            Rich.IDLE -> {
                largeImageKey = "denpa"
                largeImageText = "DenpaPlayer"
                smallImageKey = "idle"
                smallImageText = "Idle"
                details = "Idle"
            }

            Rich.LISTENING -> {
                largeImageKey = singerIconId
                largeImageText = singerName
                smallImageKey = "playing"
                smallImageText = "Listening"
                details = "Listening to $singerName"
            }

            Rich.PAUSED -> {
                largeImageKey = singerIconId
                largeImageText = singerName
                smallImageKey = "paused"
                smallImageText = "Paused"
                details = "Paused $singerName"
            }
        }
    }

    DiscordRPC.discordUpdatePresence(presence)
}

/** returns empty string if not contained, this otherwise */
private fun String.containedIn(string: String) =
    if (string.lowercase().contains(this.lowercase())) this else ""

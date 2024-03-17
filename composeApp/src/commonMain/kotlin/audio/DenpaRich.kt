package audio

import appName
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
        .res(Res.drawable.higurashi),
    Singer("33.turbo"),
    Singer("Choko"),
    Singer("doubleeleven UpperCut"),
    Singer("KoronePochi", "ころねぽち", "PochiKorone"),
    Singer("Haruko Momoi"),
    Singer("Innocent Key"),
    Singer("IOSYS"),
    Singer("Koko", "ココ"),
    Singer("Momobako", "桃箱"),
    Singer("MOSAIC.WAV"),
    Singer("nayuta"),
    Singer("yuu", "Installing!!!"),
    Singer("東方", "Touhou").icons("touhou", "touhou_2", "touhou_3"),
    Singer("Mili"),
    Singer("The Living Tombstone")
)

private const val defaultSingerName: String = appName

@OptIn(ExperimentalResourceApi::class)
val denpaSinger = Singer(defaultSingerName).icons("denpa").res(Res.drawable.denpa)

private val AudioTrack.trackInfoString: String get() = info.author + " - " + info.title + " - " + info.uri

val AudioTrack.trackName: String
    get() = if (info.uri.contains("https://")) info.title
    else identifier.substring(identifier.lastIndexOf('\\') + 1).removeSuffix(".mp3")

val DenpaTrack.songAuthorPlusTitle get() = "$author - $name"

fun registeredSingerBySongName(track: DenpaTrack): Singer =
    registeredSingerBySongName(track.songAuthorPlusTitle)

fun registeredSingerBySongName(songName: String = defaultSingerName): Singer {
    if (songName == defaultSingerName || songName == "") return denpaSinger

    singers.forEach { singer ->
        if (singer.names.any { it.containedIn(songName) != "" }) {
            return singer
        }
    }

    return denpaSinger
}

fun discordRich(rich: Rich, track: AudioTrack?) {
    if (discordRichDisabled) return

    val songName = track?.trackName

    val singer = registeredSingerBySongName(track?.trackInfoString ?: defaultSingerName)
    val singerName = singer.names.first()
    val singerIconId = singer.iconIds.random().replace(".", "_").replace(" ", "_").lowercase()

    val presence = DiscordRichPresence().apply {
        when (rich) {
            Rich.IDLE -> {
                largeImageKey = "denpa"
                largeImageText = appName
                smallImageKey = "idle"
                smallImageText = "Idle"
                details = "Idle"
            }

            Rich.LISTENING -> {
                largeImageKey = singerIconId
                largeImageText = singerName
                smallImageKey = "playing"
                smallImageText = "Listening"
                details = "$songName"
                endTimestamp = System.currentTimeMillis() + (track?.duration ?: Long.MAX_VALUE) - (track?.position ?: 0L)
            }

            Rich.PAUSED -> {
                largeImageKey = singerIconId
                largeImageText = singerName
                smallImageKey = "paused"
                smallImageText = "Paused"
                details = "$songName"
            }
        }
    }

    DiscordRPC.discordUpdatePresence(presence)
}

/** returns empty string if not contained, this otherwise */
private fun String.containedIn(string: String) =
    if (string.lowercase().contains(this.lowercase())) this else ""

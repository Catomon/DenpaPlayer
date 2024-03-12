import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import audio.DenpaPlayer
import audio.DenpaTrack
import audio.registeredSingerBySongName
import audio.singers
import audio.songFullName
import com.darkrockstudios.libraries.mpfilepicker.MultipleFilePicker
import denpaplayer.composeapp.generated.resources.BadComic_Regular
import denpaplayer.composeapp.generated.resources.Res
import denpaplayer.composeapp.generated.resources.close_window
import denpaplayer.composeapp.generated.resources.denpa
import denpaplayer.composeapp.generated.resources.idle
import denpaplayer.composeapp.generated.resources.minimize_window
import denpaplayer.composeapp.generated.resources.nanahira
import denpaplayer.composeapp.generated.resources.next
import denpaplayer.composeapp.generated.resources.pause
import denpaplayer.composeapp.generated.resources.play
import denpaplayer.composeapp.generated.resources.playlist
import denpaplayer.composeapp.generated.resources.prev
import denpaplayer.composeapp.generated.resources.random
import denpaplayer.composeapp.generated.resources.repeat_all
import denpaplayer.composeapp.generated.resources.repeat_single
import denpaplayer.composeapp.generated.resources.single
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun DenpaScreen(modifier: Modifier = Modifier, topBar: @Composable (() -> Unit)?) {
    MaterialTheme(typography = Typography(FontFamily(Font(Res.font.BadComic_Regular)))) {
        Box(modifier) { //.background(Color(33, 13, 51))
            Column(Modifier.align(Alignment.BottomCenter)) {
                //topBar?.invoke()
                Player(Modifier.fillMaxSize())
            }

            topBar?.invoke()
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun DenpaScreen(modifier: Modifier = Modifier) {
    MaterialTheme(typography = Typography(FontFamily(Font(Res.font.BadComic_Regular)))) {
        Box(modifier) {
            Column {
                Player(Modifier.fillMaxSize())
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ImageButton(playing: DrawableResource, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Image(painterResource(playing), null, modifier.clickable { onClick() })
}

val slightlyTransparentWhite = Color(255, 255, 255, 230)
val halfTransparentWhite = Color(255, 255, 255, 255 / 2)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun DenpaImage(currentTrack: DenpaTrack?, modifier: Modifier = Modifier) {
    val imageRes = registeredSingerBySongName(currentTrack?.songFullName ?: "").drawableRes
    AnimatedContent(imageRes, modifier) {
        Image(
            imageResource(it),
            contentDescription = "Track Image",
            //modifier = Modifier.clip(RoundedCornerShape(20.dp))
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun Player(modifier: Modifier = Modifier) {
    val denpaPlayer = remember { createDenpaPlayer }
    val playlist by remember { denpaPlayer.playlist }
    val currentTrack by remember { denpaPlayer.currentTrack }
    val playState by remember { denpaPlayer.playState }
    val playMode by remember { denpaPlayer.playMode }

    Box(modifier) {
        DenpaImage(
            currentTrack,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 25.dp)
        )
        Box(Modifier.fillMaxSize()) {
//            Box(
//                Modifier.fillMaxWidth().height(150.dp).padding(top = 25.dp)
//                    .background(halfTransparentWhite)
//            ) { }
            Playlist(
                denpaPlayer,
                playlist,
                Modifier.align(Alignment.CenterStart).padding(start = 5.dp)
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).height(25.dp)
                .background(slightlyTransparentWhite)
        ) {
            PlaybackButtons(playState, playMode, denpaPlayer)
            PlaylistButtons(denpaPlayer)
        }

        AnimatedVisibility(
            currentTrack != null,
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 25.dp)
        ) {
            CurrentTrackHeader(currentTrack, playState == DenpaPlayer.PlayState.PLAYING)
        }
    }
}

@Composable
expect fun DenpaFilePicker(show: MutableState<Boolean>, denpaPlayer: DenpaPlayer<DenpaTrack>)

@Composable
fun PlaylistButtons(denpaPlayer: DenpaPlayer<DenpaTrack>, modifier: Modifier = Modifier) {
    val showFilePicker = remember { mutableStateOf(false) }
    DenpaFilePicker(showFilePicker, denpaPlayer)
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.End), modifier = modifier) {
        Text("BruSkye", Modifier.clickable {
            denpaPlayer.load(
                listOf(
                    "https://www.youtube.com/playlist?list=PLjw1aNT6Kz2mxC1-Rys1-_Q84o4Ffv8rM",
                )
            )
        })
        Text("Nipah~", Modifier.clickable {
            denpaPlayer.load(
                listOf(
                    "https://www.youtube.com/watch?v=diP3uwWQjA4",
                )
            )
        })
        Text("File", Modifier.clickable {
            showFilePicker.value = true
        }.padding(end = 5.dp))
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PlaybackButtons(
    playState: DenpaPlayer.PlayState,
    playMode: DenpaPlayer.PlayMode,
    denpaPlayer: DenpaPlayer<DenpaTrack>,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        PlayerButton(Res.drawable.prev) {
            denpaPlayer.prevTrack()
        }
        AnimatedContent(playState == DenpaPlayer.PlayState.PLAYING) { playing ->
            if (playing) {
                PlayerButton(Res.drawable.pause) {
                    denpaPlayer.pause()
                }
            } else {
                PlayerButton(Res.drawable.play) {
                    denpaPlayer.resume()
                }
            }
        }
        PlayerButton(Res.drawable.next) {
            denpaPlayer.nextTrack()
        }
        PlayerButton(Res.drawable.idle, Modifier) {
            denpaPlayer.stop()
        }
        AnimatedContent(playMode) {
            when (it) {
                DenpaPlayer.PlayMode.ONCE -> {
                    PlayerButton(Res.drawable.single) {
                        denpaPlayer.playMode.value = DenpaPlayer.PlayMode.REPEAT_TRACK
                    }
                }

                DenpaPlayer.PlayMode.REPEAT_TRACK -> {
                    PlayerButton(Res.drawable.repeat_single) {
                        denpaPlayer.playMode.value = DenpaPlayer.PlayMode.PLAYLIST
                    }
                }

                DenpaPlayer.PlayMode.PLAYLIST -> {
                    PlayerButton(Res.drawable.playlist) {
                        denpaPlayer.playMode.value = DenpaPlayer.PlayMode.REPEAT_PLAYLIST
                    }
                }

                DenpaPlayer.PlayMode.REPEAT_PLAYLIST -> {
                    PlayerButton(Res.drawable.repeat_all) {
                        denpaPlayer.playMode.value = DenpaPlayer.PlayMode.RANDOM
                    }
                }

                DenpaPlayer.PlayMode.RANDOM -> {
                    PlayerButton(Res.drawable.random) {
                        denpaPlayer.playMode.value = DenpaPlayer.PlayMode.ONCE
                    }
                }
            }
        }
//        var volume by remember { mutableStateOf(0.5f) }
//        Slider(volume, {
//            volume = it
//        }, modifier.height(25.dp).width(60.dp),
//            colors = SliderDefaults.colors(Color.DarkGray, Color.DarkGray, Color.DarkGray, Color.DarkGray, Color.DarkGray, Color.DarkGray, Color.DarkGray, Color.DarkGray, Color.DarkGray, Color.DarkGray))
    }
}

val lightBlue = Color(2, 158, 224)
val lightPink = Color(255, 158, 242)
val lightPinkHalfTransparent = Color(255, 158, 242, 255 / 2)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CurrentTrackHeader(currentTrack: DenpaTrack?, playing: Boolean, modifier: Modifier = Modifier) {
    var songAuthor by remember { mutableStateOf("") }
    var songName by remember { mutableStateOf("") }
    if (currentTrack != null) {
        songAuthor = currentTrack.author
        songName = currentTrack.name
    }
    //, playState: DenpaPlayer.PlayState
    //if (playState == DenpaPlayer.PlayState.PLAYING) animatedColor else Color.Black
    Row(modifier.background(slightlyTransparentWhite)) {
        //val color by remember { mutableStateOf(Color.Black) }
        val infiniteTransition = rememberInfiniteTransition()
        val animatedColor by infiniteTransition.animateColor(
            initialValue = lightPink,
            targetValue = lightBlue,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        )
        val animatedColor2 by animateColorAsState(if (playing) animatedColor else Color.Black)
        AnimatedContent(
            songName
        ) {
            Row {
                Text(
                    "$songAuthor ~ $songName",
                    color = animatedColor2,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee().padding(start = 5.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Playlist(
    player: DenpaPlayer<DenpaTrack>,
    playlist: MutableList<DenpaTrack>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier) {
        item(key = "top spacer") {
            Spacer(Modifier.size(25.dp))
        }
        itemsIndexed(playlist) { i, track ->
            Text(track.songFullName, Modifier.clickable {
                player.play(playlist[i])
            }
                .background(if (player.currentTrack.value == track) lightPinkHalfTransparent else Color.Transparent))
        }
        item(key = "bottom spacer") {
            Spacer(Modifier.size(35.dp))
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PlayerButton(image: DrawableResource, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ImageButton(image, modifier.size(25.dp), onClick)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TopPanel(exitApp: () -> Unit, minimize: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(25.dp).background(slightlyTransparentWhite)
    ) { //.background(Color.LightGray)
        Text(
            "DenpaPlayer",
            modifier = Modifier.padding(horizontal = 2.dp).align(Alignment.TopStart)
        )

        val minimizeImage = imageResource(Res.drawable.minimize_window)
        val closeImage = imageResource(Res.drawable.close_window)

        Row(modifier = Modifier.align(Alignment.TopEnd)) {
            Row {
                Image(
                    remember { SmoothPainter(minimizeImage) },
                    null,
                    modifier = Modifier.size(25.dp).clickable {
                        minimize()
                    }
                )

                Image(
                    remember { SmoothPainter(closeImage) },
                    null,
                    modifier = Modifier.size(25.dp).clickable {
                        exitApp()
                    }
                )
            }
        }
    }
}
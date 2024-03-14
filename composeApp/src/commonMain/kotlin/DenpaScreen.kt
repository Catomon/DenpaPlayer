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
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import audio.DenpaPlayer
import audio.DenpaTrack
import audio.registeredSingerBySongName
import audio.songAuthorPlusTitle
import denpaplayer.composeapp.generated.resources.BadComic_Regular
import denpaplayer.composeapp.generated.resources.Res
import denpaplayer.composeapp.generated.resources.menu
import denpaplayer.composeapp.generated.resources.minimize_window
import denpaplayer.composeapp.generated.resources.next
import denpaplayer.composeapp.generated.resources.nurse_back
import denpaplayer.composeapp.generated.resources.pause
import denpaplayer.composeapp.generated.resources.play
import denpaplayer.composeapp.generated.resources.playlist
import denpaplayer.composeapp.generated.resources.prev
import denpaplayer.composeapp.generated.resources.random
import denpaplayer.composeapp.generated.resources.repeat_all
import denpaplayer.composeapp.generated.resources.repeat_single
import denpaplayer.composeapp.generated.resources.single
import denpaplayer.composeapp.generated.resources.stop
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun DenpaScreen(
    denpaPlayer: DenpaPlayer<DenpaTrack>,
    playlist: MutableList<DenpaTrack>,
    currentTrack: DenpaTrack?,
    playState: DenpaPlayer.PlayState,
    playMode: DenpaPlayer.PlayMode,
    modifier: Modifier = Modifier, topBar: @Composable (() -> Unit)?
) {
    Box(modifier) { //.background(Color(33, 13, 51))
        Player(
            denpaPlayer,
            playlist,
            currentTrack,
            playState,
            playMode,
            Modifier.fillMaxSize()
        )
        topBar?.invoke()
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun DenpaScreen(modifier: Modifier = Modifier) {
    val denpaPlayer = remember { createDenpaPlayer }
    val playlist by remember { denpaPlayer.playlist }
    val currentTrack by remember { denpaPlayer.currentTrack }
    val playState by remember { denpaPlayer.playState }
    val playMode by remember { denpaPlayer.playMode }

    MaterialTheme(typography = Typography(FontFamily(Font(Res.font.BadComic_Regular)))) {
        Box(modifier) {
            Column {
                Player(
                    denpaPlayer,
                    playlist,
                    currentTrack,
                    playState,
                    playMode,
                    Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ImageButton(playing: DrawableResource, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Image(painterResource(playing), null, modifier.clickable { onClick() })
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun DenpaImage(currentTrack: DenpaTrack?, modifier: Modifier = Modifier) {
    val imageRes = registeredSingerBySongName(currentTrack?.songAuthorPlusTitle ?: "").drawableRes
    AnimatedContent(imageRes, modifier) {
        Image(
            imageResource(it),
            contentDescription = "Track Image",
            //modifier = Modifier.clip(RoundedCornerShape(20.dp))
        )
    }
}

val almostWhiteGray = Color(240, 240, 240)

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun Player(
    denpaPlayer: DenpaPlayer<DenpaTrack>,
    playlist: MutableList<DenpaTrack>,
    currentTrack: DenpaTrack?,
    playState: DenpaPlayer.PlayState,
    playMode: DenpaPlayer.PlayMode,
    fillMaxSize: Modifier,
    modifier: Modifier = Modifier,
) {
    Box(modifier.background(Color.White)) {
        Image(
            painterResource(Res.drawable.nurse_back),
            null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter,
            alpha = 0.7f
        )

//        DenpaImage(
//            currentTrack,
//            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 25.dp)
//        )

        Playlist(
            denpaPlayer,
            playlist,
            currentTrack,
            Modifier.align(Alignment.CenterStart).fillMaxSize()
        )


        BottomBar(denpaPlayer, modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter))

        AnimatedVisibility(
            currentTrack != null,
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 25.dp)
        ) {
            CurrentTrackHeader(
                denpaPlayer,
                currentTrack,
                playState == DenpaPlayer.PlayState.PLAYING,
                Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun BottomBar(denpaPlayer: DenpaPlayer<DenpaTrack>, modifier: Modifier = Modifier) {
    Column(modifier.height(55.dp)) {
        PlaybackButtons(
            denpaPlayer,
            Modifier.height(30.dp).fillMaxWidth().background(halfTransparentWhite)
        )
        PlaylistButtons(
            denpaPlayer,
            Modifier.height(25.dp).fillMaxWidth().background(slightlyTransparentWhite)
        )
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

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlaybackButtons(
    denpaPlayer: DenpaPlayer<DenpaTrack>,
    modifier: Modifier = Modifier
) {
    val playState by denpaPlayer.playState
    val playMode by denpaPlayer.playMode
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
        PlayerButton(Res.drawable.stop, Modifier) {
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
        var volume by remember { mutableStateOf(0.5f) }
        val interactionSource = MutableInteractionSource()
        val colors = SliderDefaults.colors(
            Color.Black,
            Color.Black,
            Color.Black,
            Color.Black,
            Color.Black,
            Color.Black,
            Color.Black,
            Color.Black,
            Color.Black,
            Color.Black
        )
        Slider(
            value = volume,
            onValueChange = {
                volume = it
                denpaPlayer.setVolume(volume)
            },
            modifier = Modifier.width(60.dp),
            colors = colors,
            interactionSource = interactionSource,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    modifier = Modifier.offset(6.dp, 6.dp),
                    thumbSize = DpSize(8.dp, 8.dp),
                    colors = colors
                )
            }
        )
    }
}

val slightlyTransparentWhite = Color(255, 255, 255, 230)
val halfTransparentWhite = Color(255, 255, 255, 255 / 2)
val lightBlue = Color(2, 158, 224)
val halfTransparentLightBlue = Color(56, 196, 255, 255 / 2)
val pink = Color(255, 117, 236)
val deepPink = Color(153, 0, 132)
val lightPink = Color(255, 158, 242)
val lightPinkHalfTransparent = Color(255, 158, 242, 255 / 2)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CurrentTrackHeader(
    denpaPlayer: DenpaPlayer<DenpaTrack>,
    currentTrack: DenpaTrack?,
    playing: Boolean,
    modifier: Modifier = Modifier,
    showTrackName: Boolean = true
) {
    var songName by remember { mutableStateOf("") }
    if (currentTrack != null) {
        songName = currentTrack.name.replace(" - ", " ~ ")
    }
    var progress by remember { mutableStateOf(0f) }
    val updateProgress = {
        progress = when (currentTrack) {
            null -> 0f
            else -> if (currentTrack.duration > 0 && currentTrack.duration < Long.MAX_VALUE)
                denpaPlayer.position.toFloat() / currentTrack.duration else 1f
        }
    }
    LaunchedEffect(currentTrack) {
        while (true) {
            if (denpaPlayer.playState.value == DenpaPlayer.PlayState.PLAYING)
                updateProgress()
            delay(1000)
        }
    }
    Column(modifier.background(slightlyTransparentWhite).fillMaxWidth()
        .pointerInput(currentTrack) {
            if (currentTrack == null) return@pointerInput
            val width = this.size.width
            detectTapGestures {
                denpaPlayer.seek((currentTrack.duration * (it.x / width)).toLong())
                updateProgress()
            }
        }) {
        val infiniteTransition = rememberInfiniteTransition()
        val animatedColor by infiniteTransition.animateColor(
            initialValue = lightPink,
            targetValue = lightBlue,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        )
        val animatedColor2 by animateColorAsState(if (playing) animatedColor else Color.Black)
        AnimatedVisibility(showTrackName) {
            AnimatedContent(
                songName,
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(25.dp).fillMaxWidth()
            ) {
                Text(
                    songName,
                    color = animatedColor2,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee().padding(horizontal = 5.dp)
                )
            }
        }
        LinearProgressIndicator(
            progress = progress,
            color = pink,
            backgroundColor = lightPinkHalfTransparent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun Playlist(
    player: DenpaPlayer<DenpaTrack>,
    playlist: MutableList<DenpaTrack>,
    currentTrack: DenpaTrack?,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
//    LaunchedEffect(currentTrack) {
//        if (currentTrack != null) {
//            listState.scrollToItem(playlist.indexOf(currentTrack))
//        }
//    }
    Box(modifier) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            item(key = "top spacer") {
                Spacer(Modifier.size(60.dp))
            }
            itemsIndexed(playlist) { i, track ->
                if (currentTrack == track) {

                }
                Box(Modifier.background(if (currentTrack == track) lightPinkHalfTransparent else Color.Transparent)) {
                    Text(track.name, Modifier
                        .clickable { player.play(playlist[i]) }
                        .fillMaxWidth()
                        .padding(start = 5.dp)
                    )
                }
            }
            item(key = "bottom spacer") {
                Spacer(Modifier.size(60.dp))
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                .padding(top = 25.dp, bottom = 55.dp, end = 4.dp),
            adapter = rememberScrollbarAdapter(
                scrollState = listState
            )
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PlayerButton(image: DrawableResource, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ImageButton(image, modifier.size(50.dp), onClick)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TopPanel(exitApp: () -> Unit, minimize: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(25.dp).background(slightlyTransparentWhite)
    ) { //.background(Color.LightGray)
        Text(
            "電波プレーヤー",
            modifier = Modifier.padding(horizontal = 4.dp).align(Alignment.TopStart)
        )

        val minimizeImage = imageResource(Res.drawable.minimize_window)
        //val closeImage = imageResource(Res.drawable.close_window)

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
                    painterResource(Res.drawable.menu),
                    null,
                    modifier = Modifier.size(25.dp).clickable {
                        exitApp()
                    }
                )
            }
        }
    }
}

@Composable
fun ClickableProgressIndicator(key1: Any?, progress: Float, onClick: (newProgress: Float) -> Unit) {
    LinearProgressIndicator(
        progress = progress,
        color = pink,
        backgroundColor = lightPinkHalfTransparent,
        modifier = Modifier.fillMaxWidth().pointerInput(key1) {
            val width = this.size.width
            detectTapGestures {
                onClick(it.x / width)
            }
        }
    )
}
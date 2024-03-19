import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.Typography
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import audio.DenpaPlayer
import audio.DenpaTrack
import audio.registeredSingerBySongName
import audio.songAuthorPlusTitle
import denpaplayer.composeapp.generated.resources.BadComic_Regular
import denpaplayer.composeapp.generated.resources.Res
import denpaplayer.composeapp.generated.resources.folder
import denpaplayer.composeapp.generated.resources.menu
import denpaplayer.composeapp.generated.resources.minimize_window
import denpaplayer.composeapp.generated.resources.next
import denpaplayer.composeapp.generated.resources.pause
import denpaplayer.composeapp.generated.resources.play
import denpaplayer.composeapp.generated.resources.playlist
import denpaplayer.composeapp.generated.resources.playlists
import denpaplayer.composeapp.generated.resources.prev
import denpaplayer.composeapp.generated.resources.random
import denpaplayer.composeapp.generated.resources.repeat_all
import denpaplayer.composeapp.generated.resources.repeat_single
import denpaplayer.composeapp.generated.resources.single
import denpaplayer.composeapp.generated.resources.stop
import denpaplayer.composeapp.generated.resources.url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun DenpaScreen(
    state: DenpaState,
    modifier: Modifier = Modifier, topBar: @Composable (() -> Unit)?
) {
    Box(modifier) {
        Player(
            state,
            Modifier.fillMaxSize()
        )
        topBar?.invoke()
    }
}

class DenpaState {
    val denpaPlayer = createDenpaPlayer
    val playlist by denpaPlayer.playlist
    val currentTrack by denpaPlayer.currentTrack
    val playState by denpaPlayer.playState
    val playMode by denpaPlayer.playMode
    var currentPlaylistName by mutableStateOf("default")
    var showSongUrlInput by mutableStateOf(false)
    var showPlaylistsPane by mutableStateOf(false)
    var isLoadingPlaylistFile by mutableStateOf(false)
    var isLoadingSong by mutableStateOf<DenpaTrack?>(null)

    var height by mutableStateOf(0)
    var width by mutableStateOf(0)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun DenpaScreen(modifier: Modifier = Modifier) {
    val state = DenpaState()

    MaterialTheme(typography = Typography(FontFamily(Font(Res.font.BadComic_Regular)))) {
        Box(modifier) {
            Column {
                Player(
                    state,
                    Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ImageButton(playing: DrawableResource, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Image(imageResource(playing), null, modifier.clickable { onClick() })
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
    state: DenpaState,
    modifier: Modifier = Modifier
) {
    val denpaPlayer = state.denpaPlayer
    val playlist = state.playlist
    val currentTrack = state.currentTrack
    val playState = state.playState
    val playMode = state.playMode
    val currentPlaylistName = state.currentPlaylistName

    LaunchedEffect(currentPlaylistName) {
        CoroutineScope(Dispatchers.Default).launch {
            state.isLoadingPlaylistFile = true
            try {
                val trackUris = loadPlaylist(currentPlaylistName)?.tracks
                if (trackUris != null) {
                    denpaPlayer.playlist.value = mutableListOf()
                    trackUris.forEach {
                        denpaPlayer.addToPlaylist(createDenpaTrack(it.uri, it.name))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            state.isLoadingPlaylistFile = false
        }
    }

    Box(modifier.background(Color.White)) {
//        Image(
//            painterResource(Res.drawable.nurse_back),
//            null,
//            modifier = Modifier.fillMaxWidth(),
//            contentScale = ContentScale.FillWidth,
//            alignment = Alignment.TopCenter,
//            alpha = 0.7f
//        )

//        DenpaImage(
//            currentTrack,
//            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 25.dp)
//        )

        val alpha by animateFloatAsState(if (state.height > 108) 1f else 0f)
        Playlist(state, Modifier.align(Alignment.CenterStart).fillMaxSize().alpha(alpha))

        Box(Modifier.fillMaxSize().padding(bottom = 55.dp)) {
            AnimatedVisibility(
                state.showSongUrlInput,
                modifier = Modifier.pointerInput(Unit) {},
                enter = expandIn(
                    expandFrom = Alignment.CenterStart,
                    initialSize = { IntSize(it.width, 0) }) + fadeIn(),
                exit = shrinkOut(
                    shrinkTowards = Alignment.CenterStart,
                    targetSize = { IntSize(it.width, 0) }) + fadeOut()
            ) {
                SongUrlInputPane(state)
            }
            AnimatedVisibility(
                state.showPlaylistsPane,
                modifier = Modifier.pointerInput(Unit) {},
                enter = expandIn(
                    expandFrom = Alignment.CenterStart,
                    initialSize = { IntSize(it.width, 0) }) + fadeIn(),
                exit = shrinkOut(
                    shrinkTowards = Alignment.CenterStart,
                    targetSize = { IntSize(it.width, 0) }) + fadeOut()
            ) {
                PlaylistsPane(state)
            }
        }

        BottomBar(
            state,
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
        ) //height 55.dp

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

        AnimatedVisibility(
            state.isLoadingPlaylistFile,
            Modifier.fillMaxSize().pointerInput(Unit) {}.background(halfTransparentWhite)
        ) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    trackColor = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
                Text("Loading playlist...")
            }
        }
    }
}

@Composable
fun BottomBar(state: DenpaState, modifier: Modifier = Modifier) {
    Column(modifier.height(55.dp)) {
        PlaybackButtons(
            state.denpaPlayer,
            Modifier.height(30.dp).fillMaxWidth().background(halfTransparentWhite)
        )
        PlaylistButtons(
            state,
            Modifier.height(25.dp).fillMaxWidth().background(slightlyTransparentWhite)
        )
    }
}

@Composable
expect fun DenpaFilePicker(
    show: MutableState<Boolean>,
    denpaPlayer: DenpaPlayer<DenpaTrack>,
    currentPlaylistName: String
)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PlaylistsPane(state: DenpaState) {
    var playlists by remember { mutableStateOf(loadPlaylists()) }
    var newPlaylistName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val crateNewPlaylist = {
        state.currentPlaylistName = newPlaylistName
        savePlaylist(newPlaylistName, emptyArray())
        playlists = loadPlaylists()
    }
    Box(
        Modifier.fillMaxSize().background(slightlyTransparentWhite).fillMaxSize()
            .pointerInput(Unit) {}) {
            LazyColumn {
                item(key = "top spacer") {
                    Spacer(Modifier.size(60.dp))
                }
                items(playlists) {
                    ContextMenuArea(items = {
                        listOf(
                            ContextMenuItem("Remove") {
                                removePlaylist(it.first)
                                if (state.currentPlaylistName == it.first)
                                    state.currentPlaylistName = "default"
                                playlists = loadPlaylists()
                            },
                        )
                    }) {
                        Text(it.first, Modifier.fillMaxWidth().padding(start = 5.dp)
                            .clickable {
                                state.currentPlaylistName = it.first
                                state.showPlaylistsPane = false
                            }
                        )
                    }
                }
                item(key = "bottom spacer") {
                    Spacer(Modifier.size(60.dp))
                }
            }

        Row(Modifier.align(Alignment.BottomCenter).padding(6.dp)) {
            OutlinedTextField(
                newPlaylistName,
                onValueChange = { newPlaylistName = it },
                label = { Text("Playlist Name") },
                modifier = Modifier.width(300.dp).height(65.dp),
                singleLine = true,
                isError = isError,
                trailingIcon = {
                    Button(
                        {
                            if (isValidFileName(newPlaylistName))
                                crateNewPlaylist()
                            else
                                isError = true
                        },
                        modifier = Modifier.width(90.dp).height(42.dp).padding(end = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Create")
                    }
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.LightGray,
                    focusedIndicatorColor = Color.Black,
                    focusedLabelColor = Color.Black
                )
            )

        }
    }
}

@Composable
fun SongUrlInputPane(state: DenpaState) {
    var url by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    Column(
        Modifier.background(slightlyTransparentWhite).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        Box(Modifier.weight(1f)) {
            Text(
                text = "Enter track or playlist url to add them to the current playlist. Make sure its not private or age-restricted.",
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        OutlinedTextField(
            url,
            label = { Text("URL") },
            onValueChange = { text: String -> url = text },
            singleLine = true,
            modifier = Modifier.width(300.dp).height(65.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.LightGray,
                focusedIndicatorColor = Color.Black,
                focusedLabelColor = Color.Black
            ),
            trailingIcon = {
                Button(
                    {
                        CoroutineScope(Dispatchers.Default).launch {
                            loading = true
                            state.denpaPlayer.load(listOf(url))
                            savePlaylist(state.currentPlaylistName, state.playlist.toTypedArray())
                            state.showSongUrlInput = false
                        }
                    },
                    Modifier.width(90.dp).height(42.dp).padding(end = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    AnimatedContent(loading) {
                        if (it)
                            CircularProgressIndicator(
                                color = Color.White,
                                trackColor = Color.Black,
                                modifier = Modifier.size(30.dp)
                            )
                        else
                            Text("Add")
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
@Composable
fun PlaylistButtons(state: DenpaState, modifier: Modifier = Modifier) {
    val showFilePicker = remember { mutableStateOf(false) }
    DenpaFilePicker(showFilePicker, state.denpaPlayer, state.currentPlaylistName)
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
        modifier = modifier.pointerInput(Unit) {}) {
        //        AnimatedContent(state.playlist) {
        Text(
            "${state.currentPlaylistName}: ${state.playlist.size}",
            modifier = Modifier.weight(1f).padding(start = 5.dp).basicMarquee(),
            maxLines = 1,
        )
//        }
        Image(imageResource(Res.drawable.playlists), "Manage playlists", Modifier.clickable {
            state.showSongUrlInput = false
            state.showPlaylistsPane = !state.showPlaylistsPane
        })
        Image(imageResource(Res.drawable.url), "Add tracks by url", Modifier.clickable {
            state.showPlaylistsPane = false
            state.showSongUrlInput = !state.showSongUrlInput
        })
        Image(
            imageResource(Res.drawable.folder),
            "Add tracks from folder",
            modifier = Modifier.clickable {
                showFilePicker.value = true
            })
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
    Row(modifier.pointerInput(Unit) {}) {
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Playlist(
    state: DenpaState,
    modifier: Modifier = Modifier
) {
    val player: DenpaPlayer<DenpaTrack> = state.denpaPlayer
    val playlist: MutableList<DenpaTrack> = state.playlist
    val currentTrack: DenpaTrack? = state.currentTrack
    val listState = rememberLazyListState()
    LaunchedEffect(state.currentPlaylistName) {
        listState.scrollToItem(0)
    }
//    LaunchedEffect(currentTrack) {
//        if (currentTrack != null) {
//            listState.scrollToItem(playlist.indexOf(currentTrack))
//        }
//    }
    Box(modifier) {
        val coroutineScope = rememberCoroutineScope()
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            item(key = "top spacer") {
                Spacer(Modifier.size(60.dp))
            }
            itemsIndexed(playlist) { i, track ->
                var hovered by remember { mutableStateOf(false) }
                ContextMenuArea(items = {
                    listOf(
                        ContextMenuItem("Remove") {
                            coroutineScope.launch {
                                state.isLoadingSong = track
                                state.denpaPlayer.removeFromPlaylist(track)
                                state.denpaPlayer.playlist.value = state.denpaPlayer.playlist.value
                                savePlaylist(state.currentPlaylistName, state.denpaPlayer.playlist.value.toTypedArray())
                                //listState.scrollToItem(i, -60)
                                state.isLoadingSong = null
                            }
                        }
                    )
                }) {
                    Box(Modifier.background(if (currentTrack == track) lightPinkHalfTransparent else Color.Transparent)
                        .onPointerEvent(
                            PointerEventType.Enter
                        ) {
                            hovered = true
                        }.onPointerEvent(
                            PointerEventType.Exit
                        ) {
                            hovered = false
                        }) {

                        Text(track.name, Modifier
                            .clickable {
                                if (state.isLoadingSong != null) return@clickable

                                CoroutineScope(Dispatchers.Default).launch {
                                    state.isLoadingSong = track
                                    player.play(playlist[i])
                                    state.isLoadingSong = null
                                }
                            }
                            .fillMaxWidth()
                            .padding(start = 5.dp)
                        )

                        AnimatedVisibility(
                            state.isLoadingSong == track,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 5.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                trackColor = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            item(key = "bottom spacer") {
                Spacer(Modifier.size(60.dp))
            }
        }

        VertScrollbar(
            listState, Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                .padding(top = 25.dp, bottom = 55.dp, end = 4.dp)
        )
    }
}

@Composable
expect fun VertScrollbar(listState: LazyListState, modifier: Modifier)

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
            appName,
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
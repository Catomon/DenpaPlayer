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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
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
import denpaplayer.composeapp.generated.resources.denpa_top_icon
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
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.system.exitProcess

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
    var showOptionsPane by mutableStateOf(false)
    var isLoadingPlaylistFile by mutableStateOf(false)
    var isLoadingSong by mutableStateOf<DenpaTrack?>(null)

    var settings by mutableStateOf(loadSettings())

    var height by mutableStateOf(0)
    var width by mutableStateOf(0)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun DenpaScreen(modifier: Modifier = Modifier) {
    val state = DenpaState()

    MaterialTheme(typography = Typography(TextStyle(fontFamily = FontFamily(Font(Res.font.BadComic_Regular))))) {
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
    Image(
        imageResource(playing),
        null,
        modifier.clickable { onClick() },
        colorFilter = ColorFilter.tint(Colors.objectPrimary)
    )
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

    Box(modifier.background(Colors.backgroundPrimary)) {
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
                state.showOptionsPane,
                modifier = Modifier.pointerInput(Unit) {},
                enter = expandIn(
                    expandFrom = Alignment.CenterStart,
                    initialSize = { IntSize(it.width, 0) }) + fadeIn(),
                exit = shrinkOut(
                    shrinkTowards = Alignment.CenterStart,
                    targetSize = { IntSize(it.width, 0) }) + fadeOut()
            ) {
                OptionsPane(state)
            }
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
            Modifier.fillMaxSize().pointerInput(Unit) {}.background(Colors.surfaceSecondary)
        ) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Colors.objectBackgroundPrimary,
                    trackColor = Colors.objectPrimary,
                    modifier = Modifier.size(30.dp)
                )
                Text("Loading playlist...", color = Colors.textPrimary)
            }
        }
    }
}

@Composable
fun OptionsPane(state: DenpaState) {
    val settings = state.settings
    var showTrackBarWin by remember { mutableStateOf(settings.showTrackProgressBar) }
    var discordIntegration by remember { mutableStateOf(settings.discordIntegration) }
    var japaneseTitle by remember { mutableStateOf(settings.japaneseTitle) }
    val theme = settings.theme
    var alwaysOnTop by remember { mutableStateOf(settings.alwaysOnTop) }

    Column(
        Modifier.background(Colors.surfacePrimary).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        Spacer(Modifier.size(60.dp))

//        Text("Some might require restart", color = Colors.textPrimary)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Show track progress bar", color = Colors.textPrimary)
            Switch(showTrackBarWin, {
                showTrackBarWin = it
                settings.showTrackProgressBar = it
                saveSettings(settings)
                state.settings = loadSettings()
            }, colors = SwitchDefaults.colors(checkedThumbColor = Colors.objectPrimary))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Discord Integration", color = Colors.textPrimary)
            Switch(discordIntegration, {
                discordIntegration = it
                settings.discordIntegration = it
                saveSettings(settings)
                state.settings = loadSettings()
            }, colors = SwitchDefaults.colors(checkedThumbColor = Colors.objectPrimary))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Japanese title", color = Colors.textPrimary)
            Switch(japaneseTitle, {
                japaneseTitle = it
                settings.japaneseTitle = it
                saveSettings(settings)
                state.settings = loadSettings()
            }, colors = SwitchDefaults.colors(checkedThumbColor = Colors.objectPrimary))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Theme", color = Colors.textPrimary)
            RadioButton(
                theme == Themes.WHITE,
                colors = RadioButtonDefaults.colors(Color.LightGray, Color.LightGray),
                onClick = {
                    settings.theme = Themes.WHITE
                    saveSettings(settings)
                    state.settings = loadSettings()
                })
            RadioButton(
                theme == Themes.VIOL,
                colors = RadioButtonDefaults.colors(Color.Black, Color.Black),
                onClick = {
                    settings.theme = Themes.VIOL
                    saveSettings(settings)
                    state.settings = loadSettings()
                })
            RadioButton(
                theme == Themes.PINK,
                colors = RadioButtonDefaults.colors(Colors.pink1, Colors.pink1),
                onClick = {
                    settings.theme = Themes.PINK
                    saveSettings(settings)
                    state.settings = loadSettings()
                })
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Window always on top", color = Colors.textPrimary)
            Switch(alwaysOnTop, {
                alwaysOnTop = it
                settings.alwaysOnTop = it
                saveSettings(settings)
                state.settings = loadSettings()
            }, colors = SwitchDefaults.colors(checkedThumbColor = Colors.objectPrimary))
        }

        Spacer(Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(50.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                " ",
                color = Colors.textPrimary,
                fontStyle = FontStyle.Italic,
               /* modifier = Modifier.clickable {

                }*/)

            Button(
                { exitProcess(0) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Colors.objectPrimary,
                    contentColor = Colors.textSecondary
                )
            ) { Text("Exit App", color = Colors.textSecondary) }

            Text(
                "GitHub",
                color = Colors.textPrimary,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.clickable {
                    openGitHub()
                })
        }

        Spacer(Modifier.size(6.dp))
    }
}

@Composable
fun BottomBar(state: DenpaState, modifier: Modifier = Modifier) {
    Column(modifier.height(55.dp)) {
        PlaybackButtons(
            state.denpaPlayer,
            Modifier.height(30.dp).fillMaxWidth().background(Colors.surfaceSecondary)
        )
        PlaylistButtons(
            state,
            Modifier.height(25.dp).fillMaxWidth().background(Colors.surfacePrimary)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongUrlInputPane(state: DenpaState) {
    var url by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    Column(
        Modifier.background(Colors.surfacePrimary).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        Box(Modifier.weight(1f)) {
            Text(
                text = "Enter track or playlist url to add them to the current playlist. Make sure its not private or age-restricted.",
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
                color = Colors.textPrimary
            )
        }
        Box(Modifier.padding(6.dp)) {
            OutlinedTextField(
                url,
                label = { Text("URL", color = Colors.textPrimary) },
                onValueChange = { text: String -> url = text },
                singleLine = true,
                modifier = Modifier.width(300.dp).height(65.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Colors.objectBackgroundPrimary,
                    focusedIndicatorColor = Colors.objectPrimary,
                    focusedLabelColor = Colors.objectPrimary
                ),
                trailingIcon = {
                    Button(
                        {
                            CoroutineScope(Dispatchers.Default).launch {
                                loading = true
                                state.denpaPlayer.load(listOf(url))
                                savePlaylist(
                                    state.currentPlaylistName,
                                    state.playlist.toTypedArray()
                                )
                                state.showSongUrlInput = false
                            }
                        },
                        Modifier.width(90.dp).height(42.dp).padding(end = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Colors.objectPrimary,
                            contentColor = Colors.textSecondary
                        )
                    ) {
                        AnimatedContent(loading) {
                            if (it)
                                CircularProgressIndicator(
                                    color = Colors.objectBackgroundPrimary,
                                    trackColor = Colors.objectPrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            else
                                Text("Add", color = Colors.textSecondary)
                        }
                    }
                },
            )
        }
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
            color = Colors.textPrimary
        )
//        }
        ImageButton(Res.drawable.playlists, onClick = {
            state.showSongUrlInput = false
            state.showOptionsPane = false
            state.showPlaylistsPane = !state.showPlaylistsPane
        })
        ImageButton(Res.drawable.url, onClick = {
            state.showPlaylistsPane = false
            state.showOptionsPane = false
            state.showSongUrlInput = !state.showSongUrlInput
        })
        ImageButton(Res.drawable.folder, onClick = {
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
        val interactionSource = remember { MutableInteractionSource() }
        val colors = SliderDefaults.colors(
            Colors.objectPrimary,
            Colors.objectPrimary,
            Colors.objectPrimary,
            Colors.objectPrimary,
            Colors.objectPrimary,
            Colors.objectPrimary,
            Colors.objectPrimary,
            Colors.objectPrimary,
            Colors.objectPrimary,
            Colors.objectPrimary
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
    Column(modifier.background(Colors.surfacePrimary).fillMaxWidth()
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
            initialValue = Colors.textSpecial,
            targetValue = Colors.textSpecial2,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        )
        val animatedColor2 by animateColorAsState(if (playing) animatedColor else Colors.textPrimary)
        AnimatedVisibility(showTrackName) {
            AnimatedContent(
                songName,
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(25.dp).fillMaxWidth()
            ) {
                it
                Text(
                    songName,
                    color = animatedColor2,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee().padding(horizontal = 5.dp),
                )
            }
        }
        LinearProgressIndicator(
            progress = progress,
            trackColor = Colors.specialPrimary,
            color = Colors.specialPrimaryTransparent,
            modifier = Modifier.fillMaxWidth()
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
fun TopPanel(state: DenpaState, exitApp: () -> Unit, minimize: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(25.dp).background(Colors.surfacePrimary)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp).align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageResource(Res.drawable.denpa_top_icon),
                null,
                modifier = Modifier.offset(y = 2.dp)
            )
            Text(
                appName,
                modifier = Modifier.padding(horizontal = 4.dp),
                color = Colors.textPrimary
            )
        }

        Row(modifier = Modifier.align(Alignment.TopEnd)) {
            Row {
                ImageButton(Res.drawable.minimize_window,
                    modifier = Modifier.size(25.dp),
                    onClick = {
                        minimize()
                    }
                )
                ImageButton(Res.drawable.menu,
                    modifier = Modifier.size(25.dp),
                    onClick = {
                        state.showPlaylistsPane = false
                        state.showSongUrlInput = false
                        state.showOptionsPane = !state.showOptionsPane
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsPane(state: DenpaState) {
    var playlists by remember { mutableStateOf(loadPlaylists()) }
    var newPlaylistName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    Box(
        Modifier.fillMaxSize().background(Colors.surfacePrimary).fillMaxSize()
            .pointerInput(Unit) {}) {
        LazyColumn {
            item(key = "top spacer") {
                Spacer(Modifier.size(60.dp))
            }
            items(playlists) {
                Text(it.first, Modifier.fillMaxWidth().padding(start = 5.dp)
                    .clickable {
                        state.currentPlaylistName = it.first
                        state.showPlaylistsPane = false
                    }
                )
            }
            item(key = "bottom spacer") {
                Spacer(Modifier.size(60.dp))
            }
        }

        Row(Modifier.align(Alignment.BottomCenter).padding(6.dp)) {
            OutlinedTextField(
                newPlaylistName,
                onValueChange = { newPlaylistName = it },
                label = { Text("Playlist Name", color = Colors.textPrimary) },
                modifier = Modifier.width(300.dp).height(65.dp),
                singleLine = true,
                isError = isError,
                trailingIcon = {
                    Button(
                        {
                            if (isValidFileName(newPlaylistName)) {
                                state.currentPlaylistName = newPlaylistName
                                savePlaylist(newPlaylistName, emptyArray())
                                playlists = loadPlaylists()
                                newPlaylistName = ""
                            } else
                                isError = true
                        },
                        modifier = Modifier.width(90.dp).height(42.dp).padding(end = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Colors.objectPrimary,
                            contentColor = Colors.textSecondary
                        )
                    ) {
                        Text("Create", color = Colors.textSecondary)
                    }
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Colors.objectBackgroundPrimary,
                    focusedIndicatorColor = Colors.objectPrimary,
                    focusedLabelColor = Colors.objectPrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Playlist(state: DenpaState, modifier: Modifier) {
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
                Box(Modifier.background(if (currentTrack == track) Colors.specialPrimaryTransparent else Color.Transparent)) {
                    Text(track.name, Modifier
                        .clickable {
                            if (state.isLoadingSong != null) return@clickable

//                            CoroutineScope(Dispatchers.Default).launch {
                            state.isLoadingSong = track
                            player.play(playlist[i])
                            state.isLoadingSong = null
//                            }
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
            item(key = "bottom spacer") {
                Spacer(Modifier.size(60.dp))
            }
        }
    }
}
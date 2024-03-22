import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import audio.DenpaPlayer
import audio.DenpaTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
actual fun VertScrollbar(listState: LazyListState, modifier: Modifier) {
    VerticalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(
            scrollState = listState
        )
    )
}

@Composable
actual fun PlaylistsPane(state: DenpaState) {
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
                ContextMenuArea(items = {
                    listOf(
                        ContextMenuItem("Clear") {
                            removePlaylist(it.first)
                            if (state.currentPlaylistName == it.first)
                                state.denpaPlayer.playlist.value = mutableListOf()
                            savePlaylist(state.currentPlaylistName, state.denpaPlayer.playlist.value.toTypedArray())
                        },
                        ContextMenuItem("Remove") {
                            removePlaylist(it.first)
                            if (state.currentPlaylistName == it.first)
                                state.currentPlaylistName = "default"
                            playlists = loadPlaylists()
                        },
                    )
                }) {
                    Text(
                        it.first, Modifier.fillMaxWidth().padding(start = 5.dp)
                            .clickable {
                                state.currentPlaylistName = it.first
                                state.showPlaylistsPane = false
                            },
                        color = Colors.textPrimary
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
                            backgroundColor = Colors.objectPrimary,
                            contentColor = Colors.textSecondary
                        )
                    ) {
                        Text("Create", color = Colors.textSecondary)
                    }
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Colors.objectBackgroundPrimary,
                    focusedIndicatorColor = Colors.objectPrimary,
                    focusedLabelColor = Colors.objectPrimary
                )
            )

        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Playlist(state: DenpaState, modifier: Modifier) {
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
    Box(modifier.background(Colors.backgroundPrimary)) {
        val clipboard = LocalClipboardManager.current
        val coroutineScope = rememberCoroutineScope()
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            item(key = "top spacer") {
                Spacer(Modifier.size(60.dp))
            }
            itemsIndexed(playlist) { i, track ->
                var hovered by remember { mutableStateOf(false) }
                ContextMenuArea(items = {
                    listOf(
                        ContextMenuItem("Copy URI") {
                            clipboard.setText(AnnotatedString(track.uri))
                        },
                        ContextMenuItem("Remove") {
                            coroutineScope.launch {
                                state.isLoadingSong = track
                                state.denpaPlayer.removeFromPlaylist(track)
                                state.denpaPlayer.playlist.value = state.denpaPlayer.playlist.value
                                savePlaylist(
                                    state.currentPlaylistName,
                                    state.denpaPlayer.playlist.value.toTypedArray()
                                )
                                //listState.scrollToItem(i, -60)
                                state.isLoadingSong = null
                            }
                        }
                    )
                }) {
                    Box(Modifier.background(if (currentTrack == track) Colors.specialPrimaryTransparent else Color.Transparent)
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
                            .padding(start = 5.dp),
                            color = Colors.textPrimary
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
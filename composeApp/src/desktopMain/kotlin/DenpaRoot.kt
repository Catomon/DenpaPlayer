import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import audio.DenpaPlayer
import audio.DenpaTrack
import denpaplayer.composeapp.generated.resources.BadComic_Regular
import denpaplayer.composeapp.generated.resources.Res
import denpaplayer.composeapp.generated.resources.denpa
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.ComponentAdapter
import java.awt.geom.RoundRectangle2D

@OptIn(ExperimentalResourceApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ApplicationScope.DenpaRoot() {
    val denpaPlayer = remember { createDenpaPlayer }
    val playlist by remember { denpaPlayer.playlist }
    val currentTrack by remember { denpaPlayer.currentTrack }
    val playState by remember { denpaPlayer.playState }
    val playMode by remember { denpaPlayer.playMode }

    val playerWindowState = rememberWindowState(
        width = 320.dp, height = 400.dp, position = WindowPosition(Alignment.Center),
    )

    MaterialTheme(typography = Typography(FontFamily(Font(Res.font.BadComic_Regular)))) {
        PlayerWindow(denpaPlayer, playlist, currentTrack, playState, playMode, playerWindowState)

        if (playerWindowState.isMinimized) {
            TrackHeaderWindow(denpaPlayer) { playerWindowState.isMinimized = false }
        }

        Tray(
            icon = painterResource(Res.drawable.denpa),
            tooltip = "DenpaPlayer",
            onAction = { playerWindowState.isMinimized = false },
            menu = {
                Item("Exit", onClick = ::exitApplication)
            },
        )
    }
}

val screenWidth = Toolkit.getDefaultToolkit().screenSize.width
val Density.headerMinSize get() = DpSize(screenWidth.toDp(), 4.dp)
val Density.headerMaxSize get() = DpSize(screenWidth.toDp(), 29.dp)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ApplicationScope.PlayerWindow(
    denpaPlayer: DenpaPlayer<DenpaTrack>,
    playlist: MutableList<DenpaTrack>,
    currentTrack: DenpaTrack?,
    playState: DenpaPlayer.PlayState,
    playMode: DenpaPlayer.PlayMode,
    state: WindowState
) {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DenpaPlayer",
        resizable = true,
        state = state,
        undecorated = true,
        icon = painterResource(Res.drawable.denpa),
    ) {
        window.addComponentListener(ShapeOnResize(window))

//        LaunchedEffect(state) {
//            snapshotFlow { state.size }
//                .onEach {  }
//        }

        window.minimumSize = Dimension(320, 108)

        val exitApp = ::exitApplication

        val minimize = {
            state.isMinimized = state.isMinimized.not()
        }

        DenpaScreen(
            denpaPlayer,
            playlist,
            currentTrack,
            playState,
            playMode,
            Modifier.fillMaxSize().border(1.8.dp, Color.Black, RoundedCornerShape(10.dp))
        ) {
            WindowDraggableArea {
                TopPanel(exitApp, minimize)
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TrackHeaderWindow(denpaPlayer: DenpaPlayer<DenpaTrack>, onCloseRequest: () -> Unit) {
    val state = rememberWindowState(
        size = with(LocalDensity.current) { headerMinSize },
        position = WindowPosition(
            Alignment.TopCenter
        )
    )

    Window(
        onCloseRequest = onCloseRequest,
        title = "Song header",
        resizable = false,
        undecorated = true,
        //transparent = true,
        alwaysOnTop = true,
        //visible = false,
        icon = painterResource(Res.drawable.denpa),
        state = state,
    ) {
        var showTrackName by remember { mutableStateOf(false) }
        CurrentTrackHeader(
            denpaPlayer,
            denpaPlayer.currentTrack.value,
            denpaPlayer.playState.value == DenpaPlayer.PlayState.PLAYING,
            Modifier.fillMaxSize().background(Color.Transparent)
                .onPointerEvent(PointerEventType.Enter) {
                    showTrackName = true
                    state.size = headerMaxSize
                }.onPointerEvent(PointerEventType.Exit) {
                    showTrackName = false
                    state.size = headerMinSize
                },
            showTrackName
        )
    }
}

class ShapeOnResize(val window: ComposeWindow) : ComponentAdapter() {
    override fun componentResized(e: java.awt.event.ComponentEvent?) {
        val width = if (window.width > 600)
            600 else window.width
        val height = if (window.height > 900)
            900 else window.height
        window.setSize(width, height)

        window.shape = RoundRectangle2D.Double(
            0.0,
            0.0,
            window.width.toDouble(),
            window.height.toDouble(),
            20.0,
            20.0
        )
    }
}
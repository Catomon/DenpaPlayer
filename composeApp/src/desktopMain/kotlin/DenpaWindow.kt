import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.onGloballyPositioned
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
import audio.discordRichDisabled
import denpaplayer.composeapp.generated.resources.BadComic_Regular
import denpaplayer.composeapp.generated.resources.Res
import denpaplayer.composeapp.generated.resources.denpa
import denpaplayer.composeapp.generated.resources.pause_icon
import denpaplayer.composeapp.generated.resources.play_icon
import net.arikia.dev.drpc.DiscordRPC
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.ComponentAdapter
import java.awt.geom.RoundRectangle2D

@OptIn(ExperimentalResourceApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ApplicationScope.DenpaApp() {
    val denpaState = remember { DenpaState() }

    val playerWindowState = rememberWindowState(
        width = 320.dp, height = 400.dp, position = WindowPosition(Alignment.Center),
    )

    MaterialTheme(
        typography = Typography(FontFamily(Font(Res.font.BadComic_Regular)))
    ) {
        PlayerWindow(denpaState, playerWindowState)

        if (playerWindowState.isMinimized && denpaState.settings.showTrackProgressBar) {
            TrackProgressBarWindow(denpaState.denpaPlayer) { playerWindowState.isMinimized = false }
        }

        Tray(
            icon = if (denpaState.playState == DenpaPlayer.PlayState.PLAYING)
                painterResource(Res.drawable.pause_icon)
            else
                painterResource(Res.drawable.play_icon),
            tooltip = "DenpaPlayer",
            onAction = {
                if (denpaState.playState == DenpaPlayer.PlayState.PLAYING)
                    denpaState.denpaPlayer.pause()
                else
                    denpaState.denpaPlayer.resume()
            },
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
    denpaState: DenpaState,
    windowState: WindowState
) {

    var title by remember { mutableStateOf(if (denpaState.settings.japaneseTitle) appNameJp else appNameEng) }

    LaunchedEffect(denpaState.settings) {
        val setts = loadSettings()

        if (!discordRichDisabled && !setts.discordIntegration)
            DiscordRPC.discordClearPresence()

        discordRichDisabled = !setts.discordIntegration

        when (setts.theme) {
            Themes.WHITE -> Colors.whiteTheme()
            Themes.VIOL -> Colors.darkTheme()
            Themes.PINK -> Colors.pinkTheme()
        }

        title = if (denpaState.settings.japaneseTitle) appNameJp else appNameEng
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = title,
        resizable = true,
        state = windowState,
        undecorated = true,
        icon = painterResource(Res.drawable.denpa),
        alwaysOnTop = denpaState.settings.alwaysOnTop
    ) {

        window.addComponentListener(ShapeOnResize(window))
        window.minimumSize = Dimension(minWindowWidth, minWindowHeight)
        val exitApp = ::exitApplication
        val minimize = {
            windowState.isMinimized = windowState.isMinimized.not()
        }

        DenpaScreen(
            denpaState,
            Modifier.fillMaxSize()
                .border(1.8.dp, Colors.objectPrimary, RoundedCornerShape(10.dp))
                .background(Colors.backgroundPrimary)
                .onGloballyPositioned {
                    denpaState.height = it.size.height
                    denpaState.width = it.size.width
                }
        ) {
            WindowDraggableArea {
                TopPanel(denpaState, exitApp, minimize)
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TrackProgressBarWindow(denpaPlayer: DenpaPlayer<DenpaTrack>, onCloseRequest: () -> Unit) {
    val state = rememberWindowState(
        size = with(LocalDensity.current) { headerMinSize },
        position = WindowPosition(
            Alignment.TopCenter
        )
    )

    Window(
        onCloseRequest = onCloseRequest,
        title = "Track Progress Bar",
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
        val width = if (window.width > maxWindowWidth)
            maxWindowWidth else window.width
        val height = if (window.height > maxWindowHeight)
            maxWindowHeight else window.height
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import denpaplayer.composeapp.generated.resources.Res
import denpaplayer.composeapp.generated.resources.denpa
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import java.awt.geom.RoundRectangle2D

@OptIn(ExperimentalResourceApi::class)
fun main() = application {
    val state = rememberWindowState(
        //175 with top bar
        width = 400.dp, height = 175.dp, position = WindowPosition(Alignment.Center)
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "DenpaPlayer",
        resizable = true,
        state = state,
        undecorated = true,
        icon = painterResource(Res.drawable.denpa)
    ) {
        window.addComponentListener(object : java.awt.event.ComponentAdapter() {
            override fun componentResized(e: java.awt.event.ComponentEvent?) {
                window.shape = RoundRectangle2D.Double(
                    0.0,
                    0.0,
                    window.width.toDouble(),
                    window.height.toDouble(),
                    20.0,
                    20.0
                )
            }
        })

        val exitApp = {
            exitApplication()
        }

        val minimize = {
            state.isMinimized = state.isMinimized.not()
        }

        DenpaScreen(Modifier.fillMaxSize().border(1.8.dp, Color.Black, RoundedCornerShape(10.dp))) {
            WindowDraggableArea {
                TopPanel(exitApp, minimize)
            }
        }
    }
}
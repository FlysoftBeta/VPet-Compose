import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.theme.AppTheme


@Composable
@Preview
fun WindowScope.App() {
    AppTheme(useDarkTheme = true) {
        Pet()
    }
}

@OptIn(ExperimentalComposeUiApi::class)


fun main() = application {
    ManagersProvider {
        Window(
            onCloseRequest = ::exitApplication,
            transparent = true,
            undecorated = true,
            resizable = false,
            alwaysOnTop = true,
            title = "",
            state = rememberWindowState(size = DpSize.Unspecified)
        ) {
            Box(modifier = Modifier.size(300.dp)) {
                App()
            }
        }
    }
}
package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.awaitApplication
import androidx.compose.ui.window.rememberDialogState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ui.theme.AppTheme
import utils.resourceDirectory

fun app() = runBlocking {
    awaitApplication {
        ManagersProvider {
            val resourceManager = LocalManagers.current.resourceManager

            var loading: String? by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                loading = "正在加载资源..."
                resourceManager.loadFromDirectories(
                    listOf(resourceDirectory.resolve("default_mod"))
                )
                delay(1000)
                loading = null
            }

            Dialog(
                onCloseRequest = ::exitApplication,
                transparent = true,
                undecorated = true,
                resizable = false,
                title = "",
                state = rememberDialogState(size = DpSize(300.dp, 300.dp))
            ) {
                window.isAlwaysOnTop = true
                AppTheme(useDarkTheme = true) {
                    loading?.let { loading ->
                        Surface {
                            Box(modifier = Modifier.padding(8.dp)) { Text(loading) }
                        }
                    } ?: Box(modifier = Modifier.fillMaxSize()) {
                        PetStateProvider {
                            PetMenu { Pet() }
                        }
                    }
                }
            }
        }
    }
}
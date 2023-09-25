package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.awaitApplication
import androidx.compose.ui.window.rememberDialogState
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        loading?.let { loading ->
                            Card {
                                Box(modifier = Modifier.padding(8.dp)) { Text(loading) }
                            }
                        } ?: PetStateProvider {
                            PetMenu { Pet() }
                        }
                    }
                }
            }
        }
    }
}
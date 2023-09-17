import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.runBlocking
import platform.audio.AudioManager
import resource.FrameManager
import resource.ResourceManager
import java.io.File

@Composable
fun ManagersProvider(content: @Composable () -> Unit = {}) {
    CompositionLocalProvider(LocalManagers provides (object : Managers {
        override val audioManager: AudioManager = AudioManager()
        override val resourceManager: ResourceManager = ResourceManager()
        override val frameManager: FrameManager = FrameManager()
    })) {
        val resourceManager = LocalManagers.current.resourceManager

        runBlocking {
            resourceManager.loadFromDirectories(
                listOf(
                    File(System.getProperty("compose.application.resources.dir")).resolve(
                        "default_mod"
                    )
                )
            )
        }

        content()
    }
}

val LocalManagers = compositionLocalOf<Managers> {
    error("LocalManagers not present")
}

interface Managers {
    val audioManager: AudioManager
    val resourceManager: ResourceManager
    val frameManager: FrameManager
}
package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import platform.audio.AudioManager
import resource.ResourceManager
import resource.frame.FrameManager

@Composable
fun ManagersProvider(content: @Composable () -> Unit = {}) {
    CompositionLocalProvider(LocalManagers provides (object : Managers {
        override val audioManager: AudioManager = AudioManager()
        override val resourceManager: ResourceManager = ResourceManager()
        override val frameManager: FrameManager = FrameManager()
    })) {
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
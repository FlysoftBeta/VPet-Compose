import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.withDI
import platform.audio.AudioManager
import platform.audio.AudioPeakUpdateEvent
import resource.FrameManager
import resource.ResourceManager
import resource.pet.PetState
import ui.theme.AppTheme
import java.io.File


@Composable
@Preview
fun App() {
    AppTheme(useDarkTheme = true) {
        Pet()
    }
}

@Composable
fun Pet() {
    val audioManager: AudioManager by rememberInstance()
    val resourceManager: ResourceManager by rememberInstance()
    val frameManager: FrameManager by rememberInstance()

    var climaxLevel by remember { mutableStateOf(0) }

    val preferredFrameList by remember { mutableStateOf(resourceManager.pet!!.defaultResource.frameList) }
    var frameList by remember(preferredFrameList) { mutableStateOf(preferredFrameList) }

    val petState by remember { mutableStateOf(PetState.HAPPY) }
    var variant by remember(petState) { mutableStateOf("A") }

    val loops by remember(frameList, petState, variant) {
        mutableStateOf(
            frameList[petState]?.get(
                variant
            )
        )
    }
    var loopIdx by remember(petState, variant) { mutableStateOf(0) }
    val loop by remember(loops, loopIdx) {
        mutableStateOf(
            loops?.getOrNull(loopIdx)
        )
    }

    var frameIdx by remember { mutableStateOf(0) }
    val frame by remember(loop, frameIdx) { mutableStateOf(loop?.getOrNull(frameIdx)) }

    var images: List<ImageBitmap>? by remember { mutableStateOf(null) }
    val frameImageBitmap by remember(images, frameIdx) { mutableStateOf(images?.getOrNull(frameIdx)) }

    suspend fun updateLoop(loop: Int = 0) {
        var newLoopIdx = loop
        // Keep searching until we find a new loop that exists
        while (loops?.getOrNull(newLoopIdx) == null) {
            if (newLoopIdx == loops?.size)
                newLoopIdx = 0
            else newLoopIdx++
        }
        images = frameManager.loadFrames(loops!![newLoopIdx]!!)
        loopIdx = newLoopIdx
        frameIdx = 0
    }

    LaunchedEffect(loop, frame, frame) {
        if (loop != null && images == null) {
            images = frameManager.loadFrames(loop!!)
        }

        val newFrameIdx = frameIdx + 1
        // If we're at the end of the loop, go to a new loop
        if (loop?.getOrNull(newFrameIdx) == null) {
            updateLoop(loopIdx + 1)
            frame?.let { delay(it.duration) }
        } else {
            frame?.let { delay(it.duration) }
            frameIdx = newFrameIdx
        }
    }

    LaunchedEffect(loops) {
        updateLoop(0)
    }

    LaunchedEffect(climaxLevel) {
        // We add a delay here to avoid changing frameList too often
        delay(3000)
        if (climaxLevel > 0) {
            frameList = resourceManager.pet!!.climaxResource.frameList
            variant = if (climaxLevel == 1) "B" else "Single"
        } else frameList = resourceManager.pet!!.defaultResource.frameList
    }

    LaunchedEffect(audioManager) {
        audioManager.subscribe<AudioPeakUpdateEvent> {
            climaxLevel = if (it.peak > 0.7) 2 else if (it.peak > 0.1) 1 else 0
        }
    }

    Box(modifier = Modifier.offset()) {}

    frameImageBitmap?.let { Image(bitmap = it, modifier = Modifier.fillMaxSize(), contentDescription = null) }
}

@Composable
fun Provider(content: @Composable () -> Unit = {}) {
    withDI(DI {
        bindSingleton { AudioManager() }
        bindSingleton { FrameManager() }
        bindSingleton { ResourceManager() }
    }) {
        val resourceManager: ResourceManager by rememberInstance()
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

fun main() = application {
    Provider {
        val state = rememberWindowState(size = DpSize.Unspecified)

        Window(
            onCloseRequest = ::exitApplication,
            transparent = true,
            undecorated = true,
            resizable = false,
            alwaysOnTop = true,
            title = "",
            state = state
        ) {
            LaunchedEffect(state) {
                snapshotFlow { state.position }
                    .filter { it.isSpecified }
                    .onEach { windowPosition ->
                        println(windowPosition)
                    }
                    .launchIn(this)
            }
            Box(modifier = Modifier.size(300.dp).background(Color.Red)) {

                WindowDraggableArea {
                    App()
                }
            }
        }
    }
}
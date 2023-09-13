import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
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
import resource.FrameList
import resource.FrameManager
import resource.ResourceManager
import resource.pet.PetState
import ui.theme.AppTheme
import java.io.File


@Composable
@Preview
fun WindowScope.App(windowState: WindowState) {
    AppTheme(useDarkTheme = true) {
        Pet()
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun WindowScope.Pet() {
    val audioManager: AudioManager by rememberInstance()
    val resourceManager: ResourceManager by rememberInstance()
    val frameManager: FrameManager by rememberInstance()

    var climaxLevel by remember { mutableStateOf(0) }

    val preferredFrameList: FrameList? by remember {
        mutableStateOf(
            resourceManager.pet!!.defaultResource.allFrameList["A"]!!
        )
    }
    val breakableFrameListArray by remember { mutableStateOf(arrayOfNulls<FrameList>(10)) }
    val onceFrameListStack by remember { mutableStateOf(ArrayDeque<FrameList>()) }
    val frameList = onceFrameListStack.firstOrNull() ?: breakableFrameListArray.firstOrNull { it != null }
    ?: preferredFrameList

    val petState by remember { mutableStateOf(PetState.HAPPY) }

    val loops by remember(frameList, petState) {
        mutableStateOf(
            frameList?.get(petState)
        )
    }
    var loopIdx by remember(petState) { mutableStateOf(0) }
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
            if (newLoopIdx == loops?.size) {
                if (frameList == onceFrameListStack.firstOrNull())
                    onceFrameListStack.removeFirst()
                newLoopIdx = 0
            } else newLoopIdx++
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
        breakableFrameListArray[1] = if (climaxLevel > 0) {
            resourceManager.pet!!.climaxResource.allFrameList.get(if (climaxLevel == 1) "B" else "Single")
        } else null
    }

    LaunchedEffect(audioManager) {
        audioManager.subscribe<AudioPeakUpdateEvent> {
            climaxLevel = if (it.peak > 0.7) 2 else if (it.peak > 0.1) 1 else 0
        }
    }

    val density = LocalDensity.current.density
    var sizeInDp by remember { mutableStateOf(DpSize.Zero) }
    Box(modifier = Modifier.fillMaxSize().onSizeChanged { sizeInPx ->

        sizeInDp = DpSize(
            width = (sizeInPx.width / density).dp,
            height = (sizeInPx.height / density).dp
        )
    }) {
        val draggingFrameList = resourceManager.pet!!.activeDragResource.allFrameList.get("")!!
        val stopDraggingFrameList = resourceManager.pet!!.lazyDragResource.allFrameList.get("C")!!
        val headRect = resourceManager.pet?.headRect

        WindowDraggableArea(
            modifier = Modifier.size(
                headRect?.second?.first?.let { it * sizeInDp.width.value }?.dp ?: 0.dp,
                headRect?.second?.second?.let { it * sizeInDp.height.value }?.dp ?: 0.dp
            ).offset(
                headRect?.first?.first?.let { it * sizeInDp.width.value }?.dp ?: 0.dp,
                headRect?.first?.second?.let { it * sizeInDp.height.value }?.dp ?: 0.dp
            ).onPointerEvent(PointerEventType.Press) {
                breakableFrameListArray[0] = draggingFrameList
            }.onPointerEvent(PointerEventType.Release) {
                breakableFrameListArray[0] = null
                onceFrameListStack.addFirst(stopDraggingFrameList)
            }
        )

        frameImageBitmap?.let { Image(bitmap = it, modifier = Modifier.fillMaxSize(), contentDescription = null) }
    }
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
        val windowState = rememberWindowState(size = DpSize.Unspecified)

        Window(
            onCloseRequest = ::exitApplication,
            transparent = true,
            undecorated = true,
            resizable = false,
            alwaysOnTop = true,
            title = "",
            state = windowState
        ) {
            LaunchedEffect(windowState) {
                snapshotFlow { windowState.position }
                    .filter { it.isSpecified }
                    .onEach { windowPosition ->
//                        println(windowPosition)
                    }
                    .launchIn(this)
            }

            Box(modifier = Modifier.size(300.dp)) {
                App(windowState)
            }
        }
    }
}
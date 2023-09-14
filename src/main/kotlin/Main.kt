import androidx.compose.desktop.ui.tooling.preview.Preview
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
import resource.Frame
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

@OptIn(ExperimentalComposeUiApi::class)
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
    var loopIdx by remember(loops) { mutableStateOf(0) }
    var loop: ArrayList<Frame>? by remember {
        mutableStateOf(
            null
        )
    }

    var frameIdx by remember(loop) { mutableStateOf(0) }
    var frame: Frame? by remember { mutableStateOf(null) }

    var images: List<ImageBitmap>? by remember { mutableStateOf(null) }
    var frameImageBitmap: ImageBitmap? by remember { mutableStateOf(null) }

    var repeatedCount by remember(loop) { mutableStateOf(0) }

    // Listen loopIdx changes and load corresponding frameList
    LaunchedEffect(loopIdx, loops, repeatedCount) {
        if (loops?.getOrNull(loopIdx) == null) {
            if (loopIdx == loops?.size) {
                loopIdx = 0
                repeatedCount++
            } else loopIdx++
        } else if (repeatedCount == 1 && onceFrameListStack.firstOrNull() == frameList) {
            onceFrameListStack.removeFirst()
            loopIdx = 0
        } else {
            val newLoop = loops?.getOrNull(loopIdx)
            // Note that loadFrames is time-consuming,
            // so there shouldn't be any recompositions before this
            // in order to avoid flickering
            newLoop?.let { images = frameManager.loadFrames(it) }
            loop = newLoop
            frameIdx = 0
        }
    }

    // Listen frameIdx changes and load corresponding frame
    DisposableEffect(frameIdx, loop) {
        if (loop?.getOrNull(frameIdx) == null) loopIdx++ else {
            frame = loop?.getOrNull(frameIdx)
            frameImageBitmap = images?.getOrNull(frameIdx)
        }
        onDispose { }
    }

    // Play frames
    LaunchedEffect(frame) {
        if (frame == null)
            loopIdx++
        else {
            frame?.let { delay(it.duration) }
            frameIdx++
        }
    }

    LaunchedEffect(climaxLevel) {
        // We add a delay here to avoid changing frameList too often
        delay(3000)
        breakableFrameListArray[1] = if (climaxLevel > 0) {
            resourceManager.pet!!.climaxResource.allFrameList[if (climaxLevel == 1) "B" else "Single"]
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
        var draggingState by remember { mutableStateOf(false) }
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
                draggingState = true
                breakableFrameListArray[0] = draggingFrameList
            }.onPointerEvent(PointerEventType.Release) {
                if (draggingState) {
                    draggingState = false
                    breakableFrameListArray[0] = null
                    onceFrameListStack.addFirst(stopDraggingFrameList)
                }
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
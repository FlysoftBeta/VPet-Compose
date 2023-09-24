package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.window.WindowScope
import kotlinx.coroutines.delay
import platform.audio.AudioPeakUpdateEvent
import resource.frame.Frame
import resource.frame.FrameList
import state.FeelingType

class PetFrameResource(defaultFrameList: FrameList?) {
    var forced by mutableStateOf<FrameList?>(null)
    var playOnce = mutableStateListOf<FrameList>()
    var breakable by mutableStateOf<FrameList?>(null)
    var preferred by mutableStateOf<FrameList?>(null)
    var idle by mutableStateOf<FrameList?>(defaultFrameList)
}

@Composable
fun WindowScope.Pet() {
    val resourceManager = LocalManagers.current.resourceManager
    val pet = resourceManager.pet

    val state = LocalPetState.current
    val feelingType = state.feelingType

    val audioManager = LocalManagers.current.audioManager
    var climaxLevel by remember { mutableStateOf(0) }
    val climaxResource =
        remember {
            mapOf(
                1 to pet.climaxResource?.allFrameList?.get("B"),
                2 to pet.climaxResource?.allFrameList?.get("Single")
            )
        }

    val frameManager = LocalManagers.current.frameManager
    val frameResource = remember { PetFrameResource(pet.defaultResource?.allFrameList?.get("A")) }
    val frameList =
        frameResource.forced ?: frameResource.playOnce.firstOrNull()
        ?: frameResource.preferred ?: frameResource.breakable ?: frameResource.idle

    val loops = remember(frameList, feelingType) {
        frameList?.get(feelingType) ?: frameList?.get(FeelingType.NORMAL)
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

    var repeatedCount by remember(loops) { mutableStateOf(0) }

    DisposableEffect(state.action) {
        state.action?.let { action ->
            val actionResource = pet.actions.firstOrNull { actionResource -> actionResource.action == action }
            actionResource?.allFrameList?.get("A")?.let { enterFrameList ->
                frameResource.playOnce.add(enterFrameList)
            }
            actionResource?.allFrameList?.get("B")?.let { doingFrameList ->
                frameResource.preferred = doingFrameList
            }
            onDispose {
                frameResource.preferred = null
                actionResource?.allFrameList?.get("C")?.let { exitFrameList ->
                    frameResource.playOnce.add(exitFrameList)
                }
            }
        } ?: onDispose {}
    }

    // Listen loopIdx changes and load corresponding frameList
    LaunchedEffect(loopIdx, loops, repeatedCount) {
        if (loops?.getOrNull(loopIdx) == null) {
            if (loopIdx == loops?.size) {
                loopIdx = 0
                repeatedCount++
            } else loopIdx++
        } else if (repeatedCount >= 1 && frameList == frameResource.playOnce.firstOrNull()) {
            frameResource.playOnce.removeFirst()
            loopIdx = 0
        } else {
            val newLoop = loops.getOrNull(loopIdx)
            // Note that loadFrames is time-consuming,
            // so there shouldn't be any recompositions before this
            // in order to avoid flickering
            newLoop?.let { images = frameManager.loadFrames(it) }
            loop = newLoop
            frameIdx = 0
        }
    }

    // Listen frameIdx changes and load corresponding frame
    LaunchedEffect(frameIdx, loop) {
        if (loop?.getOrNull(frameIdx) == null) loopIdx++ else {
            frame = loop?.getOrNull(frameIdx)
            frameImageBitmap = images?.getOrNull(frameIdx)
            frame?.let { delay(it.duration) }
            frameIdx++
        }
    }

    LaunchedEffect(climaxLevel, feelingType) {
        // We add a delay here to avoid changing frameList too often
        delay(3000)

        if (feelingType != FeelingType.ILL && climaxLevel != 0) frameResource.breakable = climaxResource[climaxLevel]
        else if (climaxResource.containsValue(frameResource.breakable)) frameResource.breakable = null
    }

    LaunchedEffect(audioManager) {
        audioManager.subscribe<AudioPeakUpdateEvent> {
            climaxLevel = if (it.peak > 0.7) 2 else if (it.peak > 0.1) 1 else 0
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Drag(feelingType, frameResource)
        frameImageBitmap?.let { Image(bitmap = it, modifier = Modifier.fillMaxSize(), contentDescription = null) }
    }
}
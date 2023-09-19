import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.window.WindowScope
import kotlinx.coroutines.delay
import platform.audio.AudioPeakUpdateEvent
import resource.Frame
import resource.FrameList
import resource.pet.PetFeeling

class PetFrameResource(defaultFrameList: FrameList) {
    var forced: FrameList? = null
    var playOnce: FrameList? = null
    var breakable: FrameList? = null
    var preferred: FrameList? = defaultFrameList
}

@Composable
fun WindowScope.Pet() {
    val frameManager = LocalManagers.current.frameManager
    val resourceManager = LocalManagers.current.resourceManager
    val pet = resourceManager.pet!!

    val audioManager = LocalManagers.current.audioManager
    var climaxLevel by remember { mutableStateOf(0) }
    val climaxResource =
        remember {
            mapOf(
                1 to pet.climaxResource.allFrameList["B"]!!,
                2 to pet.climaxResource.allFrameList["Single"]!!
            )
        }

    val frameResource by remember { mutableStateOf(PetFrameResource(pet.defaultResource.allFrameList["A"]!!)) }
    val frameList =
        remember(
            frameResource.forced,
            frameResource.playOnce,
            frameResource.breakable,
            frameResource.preferred
        ) {
            frameResource.forced ?: frameResource.playOnce ?: frameResource.breakable
            ?: frameResource.preferred
        }

    val petFeeling by remember { mutableStateOf(PetFeeling.HAPPY) }

    val loops = remember(frameList, petFeeling) {
        frameList?.get(petFeeling)
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
        } else if (repeatedCount >= 1 && frameList == frameResource.playOnce) {
            frameResource.playOnce = null
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


        if (climaxLevel != 0) frameResource.breakable = climaxResource[climaxLevel]
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
        Drag(petFeeling, frameResource)
        frameImageBitmap?.let { Image(bitmap = it, modifier = Modifier.fillMaxSize(), contentDescription = null) }
    }
}
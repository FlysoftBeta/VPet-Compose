import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.size
import androidx.compose.ui.window.WindowScope
import kotlinx.coroutines.delay
import resource.pet.PetState
import utils.offset
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter

private class DragHandler(private val window: Window, private val headDragPoint: IntOffset) {
    private val dragListener = object : MouseMotionAdapter() {
        override fun mouseDragged(event: MouseEvent) = drag()
    }
    private val removeListener = object : MouseAdapter() {
        override fun mouseReleased(event: MouseEvent) {
            window.removeMouseMotionListener(dragListener)
            window.removeMouseListener(this)
        }
    }

    fun register() {
        drag()
        window.addMouseListener(removeListener)
        window.addMouseMotionListener(dragListener)
    }

    private fun drag() {
        val point = MouseInfo.getPointerInfo().location.toComposeOffset()
        val location = point - headDragPoint
        window.location = location.toPoint()
    }

    private fun Point.toComposeOffset() = IntOffset(x, y)
    private fun IntOffset.toPoint() = Point(x, y)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WindowScope.Drag(petState: PetState, frameResource: PetFrameResource) {
    val resourceManager = LocalManagers.current.resourceManager
    val pet = resourceManager.pet!!

    val density = LocalDensity.current.density
    var boxSize by remember { mutableStateOf(DpSize.Zero) }

    val headDragPoint = remember(pet, petState, boxSize) {
        (pet.headDragPoints[petState]!! * boxSize).offset
    }
    val headRect = remember(pet.headRect, boxSize) { pet.headRect * boxSize }

    var pressState by remember { mutableStateOf(false) }
    var dragState by remember { mutableStateOf(false) }
    val dragHandler = remember(headDragPoint) {
        val headDragPointInInt =
            IntOffset((headDragPoint.x.value).toInt(), (headDragPoint.y.value).toInt())
        DragHandler(window, headDragPointInInt)
    }
    val dragFrameList = remember { pet.activeDragResource.allFrameList[""]!! }
    val dragStopFrameList = remember { pet.lazyDragResource.allFrameList["C"]!! }

    LaunchedEffect(pressState) {
        if (pressState) {
            delay(1000)
            dragState = true
            frameResource.forced = dragFrameList
            delay(100)
            dragHandler.register()
        } else {
            if (dragState) {
                dragState = false
                frameResource.forced = null
                frameResource.playOnce = dragStopFrameList
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .onSizeChanged { boxSize = DpSize((it.width / density).dp, (it.height / density).dp) }
    ) {
        Box(
            modifier = Modifier.size(headRect.size).offset(
                headRect.left, headRect.top
            ).onPointerEvent(PointerEventType.Press) {
                pressState = true
            }.onPointerEvent(PointerEventType.Release) {
                pressState = false
            }
        )
    }
}
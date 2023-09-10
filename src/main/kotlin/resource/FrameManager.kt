package resource

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import utils.FixedSizeLinkedHashMap

class FrameManager {
    private val loadedFrames = FixedSizeLinkedHashMap<Frame, ImageBitmap>(160)

    suspend fun loadFrames(frames: List<Frame>): List<ImageBitmap> {
        return frames.map { frame ->
            loadedFrames.getOrPut(frame) {
                Image.makeFromEncoded(withContext(Dispatchers.IO) {
                    frame.file.readBytes()
                }).toComposeImageBitmap()
            }
        }
    }
}
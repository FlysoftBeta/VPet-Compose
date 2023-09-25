package resource.frame

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.SamplingMode
import utils.FixedSizeLinkedHashMap

class FrameManager {
    private val loadedFrames = FixedSizeLinkedHashMap<Frame, ImageBitmap>(40)

    suspend fun loadFrames(frames: List<Frame>): List<ImageBitmap> {
        return frames.map { frame ->
            loadedFrames.getOrPut(frame) {
                val image = Image.makeFromEncoded(withContext(Dispatchers.IO) {
                    frame.file.readBytes()
                })
                val bitmap = Bitmap()
                bitmap.setImageInfo(ImageInfo(500, 500, image.colorType, image.alphaType))
                bitmap.allocPixels()
                val pixmap = bitmap.peekPixels()!!
                image.scalePixels(pixmap, SamplingMode.LINEAR, false)
                Image.makeFromPixmap(pixmap).toComposeImageBitmap()
            }
        }
    }
}
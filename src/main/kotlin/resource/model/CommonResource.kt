package resource.model

import androidx.compose.runtime.Immutable
import resource.frame.AllFrameList
import java.io.File

@Immutable
open class CommonResource(resourceDirectory: File) {
    val allFrameList: AllFrameList = AllFrameList.fromDirectory(resourceDirectory)
}
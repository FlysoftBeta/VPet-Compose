package resource.common

import androidx.compose.runtime.Immutable
import resource.AllFrameList
import java.io.File

@Immutable
open class CommonResource(resourceDirectory: File) {
    val allFrameList: AllFrameList = AllFrameList.fromDirectory(resourceDirectory)
}
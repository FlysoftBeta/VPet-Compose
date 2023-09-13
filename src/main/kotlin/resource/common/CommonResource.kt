package resource.common

import resource.AllFrameList
import java.io.File

data class CommonResource(val allFrameList: AllFrameList) {
    companion object {
        fun fromResourceDirectory(resourceDirectory: File): CommonResource {
            return CommonResource(
                allFrameList = AllFrameList.fromDirectory(resourceDirectory)
            )
        }
    }
}
package resource.common

import resource.FrameList
import java.io.File

data class CommonResource(val frameList: FrameList) {
    companion object {
        fun fromResourceDirectory(resourceDirectory: File): CommonResource {
            return CommonResource(
                frameList = FrameList.fromDirectory(resourceDirectory)
            )
        }
    }
}
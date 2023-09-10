package resource.move

import resource.FrameList
import resource.RawResource
import java.io.File

data class MoveResource(val frameList: FrameList, val id: String, val trigger: MoveTrigger) {
    companion object {
        fun fromRawResource(directory: File, rawResource: RawResource): MoveResource {
            val id = rawResource["graph"]!!
            val resourceDirectory = directory.resolve(id)
            return MoveResource(
                frameList = FrameList.fromDirectory(resourceDirectory),
                id = id,
                trigger = MoveTrigger.fromRawResource(rawResource)
            )
        }
    }
}
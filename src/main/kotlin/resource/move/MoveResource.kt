package resource.move

import resource.AllFrameList
import resource.RawResource
import java.io.File

data class MoveResource(val allFrameList: AllFrameList, val id: String, val trigger: MoveTrigger) {
    companion object {
        fun fromRawResource(directory: File, rawResource: RawResource): MoveResource {
            val id = rawResource["graph"]!!
            val resourceDirectory = directory.resolve(id)
            return MoveResource(
                allFrameList = AllFrameList.fromDirectory(resourceDirectory),
                id = id,
                trigger = MoveTrigger.fromRawResource(rawResource)
            )
        }
    }
}
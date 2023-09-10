package resource.pet

import resource.RawResourceList
import resource.common.CommonResource
import resource.move.MoveResource
import java.io.File

data class PetResource(
    val directory: File,
    val moveResourceList: List<MoveResource>,
    val defaultResource: CommonResource,
    val climaxResource: CommonResource
) {
    companion object {
        fun fromRawResourceList(directory: File, rawResourceList: RawResourceList): PetResource {
            return PetResource(
                directory = directory,
                moveResourceList = rawResourceList["move"]!!.map { rawResource ->
                    MoveResource.fromRawResource(directory.resolve("MOVE"), rawResource)
                },
                defaultResource = CommonResource.fromResourceDirectory(directory.resolve("Default")),
                climaxResource = CommonResource.fromResourceDirectory(directory.resolve("Music"))
            )
        }
    }
}
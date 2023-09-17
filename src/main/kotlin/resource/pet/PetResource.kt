package resource.pet

import resource.RawResourceList
import resource.common.CommonResource
import resource.move.MoveResource
import utils.PercentageOffset
import utils.PercentageRect
import utils.PercentageSize
import java.io.File
import java.util.*

data class PetResource(
    val directory: File,
    val headRect: PercentageRect,
    val headDragPoints: EnumMap<PetState, PercentageSize>,
    val moveResourceList: List<MoveResource>,
    val defaultResource: CommonResource,
    val activeDragResource: CommonResource,
    val lazyDragResource: CommonResource,
    val climaxResource: CommonResource
) {
    companion object {
        fun fromRawResourceList(directory: File, rawResourceList: RawResourceList): PetResource {
            val rawHead = rawResourceList["touchhead"]!![0]
            val headRect = PercentageRect(
                PercentageOffset((rawHead["px"]!!.toFloat() / 500), (rawHead["py"]!!.toFloat() / 500)),
                PercentageSize((rawHead["sw"]!!.toFloat() / 500), (rawHead["sh"]!!.toFloat() / 500))
            )

            val rawRaisePoint = rawResourceList["raisepoint"]!![0]
            val headDragPoints = EnumMap<PetState, PercentageSize>(PetState::class.java)
            PetState.values().forEach { petState ->
                val x = rawRaisePoint[petState.internalName.lowercase() + "_x"]!!.toFloat() / 500
                val y = rawRaisePoint[petState.internalName.lowercase() + "_y"]!!.toFloat() / 500
                headDragPoints[petState] = PercentageSize(x, y)
            }

            return PetResource(
                directory = directory,
                moveResourceList = rawResourceList["move"]!!.map { rawResource ->
                    MoveResource.fromRawResource(directory.resolve("MOVE"), rawResource)
                },
                defaultResource = CommonResource.fromResourceDirectory(directory.resolve("Default")),
                activeDragResource = CommonResource.fromResourceDirectory(
                    directory.resolve("Raise").resolve("Raised_Dynamic"),
                ),
                lazyDragResource = CommonResource.fromResourceDirectory(
                    directory.resolve("Raise").resolve("Raised_Static"),
                ),
                climaxResource = CommonResource.fromResourceDirectory(directory.resolve("Music")),
                headRect = headRect,
                headDragPoints = headDragPoints
            )
        }
    }
}
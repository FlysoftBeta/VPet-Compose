package resource.pet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import resource.ActionResource
import resource.RawResourceList
import resource.common.CommonResource
import resource.move.MoveResource
import state.FeelingType
import state.action.Action
import utils.PercentageOffset
import utils.PercentageRect
import utils.PercentageSize
import java.io.File
import java.util.*

class PetResource {
    var resourceName by mutableStateOf("Default Resource")
    var petName by mutableStateOf("Default Pet")
    var description by mutableStateOf("")
    val actions = mutableStateListOf<ActionResource>()
    var headRect by mutableStateOf<PercentageRect?>(null)
    var headDragPoints by mutableStateOf<EnumMap<FeelingType, PercentageSize>?>(null)
    var moveResourceList by mutableStateOf<List<MoveResource>?>(null)
    var defaultResource by mutableStateOf<CommonResource?>(null)
    var activeDragResource by mutableStateOf<CommonResource?>(null)
    var lazyDragResource by mutableStateOf<CommonResource?>(null)
    var climaxResource by mutableStateOf<CommonResource?>(null)

    fun applyRawResourceList(baseDirectory: File, rawResourceList: RawResourceList) {
        val rawPet = rawResourceList["pet"]!![0]
        val directory = baseDirectory.resolve(rawPet["path"]!!)
        rawPet["pet"]?.let { resourceName = it }
        rawPet["petname"]?.let { petName = it }
        rawPet["intor"]?.let { description = it }

        // We do this because in Unix-like operating systems, the path is case-sensitive
        val workResourceDirectories = directory.resolve("WORK").listFiles()?.map { resourceDirectory ->
            Pair(resourceDirectory, resourceDirectory.name.lowercase())
        } ?: emptyList()
        rawResourceList["work"]?.let { rawWork ->
            actions.addAll(rawWork.map { rawAction ->
                val resourceDirectory = workResourceDirectories.first { resourceDirectory ->
                    rawAction["Graph"]!! == resourceDirectory.second
                }.first
                ActionResource(resourceDirectory, Action.fromRawResource(rawAction))
            })
        }

        rawResourceList["touchhead"]?.getOrNull(0)?.let { rawHead ->
            headRect = PercentageRect(
                PercentageOffset((rawHead["px"]!!.toFloat() / 500), (rawHead["py"]!!.toFloat() / 500)),
                PercentageSize((rawHead["sw"]!!.toFloat() / 500), (rawHead["sh"]!!.toFloat() / 500))
            )
        }

        rawResourceList["raisepoint"]?.getOrNull(0)?.let { rawRaisePoint ->
            headDragPoints = EnumMap<FeelingType, PercentageSize>(FeelingType::class.java)
            FeelingType.values().forEach { feeling ->
                val x = rawRaisePoint[feeling.internalName.lowercase() + "_x"]!!.toFloat() / 500
                val y = rawRaisePoint[feeling.internalName.lowercase() + "_y"]!!.toFloat() / 500
                headDragPoints!![feeling] = PercentageSize(x, y)
            }
        }

        rawResourceList["move"]?.let { rawMove ->
            moveResourceList = rawMove.map { rawResource ->
                MoveResource.fromRawResource(directory.resolve("MOVE"), rawResource)
            }
        }

        val defaultDirectory = directory.resolve("Default")
        if (defaultDirectory.isDirectory)
            defaultResource = CommonResource(defaultDirectory)

        val activeDragDirectory = directory.resolve("Raise").resolve("Raised_Dynamic")
        if (activeDragDirectory.isDirectory)
            activeDragResource = CommonResource(activeDragDirectory)

        val lazyDragDirectory = directory.resolve("Raise").resolve("Raised_Static")
        if (lazyDragDirectory.isDirectory)
            lazyDragResource = CommonResource(lazyDragDirectory)

        val climaxDirectory = directory.resolve("Music")
        if (climaxDirectory.isDirectory)
            climaxResource = CommonResource(climaxDirectory)
    }
}
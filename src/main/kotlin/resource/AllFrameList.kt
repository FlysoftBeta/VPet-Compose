package resource

import androidx.compose.runtime.Immutable
import state.FeelingType
import utils.NTuple4
import java.io.File
import kotlin.io.path.Path
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Immutable
class AllFrameList private constructor() :
    HashMap<String, FrameList>() {

    companion object {
        fun fromDirectory(directory: File): AllFrameList {
            val allFrameList = AllFrameList()
            val feelingTypeNames = FeelingType.values()
                .flatMap { petState -> listOfNotNull(petState.internalName, petState.alternativeInternalName) }
            val variants = listOf(
                "A",
                "B",
                "C",
                "Single"
            )
            val loops = 0..4

            feelingTypeNames.flatMap { name ->
                listOf(NTuple4(name, "", 1, Path(name)), *variants.flatMap { variant ->
                    listOf(
                        *listOfNotNull(
                            Path(variant + "_" + name),
                            Path(name + "_" + variant),
                            Path(variant).resolve(name),
                            Path(name).resolve(variant),
                        ).map { path ->
                            NTuple4(name, variant, 1, path)
                        }.toTypedArray(),
                        *loops.flatMap { loop ->
                            val realLoop = loop + 1
                            listOf(
                                Path(variant + "_" + name + "_" + realLoop),
                                Path(variant + "_" + realLoop + "_" + name),
                                Path(name).resolve(variant + "_" + realLoop),
                                Path(name).resolve(realLoop.toString()),
                                Path(variant).resolve(name).resolve(realLoop.toString()),
                                Path(variant).resolve(name + "_" + realLoop.toString()),
                            ).map { path ->
                                NTuple4(name, variant, loop, path)
                            }
                        }.toTypedArray()
                    )
                }.toTypedArray())
            }.forEach { (name, variant, loop, path) ->
                val resDirectory = directory.toPath().resolve(path).toFile()
                if (resDirectory.isDirectory) {
                    resDirectory.listFiles()?.filter { file -> file.extension == "png" }?.mapNotNull { file ->
                        val parts = file.nameWithoutExtension.split("_")
                        val feelingType = FeelingType.fromString(name)!!
                        allFrameList.putIfAbsent(variant, FrameList())
                        allFrameList[variant]!!.putIfAbsent(feelingType, Array(loops.last + 1) { null })
                        allFrameList[variant]!![feelingType]!![loop] ?: let {
                            allFrameList[variant]!![feelingType]!![loop] = ArrayList()
                        }
                        allFrameList[variant]!![feelingType]!![loop]!!.add(
                            Frame(
                                file,
                                (parts.getOrNull(2)?.toInt() ?: 300).toDuration(DurationUnit.MILLISECONDS)
                            )
                        )
                    }
                }
            }

            return allFrameList
        }
    }
}
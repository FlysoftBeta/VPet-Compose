package resource

import resource.pet.PetFeeling
import utils.NTuple4
import java.io.File
import kotlin.io.path.Path
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class AllFrameList private constructor() :
    HashMap<String, FrameList>() {

    companion object {
        fun fromDirectory(directory: File): AllFrameList {
            val allFrameList = AllFrameList()
            val petFeelingNames = PetFeeling.values()
                .flatMap { petState -> listOfNotNull(petState.internalName, petState.alternativeInternalName) }
            val variants = listOf(
                "A",
                "B",
                "C",
                "Single"
            )
            val loops = 0..4

            petFeelingNames.flatMap { name ->
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
                        *loops.flatMap { type ->
                            val realType = type + 1
                            listOf(
                                Path(variant + "_" + name + "_" + realType),
                                Path(name).resolve(variant + "_" + realType),
                                Path(name).resolve(realType.toString()),
                                Path(variant).resolve(name).resolve(realType.toString()),
                                Path(variant).resolve(name + "_" + realType.toString()),
                            ).map { path ->
                                NTuple4(name, variant, type, path)
                            }
                        }.toTypedArray()
                    )
                }.toTypedArray())
            }.forEach { (name, variant, loop, path) ->
                val resDirectory = directory.toPath().resolve(path).toFile()
                if (resDirectory.isDirectory) {
                    resDirectory.listFiles()?.filter { file -> file.extension == "png" }?.mapNotNull { file ->
                        val parts = file.nameWithoutExtension.split("_")
                        val petFeeling = PetFeeling.fromString(name)!!
                        allFrameList.putIfAbsent(variant, FrameList())
                        allFrameList[variant]!!.putIfAbsent(petFeeling, Array(loops.last + 1) { null })
                        allFrameList[variant]!![petFeeling]!![loop] ?: let {
                            allFrameList[variant]!![petFeeling]!![loop] = ArrayList()
                        }
                        allFrameList[variant]!![petFeeling]!![loop]!!.add(
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
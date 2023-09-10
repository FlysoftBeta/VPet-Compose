package resource

import resource.pet.PetResource
import java.io.File

class ResourceManager {
    var pet: PetResource? = null

    suspend fun loadFromDirectories(directoryList: List<File>) {
        directoryList.forEach { directory ->
            directory.resolve("pet").listFiles()?.forEach { file ->
                if (file.isFile && file.extension == "lps") {
                    val petDirectory = file.parentFile.resolve(file.nameWithoutExtension)
                    if (petDirectory.isDirectory)
                        pet = PetResource.fromRawResourceList(petDirectory, RawResourceList.parseFile(file))

                }
            }
        }
    }
}
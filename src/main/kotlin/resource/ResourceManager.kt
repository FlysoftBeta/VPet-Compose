package resource

import resource.model.PetResource
import resource.raw.RawResourceList
import java.io.File

class ResourceManager {
    val pet = PetResource()

    suspend fun loadFromDirectories(directoryList: List<File>) {
        directoryList.forEach { directory ->
            val petDirectory = directory.resolve("pet")
            petDirectory.listFiles()?.forEach { file ->
                if (file.isFile && file.extension == "lps") {
                    pet.applyRawResourceList(petDirectory, RawResourceList.parseFile(file))
                }
            }
        }
    }
}
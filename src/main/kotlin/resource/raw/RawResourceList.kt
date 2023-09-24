package resource.raw

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class RawResourceList private constructor() : HashMap<String, MutableList<RawResource>>() {
    companion object {
        // (Maybe?) a LinePutScript Kotlin implementation
        private fun parseData(data: String): RawResourceList {
            val map = RawResourceList()
            data.split("\n").forEach { row ->
                val tokens = row.split(":|").map { token -> token.split("#").map { pair -> pair.replace("\\n", "\n") } }
                val tagName = tokens[0][0]
                map.putIfAbsent(tagName, mutableListOf())
                map[tagName]!!.add(RawResource(mapOf(*tokens.map { token ->
                    Pair(token[0], token.getOrElse(1) { "" })
                }.toTypedArray())))
            }
            return map
        }

        suspend fun parseFile(file: File): RawResourceList {
            val data = withContext(Dispatchers.IO) {
                file.readText()
            }
            return parseData(data)
        }
    }
}
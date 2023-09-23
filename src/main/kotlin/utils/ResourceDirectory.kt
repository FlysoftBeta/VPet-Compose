package utils

import java.io.File

val resourceDirectory get() = File(System.getProperty("compose.application.resources.dir"))
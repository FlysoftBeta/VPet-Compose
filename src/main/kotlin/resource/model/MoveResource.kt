package resource.model

import androidx.compose.runtime.Immutable
import java.io.File

@Immutable
class MoveResource(directory: File, val trigger: MoveTrigger) : CommonResource(directory)
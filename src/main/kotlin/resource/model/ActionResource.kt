package resource.model

import androidx.compose.runtime.Immutable
import state.action.Action
import java.io.File

@Immutable
class ActionResource(directory: File, val action: Action) : CommonResource(directory)
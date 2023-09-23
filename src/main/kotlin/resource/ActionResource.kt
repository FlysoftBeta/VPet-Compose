package resource

import androidx.compose.runtime.Immutable
import resource.common.CommonResource
import state.action.Action
import java.io.File

@Immutable
class ActionResource(directory: File, val action: Action) : CommonResource(directory)
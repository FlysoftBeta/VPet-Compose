package resource

import androidx.compose.runtime.Immutable
import state.FeelingType
import java.util.*

@Immutable
class FrameList :
    EnumMap<FeelingType, Array<ArrayList<Frame>?>>(FeelingType::class.java)
package resource

import androidx.compose.runtime.Immutable
import resource.pet.PetState
import java.util.*

@Immutable
class FrameList :
    EnumMap<PetState, Array<ArrayList<Frame>?>>(PetState::class.java)
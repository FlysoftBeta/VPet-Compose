package resource

import androidx.compose.runtime.Immutable
import resource.pet.PetFeeling
import java.util.*

@Immutable
class FrameList :
    EnumMap<PetFeeling, Array<ArrayList<Frame>?>>(PetFeeling::class.java)
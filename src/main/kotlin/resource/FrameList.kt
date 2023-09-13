package resource

import resource.pet.PetState
import java.util.*

class FrameList :
    EnumMap<PetState, Array<ArrayList<Frame>?>>(PetState::class.java)
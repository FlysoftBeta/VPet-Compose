package status.action

import status.PetStatus

interface ActionBonus {
    fun getBonusDelta(status: PetStatus, tick: Int): ActionBonusDelta
    val isCheated: Boolean
}
package state.action

import state.State

interface ActionBonus {
    fun getBonusDelta(status: State): ActionBonusDelta
    val isCheated: Boolean
}
package state.action

import resource.RawResource

data class Action(val name: String, val type: ActionType, val bonus: ActionBonus) {
    companion object {
        fun fromRawResource(rawResource: RawResource): Action {
            val actionType = ActionType.fromString(rawResource["Type"])
            return Action(
                name = rawResource["Name"] ?: "Unknown Action",
                type = actionType,
                bonus = TraditionalActionBonus.fromRawResource(actionType, rawResource)
            )
        }
    }
}
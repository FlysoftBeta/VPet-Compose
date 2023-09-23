package state.action

enum class ActionType(val internalName: String) {
    WORK("Work"),
    PLAY("Play"),
    STUDY("Study"),
    OTHER("Other");

    companion object {
        fun fromString(stateName: String?): ActionType {
            return ActionType.values().firstOrNull { it.internalName == stateName } ?: OTHER
        }
    }
}
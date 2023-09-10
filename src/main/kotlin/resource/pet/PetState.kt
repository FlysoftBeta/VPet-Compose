package resource.pet

// Terrible :(
enum class PetState(val stateName: String, val alternativeStateName: String? = null) {
    HAPPY("Happy"),
    NORMAL("Nomal"),
    POOR_CONDITION("PoorCondition"),
    ILL("Ill", "ill");

    companion object {
        fun fromString(stateName: String): PetState? {
            return values().firstOrNull { it.stateName == stateName || it.alternativeStateName == stateName }
        }
    }

    fun getValue(): String = stateName
}
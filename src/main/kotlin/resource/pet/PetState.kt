package resource.pet

// Terrible :(
enum class PetState(val internalName: String, val alternativeInternalName: String? = null) {
    HAPPY("Happy"),
    NORMAL("Nomal"),
    POOR_CONDITION("PoorCondition"),
    ILL("Ill", "ill");

    companion object {
        fun fromString(stateName: String): PetState? {
            return values().firstOrNull { it.internalName == stateName || it.alternativeInternalName == stateName }
        }
    }

    fun getValue(): String = internalName
}
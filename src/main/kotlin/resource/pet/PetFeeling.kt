package resource.pet

// Terrible :(
enum class PetFeeling(val internalName: String, val alternativeInternalName: String? = null) {
    HAPPY("Happy"),
    NORMAL("Nomal"),
    POOR_CONDITION("PoorCondition"),
    ILL("Ill", "ill");

    companion object {
        fun fromString(stateName: String): PetFeeling? {
            return values().firstOrNull { it.internalName == stateName || it.alternativeInternalName == stateName }
        }
    }
}
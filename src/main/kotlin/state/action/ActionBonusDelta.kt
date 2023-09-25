package state.action

data class ActionBonusDelta(
    val money: Double,
    val exp: Double,
    val hunger: Double,
    val thirst: Double,
    val feeling: Double
)

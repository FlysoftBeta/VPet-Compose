package state.action

data class ActionBonusDelta(
    val earnedMoney: Double,
    val earnedExp: Double,
    val spentHunger: Double,
    val spentThirst: Double,
    val spentFeeling: Double
)

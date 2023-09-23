package state.action

data class ActionBonusDelta(
    val earnedMoney: Float,
    val earnedExp: Float,
    val spentHunger: Float,
    val spentThirst: Float,
    val spentFeeling: Float
)

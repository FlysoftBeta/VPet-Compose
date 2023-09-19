package status.action

data class ActionBonusDelta(
    val earnedMoney: Float,
    val earnedExp: Float,
    val spentSatiety: Float,
    val spentThirst: Float,
    val spentFeeling: Float
)

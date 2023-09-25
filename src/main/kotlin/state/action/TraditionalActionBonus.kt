package state.action

import resource.raw.RawResource
import state.State
import kotlin.math.max

data class TraditionalActionBonus(
    val type: ActionType,
    val earnBase: Double,
    val earnPerLevel: Double,
    val earnFinal: Double,
    val spentHunger: Double,
    val spentThirst: Double,
    val spentFeeling: Double,
) : ActionBonus {
    private val isEarningExp = type == ActionType.STUDY

    override fun getBonusDelta(status: State): ActionBonusDelta {
        val efficiency = (if (status.hunger <= 25) 0.25 else 0.5) + (if (status.thirst <= 25) 0.25 else 0.5)
        val earning = max(earnBase * efficiency + status.level * earnPerLevel * (efficiency - 0.5) * 2.0, 0.0)
        return ActionBonusDelta(
            earnedMoney = if (isEarningExp) 0.0 else earning,
            earnedExp = if (isEarningExp) earning else 0.0,
            spentHunger = spentHunger,
            spentThirst = spentThirst,
            spentFeeling = (if (type == ActionType.PLAY) -status.aloneValue else 1.0) * spentFeeling
        )
    }

    // TODO: Implement cheating checking logic
    override val isCheated: Boolean
        get() = false

    companion object {
        fun fromRawResource(type: ActionType, rawResource: RawResource): TraditionalActionBonus {
            return TraditionalActionBonus(
                type = type,
                earnBase = rawResource["MoneyBase"]!!.toDouble(),
                earnPerLevel = rawResource["MoneyLevel"]!!.toDouble(),
                earnFinal = rawResource["FinishBonus"]!!.toDouble(),
                spentHunger = rawResource["StrengthFood"]!!.toDouble(),
                spentThirst = rawResource["StrengthDrink"]!!.toDouble(),
                spentFeeling = rawResource["Feeling"]!!.toDouble(),
            )
        }
    }
}

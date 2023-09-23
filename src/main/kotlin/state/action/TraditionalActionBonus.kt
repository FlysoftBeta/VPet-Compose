package state.action

import resource.RawResource
import state.State
import kotlin.math.max

data class TraditionalActionBonus(
    val type: ActionType,
    val earnBase: Float,
    val earnPerLevel: Float,
    val earnFinal: Float,
    val spentHunger: Float,
    val spentThirst: Float,
    val spentFeeling: Float,
) : ActionBonus {
    private val isEarningExp = type == ActionType.STUDY

    override fun getBonusDelta(status: State): ActionBonusDelta {
        val efficiency = (if (status.hunger <= 25) 0.25f else 0.5f) + (if (status.thirst <= 25) 0.25f else 0.5f)
        val earning = max(earnBase * efficiency + status.level * earnPerLevel * (efficiency - 0.5) * 2.0, 0.0).toFloat()
        return ActionBonusDelta(
            earnedMoney = if (isEarningExp) 0.0f else earning,
            earnedExp = if (isEarningExp) earning else 0.0f,
            spentHunger = spentHunger,
            spentThirst = spentThirst,
            spentFeeling = spentFeeling
        )
    }

    // TODO: Implement cheating checking logic
    override val isCheated: Boolean
        get() = false

    companion object {
        fun fromRawResource(type: ActionType, rawResource: RawResource): TraditionalActionBonus {
            return TraditionalActionBonus(
                type = type,
                earnBase = rawResource["MoneyBase"]!!.toFloat(),
                earnPerLevel = rawResource["MoneyLevel"]!!.toFloat(),
                earnFinal = rawResource["FinishBonus"]!!.toFloat(),
                spentHunger = rawResource["StrengthFood"]!!.toFloat(),
                spentThirst = rawResource["StrengthDrink"]!!.toFloat(),
                spentFeeling = rawResource["Feeling"]!!.toFloat(),
            )
        }
    }
}

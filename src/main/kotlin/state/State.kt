package state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import state.action.Action
import kotlin.math.pow

class State {
    private var _levelExp by mutableStateOf(0.0f)
    val expRequiredToUpgrade get() = (level * 10.0f).pow(2)
    var levelExp
        get() = _levelExp
        set(value) {
            var exp = value
            while (exp > expRequiredToUpgrade) {
                level++
                exp -= expRequiredToUpgrade
            }
            _levelExp = exp
        }
    var level by mutableStateOf(0.0f)
    var money by mutableStateOf(0.0f)

    var hunger by mutableStateOf(100.0f)
    var thirst by mutableStateOf(100.0f)
    var feeling by mutableStateOf(100.0f)
    val feelingType get() = if (feeling < 50) FeelingType.POOR_CONDITION else if (feeling < 70) FeelingType.NORMAL else FeelingType.HAPPY

    var action: Action? by mutableStateOf(null)

    private var tick by mutableStateOf(0)

    fun tick() {
        tick++

        action?.let { action ->
            val delta = action.bonus.getBonusDelta(this)
            levelExp += delta.earnedExp
            money += delta.earnedMoney
            hunger -= delta.spentHunger
            thirst -= delta.spentThirst
            feeling -= delta.spentFeeling
        }
    }
}
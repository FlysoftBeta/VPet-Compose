package state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import state.action.Action
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class State {
    val expRequiredToUpgrade get() = (level * 10.0).pow(2)
    var exp by mutableStateOf(0.0)
    val level get() = if (exp < 0) 1 else ((sqrt(exp) / 10) + 1).toInt()

    var money by mutableStateOf(0.0)

    private var _hunger by mutableStateOf(100.0)
    var hunger
        get() = _hunger
        set(value) {
            _hunger = value.coerceAtLeast(0.0).coerceAtMost(100.0)
        }

    private var _thirst by mutableStateOf(100.0)
    var thirst
        get() = _thirst
        set(value) {
            _thirst = value.coerceAtLeast(0.0).coerceAtMost(100.0)
        }

    private var _feeling by mutableStateOf(60.0)
    var feeling
        get() = _feeling
        set(value) {
            _feeling = value.coerceAtLeast(0.0).coerceAtMost(100.0)
        }

    private var _health by mutableStateOf(100.0)
    var health
        get() = _health
        set(value) {
            _health = value.coerceAtLeast(0.0).coerceAtMost(100.0)
        }

    private var _favorability by mutableStateOf(0.0)
    var favorability
        get() = _favorability
        set(value) {
            _favorability = value.coerceAtLeast(0.0).coerceAtMost(100.0)
        }

    val feelingType: FeelingType
        get() {
            val healthLimit =
                60 - (if (feeling >= 80) 12 else 0) - if (favorability >= 80) 12 else if (favorability >= 40) 6 else 0
            return if (health <= healthLimit / 2) FeelingType.ILL
            else if (health <= healthLimit) FeelingType.POOR_CONDITION
            else {
                val feelingLimit = 90 - if (favorability >= 80) 20 else if (favorability >= 40) 10 else 0
                if (feeling >= feelingLimit) FeelingType.HAPPY
                else if (feeling <= feelingLimit / 2) FeelingType.POOR_CONDITION
                else FeelingType.NORMAL
            }
        }

    var action: Action? by mutableStateOf(null)

    private var tick by mutableStateOf(0)

    fun tick() {
        tick++
        if (feelingType == FeelingType.ILL) action = null
        action?.let { action ->
            val delta = action.bonus.getBonusDelta(this)
            exp += delta.earnedExp
            money += delta.earnedMoney
            hunger -= delta.spentHunger
            thirst -= delta.spentThirst
            feeling -= delta.spentFeeling
        }
        health += (-2.0 +
                (if (hunger <= 25) -2.0 else if (hunger >= 75) Random.nextDouble(1.0, 3.0) else 0.0) +
                (if (thirst <= 25) -2.0 else if (thirst >= 75) Random.nextDouble(1.0, 4.0) else 0.0))
        favorability += if (feeling >= 90) 1.0 else if (feeling <= 25) -1.0 else 0.0
        exp += (if (feeling >= 75) 2.0 else if (feeling <= 25) -1.0 else 0.0) +
                (if (thirst >= 75) Random.nextDouble(1.0, 3.0) else 0.0)
        println("Exp:$exp Level:$level Money:$money Hunger:$hunger Thirst:$thirst Feeling:$feeling Type:${feelingType.name} Health:$health Favorability:$favorability")
    }
}
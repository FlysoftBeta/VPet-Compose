package ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import kotlin.math.roundToLong

private data class StateItem(
    val text: String,
    val current: Double,
    val total: Double
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StatusCard(expanded: Boolean, onDismissRequest: () -> Unit) {
    val state = LocalPetState.current
    val list =
        remember(state.exp, state.expRequiredToUpgrade, state.health, state.feeling, state.hunger, state.thirst) {
            listOf(
                StateItem("经验", state.exp, state.exp + state.expRequiredToUpgrade),
                StateItem("体力", state.health, 100.0),
                StateItem("心情", state.feeling, 100.0),
                StateItem("饱食度", state.hunger, 100.0),
                StateItem("口渴度", state.thirst, 100.0),
            )
        }

    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded
    val transition = updateTransition(expandedStates, "StateCard")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true)
                tween(
                    durationMillis = 120,
                    easing = LinearOutSlowInEasing
                )
            else
                tween(
                    durationMillis = 1,
                    delayMillis = 75 - 1
                )
        }
    ) {
        if (it) 1f else 0.8f
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true)
                tween(durationMillis = 30)
            else
                tween(durationMillis = 75)
        }
    ) {
        if (it) 1f else 0f
    }

    if (expandedStates.targetState || expandedStates.currentState) {
        Popup(onDismissRequest = onDismissRequest, focusable = true, onKeyEvent = { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Escape) {
                onDismissRequest()
                true
            } else false
        }) {
            Card(modifier = Modifier.fillMaxWidth().zIndex(10.0f).graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    list.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.text, modifier = Modifier.weight(0.2f))
                            Column(modifier = Modifier.weight(0.7f)) {
                                Row {
                                    Text(
                                        "${item.current.roundToLong()}/${item.total.roundToLong()}",
                                        fontSize = 0.6.em,
                                        lineHeight = 0.6.em
                                    )
                                }
                                Row {
                                    LinearProgressIndicator(
                                        (item.current / item.total).toFloat(),
                                        modifier = Modifier.weight(0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
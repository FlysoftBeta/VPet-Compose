package ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val progress: StateItemProgress? = null,
    val content: String? = null
)

private data class StateItemProgress(
    val current: Double,
    val total: Double,
    val shouldBeColored: Boolean = true
)

val Green = Color(0xffaacc6c)
val Yellow = Color(0xfffccc50)
val Red = Color(0xffff4c4c)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PetStatusCard(expanded: Boolean, onDismissRequest: () -> Unit) {
    val state = LocalPetState.current
    val list =
        remember(state.exp, state.expRequiredToUpgrade, state.health, state.feeling, state.hunger, state.thirst) {
            listOf(
                StateItem("Lv ${state.level}"),
                StateItem("金钱", content = "$ ${state.money.roundToLong()}"),
                StateItem(
                    "经验",
                    progress = StateItemProgress(
                        state.exp,
                        state.exp + state.expRequiredToUpgrade,
                        shouldBeColored = false
                    )
                ),
                StateItem("心情", progress = StateItemProgress(state.feeling, 100.0)),
                StateItem("体力", progress = StateItemProgress(state.strength, 100.0)),
                StateItem("饱食度", progress = StateItemProgress(state.hunger, 100.0)),
                StateItem("口渴度", progress = StateItemProgress(state.thirst, 100.0)),
                StateItem("健康度", progress = StateItemProgress(state.health, 100.0)),
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
            ElevatedCard(modifier = Modifier.fillMaxWidth().zIndex(10.0f).graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    list.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                item.text,
                                modifier = Modifier.weight(0.2f),
                                fontSize = 0.8.em,
                                lineHeight = 0.8.em
                            )
                            item.progress?.let { progress ->
                                Column(
                                    modifier = Modifier.weight(0.7f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Row {
                                        Text(
                                            "${progress.current.roundToLong()}/${progress.total.roundToLong()}",
                                            fontSize = 0.6.em,
                                            lineHeight = 0.6.em
                                        )
                                    }
                                    val value = remember(progress) { (progress.current / progress.total).toFloat() }
                                    Row {
                                        LinearProgressIndicator(
                                            value,
                                            modifier = Modifier.weight(0.7f).clip(RoundedCornerShape(50)),
                                            color = if (progress.shouldBeColored) if (value < 0.5f) Red else if (value < 0.7f) Yellow else Green else ProgressIndicatorDefaults.linearColor
                                        )
                                    }
                                }
                            } ?: item.content?.let { content ->
                                Text(
                                    content, modifier = Modifier.weight(0.7f),
                                    fontSize = 0.8.em,
                                    lineHeight = 0.8.em
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
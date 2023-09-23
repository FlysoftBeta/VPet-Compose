import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import state.State
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun PetStateProvider(content: @Composable () -> Unit = {}) {
    val state = remember { State() }
    var updateTick by remember { mutableStateOf(false) }

    LaunchedEffect(updateTick) {
        delay(15.0.toDuration(DurationUnit.SECONDS))
        updateTick = !updateTick
        state.tick()
    }

    CompositionLocalProvider(LocalPetState provides state) {
        content()
    }
}

val LocalPetState = compositionLocalOf<State> {
    error("LocalPetState not present")
}
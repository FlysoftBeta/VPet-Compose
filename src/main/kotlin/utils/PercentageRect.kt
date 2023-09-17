package utils

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize

@Immutable
data class PercentageRect(
    @Stable
    val left: Float,
    @Stable
    val top: Float,
    @Stable
    val right: Float,
    @Stable
    val bottom: Float
) {
    constructor(origin: PercentageOffset, size: PercentageSize) :
            this(origin.x, origin.y, origin.x + size.width, origin.y + size.height)

    @Stable
    operator fun times(other: IntSize): IntRect =
        IntRect(
            (left * other.width).toInt(),
            (top * other.height).toInt(),
            (right * other.width).toInt(),
            (bottom * other.height).toInt()
        )

    @Stable
    operator fun times(other: DpSize): DpRect =
        DpRect(
            (other.width * left),
            (other.height * top),
            (other.width * right),
            (other.height * bottom)
        )
}
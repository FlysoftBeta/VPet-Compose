package utils

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize

@Immutable
@JvmInline
value class PercentageOffset(private val packedValue: Long) {
    constructor(width: Float, height: Float) : this(packFloats(width, height))

    @Stable
    val x: Float
        get() = unpackFloat1(packedValue)

    @Stable
    val y: Float
        get() = unpackFloat2(packedValue)

    @Stable
    operator fun component1(): Float = x

    @Stable
    operator fun component2(): Float = y

    @Stable
    operator fun times(other: IntSize): IntSize =
        IntSize(width = (x * other.width).toInt(), height = (y * other.height).toInt())

    @Stable
    operator fun times(other: DpSize): DpSize =
        DpSize(width = (other.width * x), height = (other.height * y))

    @Stable
    override fun toString(): String = "(${x * 100}%, ${y * 100}%)"
}
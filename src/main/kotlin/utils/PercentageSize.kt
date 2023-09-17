package utils

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize

@Immutable
@JvmInline
value class PercentageSize(private val packedValue: Long) {
    constructor(width: Float, height: Float) : this(packFloats(width, height))

    @Stable
    val width: Float
        get() = unpackFloat1(packedValue)

    @Stable
    val height: Float
        get() = unpackFloat2(packedValue)

    @Stable
    operator fun component1(): Float = width

    @Stable
    operator fun component2(): Float = height

    @Stable
    operator fun times(other: IntSize): IntSize =
        IntSize(width = (width * other.width).toInt(), height = (height * other.height).toInt())

    @Stable
    operator fun times(other: DpSize): DpSize =
        DpSize(width = (other.width * width), height = (other.height * height))

    @Stable
    override fun toString(): String = "${width * 100}% x ${height * 100}%"
}
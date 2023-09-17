package utils

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize

//@Stable
//inline val IntOffset.size: IntSize get() = IntSize(x, y)
//
//@Stable
//inline val DpOffset.size: DpSize get() = DpSize(x, y)
//
//@Stable
//inline val IntSize.offset: IntOffset get() = IntOffset(width, height)

@Stable
inline val DpSize.offset: DpOffset get() = DpOffset(width, height)

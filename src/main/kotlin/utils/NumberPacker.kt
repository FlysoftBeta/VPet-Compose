package utils

fun packFloats(val1: Float, val2: Float): Long {
    val v1 = val1.toBits().toLong()
    val v2 = val2.toBits().toLong()
    return v1.shl(32) or (v2 and 0xFFFFFFFF)
}

fun unpackFloat1(value: Long): Float {
    return Float.fromBits(value.shr(32).toInt())
}

fun unpackFloat2(value: Long): Float {
    return Float.fromBits(value.and(0xFFFFFFFF).toInt())
}
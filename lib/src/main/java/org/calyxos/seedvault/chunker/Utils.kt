package org.calyxos.seedvault.chunker

import kotlin.math.log2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

internal object Utils {

    /**
     * Base-2 logarithm function for 32-bit integers.
     */
    fun log2(i: Int): Int {
        return log2(i.toDouble()).roundToInt()
    }

    /**
     * Returns two raised to the `bits` power, minus one.
     * In other words, a bit mask with that many least-significant bits set to 1.
     */
    fun mask(bits: Int): Int {
        check(bits in 1..31)
        return (2.toDouble().pow(bits) - 1).toInt()
    }

    /**
     * Returns the middle of the desired chunk size,
     * or what the FastCDC paper refers to as the "normal size".
     */
    fun centerSize(average: Int, minimum: Int, sourceSize: Int): Int {
        var offset = minimum + minimum.ceilDiv(2)
        if (offset > average) {
            offset = average
        }
        val size = average - offset
        return min(size, sourceSize)
    }

    /**
     * Integer division that rounds up instead of down (like [floorDiv] does).
     */
    fun Int.ceilDiv(y: Int): Int {
        return (this + y - 1) / y
    }
}

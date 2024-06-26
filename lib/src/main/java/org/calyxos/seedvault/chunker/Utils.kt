package org.calyxos.seedvault.chunker

import kotlin.math.min
import kotlin.math.pow

internal object Utils {

    fun mask(bits: Double): Int {
        check(bits in 1.0..31.0)
        return (2.toDouble().pow(bits) - 1).toInt()
    }

    fun centerSize(average: Int, minimum: Int, sourceSize: Int): Int {
        var offset = minimum + ceilDiv(minimum, 2)
        if (offset > average) {
            offset = average
        }
        val size = average - offset
        return min(size, sourceSize)
    }

    private fun ceilDiv(x: Int, y: Int): Int {
        return (x + y - 1) / y
    }
}

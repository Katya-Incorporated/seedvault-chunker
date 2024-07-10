package org.calyxos.seedvault.chunker

import org.calyxos.seedvault.chunker.Const.GEAR
import kotlin.test.Test
import kotlin.test.assertEquals

class ChunkerTest {

    @Test
    fun testAllZeros() {
        val chunker = Chunker(64, 256, 1024, 1, GEAR) {
            "" // don't care
        }
        val bytes = ByteArray(10240)
        val results = chunker.addBytes(bytes).toMutableList()
        results += chunker.finalize()
        assertEquals(10, results.size)
        results.forEach { chunk ->
            // maxSize chunks
            assertEquals(0, chunk.offset % 1024)
            assertEquals(1024, chunk.length)
        }
    }
}

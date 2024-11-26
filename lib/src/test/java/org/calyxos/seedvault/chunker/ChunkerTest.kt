package app.grapheneos.seedvault.chunker

import app.grapheneos.seedvault.chunker.Const.GEAR
import kotlin.random.Random
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

    @Test
    fun testMultipleFinalizeCalls() {
        val chunker = Chunker(64, 256, 1024, 1, GEAR) {
            "" // don't care
        }
        val bytes = Random.nextBytes(4069 * 8)
        chunker.addBytes(bytes).toList()
        chunker.finalize().toList()

        for (i in 0..10) {
            assertEquals(emptyList(), chunker.finalize().toList())
        }
    }

}

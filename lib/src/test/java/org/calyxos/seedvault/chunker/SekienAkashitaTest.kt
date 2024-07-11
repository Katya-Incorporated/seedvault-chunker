package org.calyxos.seedvault.chunker

import org.calyxos.seedvault.chunker.Const.GEAR
import org.calyxos.seedvault.chunker.Const.MINIMUM_MIN
import org.junit.Assert.assertArrayEquals
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class SekienAkashitaTest {

    @Test
    fun testSekien8kChunks() {
        val chunker = Chunker(4096, 8192, 16384, 1, GEAR) {
            "" // don't care
        }
        val file = getSekienAkashita()
        val results = chunker.chunk(file)

        assertEquals(11, results.size)

        assertEquals(0, results[0].offset)
        assertEquals(16384, results[0].length)
        assertEquals(16384, results[1].offset)
        assertEquals(5982, results[1].length)
        assertEquals(22366, results[2].offset)
        assertEquals(4716, results[2].length)
        assertEquals(27082, results[3].offset)
        assertEquals(5775, results[3].length)
        assertEquals(32857, results[4].offset)
        assertEquals(10731, results[4].length)
        assertEquals(43588, results[5].offset)
        assertEquals(7129, results[5].length)
        assertEquals(50717, results[6].offset)
        assertEquals(14930, results[6].length)
        assertEquals(65647, results[7].offset)
        assertEquals(16384, results[7].length)
        assertEquals(82031, results[8].offset)
        assertEquals(10105, results[8].length)
        assertEquals(92136, results[9].offset)
        assertEquals(11927, results[9].length)
        assertEquals(104063, results[10].offset)
        assertEquals(5403, results[10].length)
    }

    @Test
    fun testSekien16kChunks() {
        val chunker = Chunker(8192, 16384, 32768, 1, GEAR) {
            "" // don't care
        }
        val file = getSekienAkashita()
        val results = chunker.chunk(file)

        assertEquals(6, results.size)

        assertEquals(0, results[0].offset)
        assertEquals(22366, results[0].length)
        assertEquals(22366, results[1].offset)
        assertEquals(8282, results[1].length)
        assertEquals(30648, results[2].offset)
        assertEquals(16303, results[2].length)
        assertEquals(46951, results[3].offset)
        assertEquals(18696, results[3].length)
        assertEquals(65647, results[4].offset)
        assertEquals(32768, results[4].length)
        assertEquals(98415, results[5].offset)
        assertEquals(11051, results[5].length)
    }

    @Test
    fun testSekien32kChunks() {
        val chunker = Chunker(16384, 32768, 65536, 1, GEAR) {
            "" // don't care
        }
        val file = getSekienAkashita()
        val results = chunker.chunk(file)

        assertEquals(3, results.size)

        assertEquals(0, results[0].offset)
        assertEquals(32857, results[0].length)
        assertEquals(32857, results[1].offset)
        assertEquals(16408, results[1].length)
        assertEquals(49265, results[2].offset)
        assertEquals(60201, results[2].length)
    }

    @Test
    fun testSekien64kChunks() {
        val chunker = Chunker(32768, 65536, 131_072, 1, GEAR) {
            "" // don't care
        }
        val file = getSekienAkashita()
        val results = chunker.chunk(file)

        assertEquals(2, results.size)

        assertEquals(0, results[0].offset)
        assertEquals(32857, results[0].length)
        assertEquals(32857, results[1].offset)
        assertEquals(76609, results[1].length)
    }

    @Test
    fun testChunkerCanBeReUsed() {
        val chunker = Chunker(32768, 65536, 131_072, 1, GEAR) {
            "" // don't care
        }
        val file = getSekienAkashita()
        buildList {
            for (i in 0..10) add(chunker.chunk(file))
        }.forEach { results ->
            // always getting same chunks back
            assertEquals(2, results.size)

            assertEquals(0, results[0].offset)
            assertEquals(32857, results[0].length)
            assertEquals(32857, results[1].offset)
            assertEquals(76609, results[1].length)
        }
    }

    @Test
    fun testSekienEndSmallerThanMinimumMin() {
        val chunker = Chunker(10942, 21884, 43768, 1, GEAR) {
            "" // don't care
        }
        val file = getSekienAkashita()
        val results = chunker.chunk(file)

        assertEquals(5, results.size)

        assertEquals(0, results[0].offset)
        assertEquals(22366, results[0].length)
        assertEquals(22366, results[1].offset)
        assertEquals(24585, results[1].length)
        assertEquals(46951, results[2].offset)
        assertEquals(18696, results[2].length)
        assertEquals(65647, results[3].offset)
        assertEquals(43768, results[3].length)
        assertEquals(109415, results[4].offset)
        assertEquals(51, results[4].length)

        assertTrue(results[4].length < MINIMUM_MIN)
    }

    @Test
    fun testSameGearTableSameChunks() {
        val key = Random.nextBytes(256 / 8)
        val gearTable = GearTableCreator.create(key)
        val file = getSekienAkashita()
        val chunker = Chunker(16384, 32768, 65536, 1, gearTable) {
            "" // don't care
        }
        val results = chunker.chunk(file)
        assertTrue(results.size in 2..5, "Got ${results.size} chunks")

        // same gear table always produces same results
        for (i in 0..10) {
            val r = chunker.chunk(file)
            results.forEachIndexed { j, chunk ->
                assertEquals(chunk.offset, r[j].offset)
                assertEquals(chunk.length, r[j].length)
                assertArrayEquals(chunk.data, r[j].data)
            }
        }
    }

    private fun getSekienAkashita(): File {
        val classLoader = javaClass.classLoader
        val fileName = classLoader.getResource("SekienAkashita.jpg")?.file ?: fail()
        return File(fileName)
    }

}

fun Chunker.chunk(file: File): List<Chunk> = buildList {
    chunk(file) { add(it) }
}

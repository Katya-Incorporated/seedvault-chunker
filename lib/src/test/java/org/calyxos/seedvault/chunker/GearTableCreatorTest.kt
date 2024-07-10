package org.calyxos.seedvault.chunker

import org.calyxos.seedvault.chunker.GearTableCreator.GEAR_SIZE
import org.junit.Assert.assertArrayEquals
import kotlin.random.Random
import kotlin.test.Test

class GearTableCreatorTest {

    @Test
    fun testProperCreation() {
        for (i in 0..10) {
            val gearTable = GearTableCreator.create(Random.nextBytes(256 / 8))
            check(gearTable.size == GEAR_SIZE)
            gearTable.forEach {
                // no negative numbers allowed
                check(it ushr 31 == 0)
            }
        }
    }

    @Test
    fun testDeterministicCreation() {
        val key = Random.nextBytes(256 / 8)
        val gearTable = GearTableCreator.create(key)
        for (i in 0..10) {
            assertArrayEquals(gearTable, GearTableCreator.create(key))
        }
    }
}

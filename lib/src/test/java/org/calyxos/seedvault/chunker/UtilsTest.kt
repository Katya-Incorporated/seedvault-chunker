package org.calyxos.seedvault.chunker

import org.calyxos.seedvault.chunker.Const.AVERAGE_MAX
import org.calyxos.seedvault.chunker.Const.AVERAGE_MIN
import org.calyxos.seedvault.chunker.Const.MAXIMUM_MAX
import org.calyxos.seedvault.chunker.Const.MINIMUM_MIN
import org.calyxos.seedvault.chunker.Utils.ceilDiv
import org.calyxos.seedvault.chunker.Utils.centerSize
import org.calyxos.seedvault.chunker.Utils.log2
import org.calyxos.seedvault.chunker.Utils.mask
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class UtilsTest {

    @Test
    fun testlog2() {
        assertTrue(log2(MINIMUM_MIN) >= 6)
        assertTrue(log2(AVERAGE_MIN) >= 8)
        assertTrue(log2(AVERAGE_MAX) <= 28)
        assertTrue(log2(MAXIMUM_MAX) <= 30)
    }

    @Test
    fun testCeilDiv() {
        assertEquals(2, 10.ceilDiv(5))
        assertEquals(3, 11.ceilDiv(5))
        assertEquals(4, 10.ceilDiv(3))
        assertEquals(3, 9.ceilDiv(3))
        assertEquals(3, 6.ceilDiv(2))
        assertEquals(3, 5.ceilDiv(2))
    }

    @Test
    fun testCenterSize() {
        assertEquals(0, centerSize(50, 100, 50))
        assertEquals(50, centerSize(200, 100, 50))
        assertEquals(40, centerSize(200, 100, 40))
    }

    @Test
    fun testMask() {
        assertFails { mask(0) }
        assertFails { mask(32) }
        assertEquals(16_777_215, mask(24))
        assertEquals(65535, mask(16))
        assertEquals(1023, mask(10))
        assertEquals(255, mask(8))
    }

}

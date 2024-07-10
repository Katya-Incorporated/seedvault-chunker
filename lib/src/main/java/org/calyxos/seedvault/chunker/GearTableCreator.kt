package org.calyxos.seedvault.chunker

import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object GearTableCreator {

    const val GEAR_SIZE = 256
    private const val KEY_SIZE = 256 / 8
    private const val IV_SIZE = 16

    /**
     * Returns a gear table of size [GEAR_SIZE] filled with "random" numbers derived from the given
     * [secret].
     * The table is created by ciphering a 1024-byte array of all zeros using a [KEY_SIZE]-byte key
     * and 16-byte nonce (a.k.a. initialization vector) of all zeroes.
     * The high bit of each value is cleared,
     * because 31-bit integers are immune from signed 32-bit integer overflow,
     * which the implementation relies on for hashing.
     *
     * @param secret [KEY_SIZE] random bytes.
     */
    fun create(secret: ByteArray): IntArray {
        check(secret.size == KEY_SIZE)

        val key = SecretKeySpec(secret, 0, KEY_SIZE, "AES")
        val cipher = Cipher.getInstance("AES/CTR/NoPadding").apply {
            val params = IvParameterSpec(ByteArray(IV_SIZE))
            init(ENCRYPT_MODE, key, params)
        }
        val zeros = ByteArray(GEAR_SIZE * 4)
        val bytes = cipher.doFinal(zeros)

        val result = IntArray(GEAR_SIZE)
        val mask = Utils.mask(31)
        for (i in 0 until GEAR_SIZE * 4 step 4) {
            result[i / 4] = ByteBuffer.wrap(bytes.copyOfRange(i, i + 4)).getInt() and mask
        }
        return result
    }

}

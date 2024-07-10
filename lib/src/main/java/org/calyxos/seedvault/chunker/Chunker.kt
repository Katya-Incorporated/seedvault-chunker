package org.calyxos.seedvault.chunker

import org.calyxos.seedvault.chunker.Const.AVERAGE_MAX
import org.calyxos.seedvault.chunker.Const.AVERAGE_MIN
import org.calyxos.seedvault.chunker.Const.MAXIMUM_MAX
import org.calyxos.seedvault.chunker.Const.MAXIMUM_MIN
import org.calyxos.seedvault.chunker.Const.MINIMUM_MAX
import org.calyxos.seedvault.chunker.Const.MINIMUM_MIN
import java.io.File
import java.lang.Byte.toUnsignedInt
import kotlin.math.min

class Chunk(val offset: Long, val length: Int, val data: ByteArray, val hash: String)

class Chunker(
    private val minSize: Int,
    avgSize: Int,
    private val maxSize: Int,
    private val normalization: Int,
    private val hashFunction: (ByteArray) -> String,
) {
    private val centerSize: Int = Utils.centerSize(avgSize, minSize, maxSize)
    private val maskS: Int
    private val maskL: Int
    private val blob = RingByteArray(maxSize * 2)
    private var offset: Long = 0

    constructor(avgSize: Int, normalization: Int = 2, hashFunction: (ByteArray) -> String) : this(
        minSize = avgSize.floorDiv(4),
        avgSize = avgSize,
        maxSize = avgSize * 8,
        normalization = normalization,
        hashFunction = hashFunction,
    )

    init {
        check(minSize in MINIMUM_MIN..MINIMUM_MAX)
        check(avgSize in AVERAGE_MIN..AVERAGE_MAX)
        check(maxSize in MAXIMUM_MIN..MAXIMUM_MAX)
        check(minSize <= avgSize)
        check(maxSize >= avgSize)
        check(normalization in 0..3)

        val bits = Utils.log2(avgSize)
        maskS = Utils.mask(bits + normalization)
        maskL = Utils.mask(bits - normalization)
    }

    fun addBytes(bytes: ByteArray): Sequence<Chunk> = sequence {
        if (bytes.size <= maxSize) {
            blob.addAll(bytes)
            if (blob.size <= maxSize) {
                // our blob is still too small for trying to get chunks
                return@sequence
            } else {
                // a blob's capacity is double maxSize and can contain several chunks
                while (blob.size > maxSize) {
                    yield(getNextChunk())
                }
            }
        } else {
            // our input is bigger than maxSize, so we need to break it down
            var offset = 0
            while (offset < bytes.size) {
                val length = min(maxSize, bytes.size - offset)
                val subBytes = bytes.copyOfRange(offset, offset + length)
                yieldAll(addBytes(subBytes))
                offset += length
            }
        }
    }

    fun finalize(): Sequence<Chunk> = sequence {
        while (!blob.isEmpty) {
            yield(getNextChunk())
        }
    }

    private fun getNextChunk(): Chunk {
        val chunkLength = getCdcOffset()
        val data = blob.getRange(0, chunkLength)
        val hash = hashFunction(data)
        val chunk = Chunk(offset, chunkLength, data, hash)
        offset += chunkLength.toLong()
        blob.resetPosition(chunkLength)
        return chunk
    }

    private fun getCdcOffset(): Int {
        var pattern = 0
        val size = blob.size
        var index = min(minSize, size)
        var barrier = min(centerSize, size)
        while (index < barrier) {
            pattern = (pattern ushr 1) + Const.GEAR[toUnsignedInt(blob[index])]
            if ((pattern and maskS) == 0) {
                return index + 1
            }
            index++
        }
        barrier = min(maxSize, size)
        while (index < barrier) {
            pattern = (pattern ushr 1) + Const.GEAR[toUnsignedInt(blob[index])]
            if ((pattern and maskL) == 0) {
                return index + 1
            }
            index++
        }
        return index
    }
}

fun Chunker.chunk(file: File, onNewChunk: (Chunk) -> Unit) {
    file.inputStream().use { inputStream ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytes = inputStream.read(buffer)
        while (bytes >= 0) {
            addBytes(buffer.copyOfRange(0, bytes)).forEach { chunk ->
                onNewChunk(chunk)
            }
            bytes = inputStream.read(buffer)
        }
        // get final chunks
        finalize().forEach { chunk -> onNewChunk(chunk) }
    }
}

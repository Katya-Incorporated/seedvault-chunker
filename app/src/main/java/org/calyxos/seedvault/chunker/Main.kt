package org.calyxos.seedvault.chunker

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import com.github.luben.zstd.ZstdOutputStream
import org.calyxos.seedvault.chunker.Const.AVERAGE_MAX
import org.calyxos.seedvault.chunker.Const.AVERAGE_MIN
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import kotlin.math.roundToInt
import kotlin.time.measureTime


class Cli : CliktCommand() {
    private val files by argument().multiple(required = true)

    private val checkDedupRatio: Boolean by option().flag(default = false)
    private val verbose: Boolean by option("-v", "--verbose").flag(default = false)
    private val writeCsv: Boolean by option().flag(default = false)

    private val size: Int by option()
        .int()
        .restrictTo(AVERAGE_MIN..AVERAGE_MAX)
        .default(16 * 1024)

    private val normalization: Int by option()
        .int()
        .restrictTo(0..3)
        .default(1)

    override fun run() {
        val digest = MessageDigest.getInstance("SHA-256")
        val chunker = Chunker(size, normalization) { bytes ->
            digest.digest(bytes).fold("") { str, it -> str + "%02x".format(it) }
        }
        val duration = measureTime {
            files.forEach { file ->
                onEachFile(chunker, File(file))
            }
        }
        println("\nTook: $duration")
        if (checkDedupRatio) {
            println()
            val sizePerTime = totalSize / duration.inWholeSeconds / 1024 / 1024
            val sizeCompressedDupe = totalSize - compressedSize
            val dedup = (sizeCompressedDupe.toDouble() / totalSize.toDouble() * 100).roundToInt()
            println("Files: ${files.size} with a total of $totalSize bytes ($sizePerTime MiB/s)")
            println("Unique chunks: ${chunks.size}")
            println("Dupe chunks: $reusedChunks")
            println("Dupe data: $sizeCompressedDupe")
            println("Dedup Ratio: ${dedup}%")
            println("Single file chunks: $singleFileChunks Deduped: $singleFileChunksDedup")

            println()
            println("Total size: ${totalSize / 1024 / 1024}MB")
            println("chunk ┃ unique ┃ compressed ┃ size  ┃ chunks ┃ chunks ┃ chunks ")
            println("size  ┃ chunks ┃    size    ┃ saved ┃ < 10KB ┃ <500KB ┃ <800KB ")
            println("━━━━━━╋━━━━━━━━╋━━━━━━━━━━━━╋━━━━━━━╋━━━━━━━━╋━━━━━━━━╋━━━━━━━ ")
            print("${(size.toDouble() / 1024 / 1024).toString().padStart(3)}MB ┃ ")
            print("${chunks.size.toString().padStart(6)} ┃ ")
            print("${(compressedSize / 1024 / 1024).toString().padStart(8)}MB ┃ ")
            print("${dedup.toString().padStart(4)}% ┃ ")
            print("${numLessThan10.toString().padStart(6)} ┃ ")
            print("${numLessThan500.toString().padStart(6)} ┃ ")
            print("${numLessThan800.toString().padStart(6)} ")
            println()
            print("   uncompressed: ")
            println("${(uniqueChunkSize / 1024 / 1024).toString().padStart(8)}MB")
        }
    }

    private val chunks = mutableSetOf<String>()
    private var reusedChunks: Int = 0
    private var sizeDupe: Long = 0L
    private var uniqueChunkSize: Long = 0L
    private var compressedSize: Long = 0L

    private var numLessThan10 = 0
    private var numLessThan500 = 0
    private var numLessThan800 = 0
    private var singleFileChunks = 0
    private var singleFileChunksDedup = 0

    private var totalSize: Long = 0

    private fun onEachFile(chunker: Chunker, file: File) {
        if (verbose) {
            println()
            println(file.absolutePath)
        }
        if (!file.isFile) {
            if (verbose) println("  not a file, ignoring...")
            return
        }
        totalSize += file.length()
        chunker.chunk(file) { chunk -> onNewChunk(chunk, file.length()) }
    }

    private val outputStream = ByteArrayOutputStream()

    private fun onNewChunk(chunk: Chunk, length: Long) {
        if (verbose) println("hash=${chunk.hash} offset=${chunk.offset} size=${chunk.length}")
        if (length == chunk.length.toLong()) singleFileChunks++
        if (checkDedupRatio) {
            if (chunk.hash in chunks) {
                sizeDupe += chunk.length
                reusedChunks++
                if (length == chunk.length.toLong()) singleFileChunksDedup++
            } else {
                chunks.add(chunk.hash)
                outputStream.reset()
                ZstdOutputStream(outputStream).use {
                    it.write(chunk.data)
                }
                compressedSize += outputStream.size()
                uniqueChunkSize += chunk.data.size

                if (outputStream.size() < 10 * 1024) numLessThan10++
                if (outputStream.size() < 500 * 1024) numLessThan500++
                if (outputStream.size() < 800 * 1024) numLessThan800++

                if (writeCsv) {
                    addToCsv("${chunk.length},${outputStream.size()}")
                }
            }
        } else if (writeCsv) {
            addToCsv(chunk.length.toString())
        }
    }

    private fun addToCsv(s: String) {
        FileOutputStream(File("chunks.csv"), true).use {
            it.write("$s\n".toByteArray())
        }
    }

}

fun main(args: Array<String>) = Cli().main(args)

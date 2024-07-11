package org.calyxos.seedvault.chunker

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import org.calyxos.seedvault.chunker.Const.AVERAGE_MAX
import org.calyxos.seedvault.chunker.Const.AVERAGE_MIN
import java.io.File
import java.security.MessageDigest
import kotlin.math.roundToInt
import kotlin.time.measureTime

class Cli : CliktCommand() {
    private val files by argument().multiple(required = true)

    private val checkDedupRatio: Boolean by option().flag(default = false)
    private val verbose: Boolean by option("-v", "--verbose").flag(default = false)

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
            val totalSize = files.sumOf { File(it).length() }
            val sizePerTime = totalSize / duration.inWholeSeconds / 1024 / 1024
            println("Files: ${files.size} with a total of $totalSize bytes ($sizePerTime MiB/s)")
            println("Unique chunks: ${chunks.size}")
            println("Dupe chunks: $reusedChunks")
            println("Dupe data: $sizeDupe")
            println("Dedup Ratio: ${(sizeDupe.toDouble() / totalSize.toDouble() * 100).roundToInt()}%")
        }
    }

    private val chunks = mutableSetOf<String>()
    private var reusedChunks: Int = 0
    private var sizeDupe: Long = 0L

    private fun onEachFile(chunker: Chunker, file: File) {
        if (verbose) {
            println()
            println(file.absolutePath)
        }
        if (!file.isFile) {
            if (verbose) println("  not a file, ignoring...")
            return
        }
        chunker.chunk(file) { chunk -> onNewChunk(chunk) }
    }

    private fun onNewChunk(chunk: Chunk) {
        if (verbose) println("hash=${chunk.hash} offset=${chunk.offset} size=${chunk.length}")
        if (checkDedupRatio) {
            if (chunk.hash in chunks) {
                sizeDupe += chunk.length
                reusedChunks++
            } else {
                chunks.add(chunk.hash)
            }
        }
    }
}

fun main(args: Array<String>) = Cli().main(args)

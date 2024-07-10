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

class Cli : CliktCommand() {
    private val files by argument().multiple(required = true)

    private val checkDedupRatio: Boolean by option().flag(default = false)

    private val size: Int by option()
        .int()
        .restrictTo(AVERAGE_MIN..AVERAGE_MAX)
        .default(16384)

    private val normalization: Int by option()
        .int()
        .restrictTo(0..3)
        .default(1)

    override fun run() {
        files.forEach { file ->
            onEachFile(File(file))
        }
        if (checkDedupRatio) {
            println()
            val totalSize = files.sumOf { File(it).length() }
            println("Files: ${files.size} with a total of $totalSize bytes")
            println("Unique chunks: ${chunks.size}")
            println("Dupe chunks: $reusedChunks")
            println("Dupe data: $sizeDupe")
            println("Dedup Ratio: ${(sizeDupe.toDouble() / totalSize.toDouble() * 100).roundToInt()}%")
        }
    }

    private val chunks = mutableSetOf<String>()
    private var reusedChunks: Int = 0
    private var sizeDupe: Long = 0L

    private fun onEachFile(file: File) {
        println()
        println(file.absolutePath)
        if (!file.isFile) {
            println("  not a file, ignoring...")
            return
        }
        val digest = MessageDigest.getInstance("SHA-256")
        val chunker = Chunker(size, normalization) { bytes ->
            digest.digest(bytes).fold("") { str, it -> str + "%02x".format(it) }
        }
        chunker.chunk(file) { chunk -> onNewChunk(chunk) }
    }

    private fun onNewChunk(chunk: Chunk) {
        println("hash=${chunk.hash} offset=${chunk.offset} size=${chunk.length}")
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

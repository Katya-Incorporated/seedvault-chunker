package org.calyxos.seedvault.chunker

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import org.calyxos.seedvault.chunker.Const.AVERAGE_MAX
import org.calyxos.seedvault.chunker.Const.AVERAGE_MIN
import java.io.File
import java.security.MessageDigest

class Cli : CliktCommand() {
    private val files by argument().multiple(required = true)

    private val size: Int by option()
        .int()
        .restrictTo(AVERAGE_MIN..AVERAGE_MAX)
        .default(16384)

    private val normalization: Int by option()
        .int()
        .restrictTo(0..3)
        .default(1)

    override fun run() {
        val digest = MessageDigest.getInstance("SHA-256")
        val chunker = Chunker(size, normalization) { bytes ->
            digest.digest(bytes).fold("") { str, it -> str + "%02x".format(it) }
        }
        files.forEach { file ->
            onEachFile(chunker, File(file))
        }
    }

    private fun onEachFile(chunker: Chunker, file: File) {
        file.inputStream().use { inputStream ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytes = inputStream.read(buffer)
            while (bytes >= 0) {
                chunker.addBytes(buffer.copyOfRange(0, bytes)).forEach { chunk ->
                    onNewChunk(chunk)
                }
                bytes = inputStream.read(buffer)
            }
            // get final chunks
            chunker.finalize().forEach { chunk -> onNewChunk(chunk) }
        }
    }

    private fun onNewChunk(chunk: Chunk) {
        println("hash=${chunk.hash} offset=${chunk.offset} size=${chunk.length}")
    }
}

fun main(args: Array<String>) = Cli().main(args)

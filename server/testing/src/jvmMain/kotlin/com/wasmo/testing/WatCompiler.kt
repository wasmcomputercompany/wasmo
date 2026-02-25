package com.wasmo.testing

import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import okio.FileNotFoundException
import okio.FileSystem

/**
 * Shell out to `wat2wasm`.
 */
class WatCompiler() {
  private val fileSystem = FileSystem.SYSTEM

  fun compile(wat: String) : ByteString {
    val data = wat.encodeUtf8()
    val dataHash = data.sha256().base64Url()
    val watCompilerDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "WatCompiler"
    val inputWat = watCompilerDir / "${dataHash}.wat"
    val outputWasm = watCompilerDir / "${dataHash}.wasm"

    try {
      return fileSystem.read(outputWasm) {
        readByteString()
      }
    } catch (_: FileNotFoundException) {
      // Cache miss. Actually build the file.
    }

    fileSystem.createDirectories(watCompilerDir)
    fileSystem.write(inputWat) {
      write(data)
    }

    val process = ProcessBuilder()
      .directory(watCompilerDir.toFile())
      .command("wat2wasm", inputWat.name, "-o", outputWasm.name)
      .start()

    check(process.waitFor() == 0) {
      "unexpected exit code for wat2wasm: ${process.exitValue()}"
    }

    return fileSystem.read(outputWasm) {
      readByteString()
    }
  }
}

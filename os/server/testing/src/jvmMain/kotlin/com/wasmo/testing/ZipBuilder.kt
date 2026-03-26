package com.wasmo.testing

import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.WasmoToml
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.serialization.encodeToString
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import okio.buffer
import okio.sink

fun buildZip(block: ZipBuilder.() -> Unit): ByteString {
  val builder = ZipBuilder()
  builder.block()
  return builder.build()
}

/**
 * Builds a ZIP archive for testing.
 */
class ZipBuilder {
  private val entries = mutableMapOf<String, ByteString>()

  fun put(appManifest: AppManifest) {
    put(
      "wasmo-manifest.toml",
      WasmoToml.encodeToString<AppManifest>(appManifest).encodeUtf8(),
    )
  }

  fun put(name: String, value: ByteString) {
    entries[name] = value
  }

  fun build(): ByteString {
    val buffer = Buffer()

    ZipOutputStream(buffer.outputStream()).use { zipOutputStream ->
      for ((name, value) in entries) {
        zipOutputStream.putNextEntry(ZipEntry(name))
        val zipSink = zipOutputStream.sink().buffer()
        zipSink.write(value)
        zipSink.emit()
      }
    }

    return buffer.readByteString()
  }
}

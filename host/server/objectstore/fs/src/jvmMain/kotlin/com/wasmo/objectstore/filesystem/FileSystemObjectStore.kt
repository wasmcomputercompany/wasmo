package com.wasmo.objectstore.filesystem

import com.wasmo.objectstore.DeleteObjectRequest
import com.wasmo.objectstore.DeleteObjectResponse
import com.wasmo.objectstore.GetObjectRequest
import com.wasmo.objectstore.GetObjectResponse
import com.wasmo.objectstore.ListObjectsRequest
import com.wasmo.objectstore.ListObjectsResponse
import com.wasmo.objectstore.ObjectStore
import com.wasmo.objectstore.PutObjectRequest
import com.wasmo.objectstore.PutObjectResponse
import com.wasmo.objectstore.etag
import okio.Buffer
import okio.FileNotFoundException
import okio.FileSystem
import okio.HashingSink
import okio.IOException
import okio.Path
import okio.Sink
import okio.Timeout

class FileSystemObjectStore(
  private val fileSystem: FileSystem,
  private val path: Path,
) : ObjectStore {
  override suspend fun put(request: PutObjectRequest): PutObjectResponse {
    val path = request.key.toPath()
    fileSystem.createDirectories(path.parent!!)
    return fileSystem.write(path) {
      write(request.value)
      PutObjectResponse(request.value.etag)
    }
  }

  override suspend fun get(request: GetObjectRequest): GetObjectResponse {
    val value = try {
      fileSystem.read(request.key.toPath()) {
        readByteString()
      }
    } catch (_: FileNotFoundException) {
      null
    }
    return GetObjectResponse(
      value = value,
    )
  }

  override suspend fun delete(request: DeleteObjectRequest): DeleteObjectResponse {
    fileSystem.delete(request.key.toPath())
    return DeleteObjectResponse
  }

  override suspend fun list(request: ListObjectsRequest): ListObjectsResponse {
    val root = request.prefix?.toPath() ?: path
    val entries = fileSystem.listRecursively(root).mapNotNull { path ->
      try {
        fileSystem.read(path) {
          val countingSink = CountingSink()
          val etag = HashingSink.md5(countingSink).use { etagSink ->
            readAll(etagSink)
            etagSink.hash
          }
          ListObjectsResponse.Object(
            key = path.toKey(),
            etag = etag.hex(),
            size = countingSink.count,
          )
        }
      } catch (_: IOException) {
        null
      }
    }
    return ListObjectsResponse(
      entries = entries.toList(),
    )
  }

  private fun String.toPath(): Path {
    val result = path.resolve(this)
    require(path.isAncestorOf(result)) { "unexpected key: $this" }
    return result
  }

  private fun Path.toKey(): String {
    return relativeTo(path).toString()
  }

  private class CountingSink : Sink {
    var count: Long = 0L
      private set

    override fun close() {
    }

    override fun flush() {
    }

    override fun timeout() = Timeout.NONE

    override fun write(source: Buffer, byteCount: Long) {
      count += byteCount
      source.skip(byteCount)
    }
  }

  companion object {
    fun Path.isAncestorOf(other: Path): Boolean {
      val segments = this.segmentsBytes
      val otherSegments = other.segmentsBytes
      return otherSegments.size > segments.size &&
        otherSegments.subList(0, segments.size) == segments
    }
  }
}

package com.wasmo.objectstore.filesystem

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.UserDefinedFileAttributeView
import okio.Buffer
import okio.FileNotFoundException
import okio.FileSystem
import okio.HashingSink
import okio.IOException
import okio.Path
import okio.Sink
import okio.Timeout
import wasmo.objectstore.GetObjectResponse
import wasmo.objectstore.ListObjectsResponse
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.etag
import wasmo.objectstore.validateKey
import wit.wasmo.object_store.Types.DeleteObjectRequest
import wit.wasmo.object_store.Types.Entry
import wit.wasmo.object_store.Types.EntryObject
import wit.wasmo.object_store.Types.GetObjectRequest
import wit.wasmo.object_store.Types.GetObjectResponse
import wit.wasmo.object_store.Types.Key
import wit.wasmo.object_store.Types.ListObjectsRequest
import wit.wasmo.object_store.Types.ListObjectsResponse
import wit.wasmo.object_store.Types.PutObjectRequest
import wit.wasmo.object_store.Types.PutObjectResponse

/**
 * This attempts to store file metadata in extended attributes.
 *
 * The file system doesn't guarantee to support this, and if it doesn't, that metadata will be lost.
 * It's likely we'll need to later support an alternative place for metadata, such as sidecar files.
 *
 * https://man7.org/linux/man-pages/man7/xattr.7.html
 */
class FileSystemObjectStore(
  private val path: Path,
) : ObjectStore {
  private val fileSystem = FileSystem.SYSTEM

  override suspend fun put(request: PutObjectRequest): PutObjectResponse {
    val path = request.key.toPath()
    fileSystem.createDirectories(path.parent!!)
    fileSystem.write(path) {
      write(request.value)
    }

    path.userAttributes?.writeAttributeUtf8("user.mimetype", request.contentType?.value)

    return PutObjectResponse(request.value.etag)
  }

  override suspend fun get(request: GetObjectRequest): GetObjectResponse {
    val path = request.key.toPath()
    try {
      val value = fileSystem.read(path) {
        readByteString()
      }
      return GetObjectResponse(
        value = value,
        contentType = path.userAttributes?.readAttributeUtf8("user.mimetype"),
      )
    } catch (_: FileNotFoundException) {
      return GetObjectResponse(
        value = null,
      )
    }
  }

  override suspend fun delete(request: DeleteObjectRequest) {
    fileSystem.delete(request.key.toPath())
  }

  override suspend fun list(request: ListObjectsRequest): ListObjectsResponse {
    val root = request.prefix?.let { Key(it) }?.toPath() ?: path
    val entries = fileSystem.listRecursively(root).mapNotNull { path ->
      try {
        fileSystem.read(path) {
          val countingSink = CountingSink()
          val etag = HashingSink.md5(countingSink).use { etagSink ->
            readAll(etagSink)
            etagSink.hash
          }
          Entry.Object(
            EntryObject(
              key = path.toKey(),
              etag = etag.hex(),
              size = countingSink.count.toULong(),
            )
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

  private fun Key.toPath(): Path {
    validateKey()
    val result = path.resolve(this.value)
    require(path.isAncestorOf(result)) { "unexpected key: $this" }
    return result
  }

  private fun Path.toKey(): Key {
    return Key(relativeTo(path).toString())
      .apply { validateKey() }
  }

  private val Path.userAttributes: UserDefinedFileAttributeView?
    get() = Files.getFileAttributeView(
      toNioPath(),
      UserDefinedFileAttributeView::class.java,
      LinkOption.NOFOLLOW_LINKS,
    )

  private fun UserDefinedFileAttributeView.readAttributeUtf8(name: String): String? {
    try {
      val byteArray = ByteArray(1024)
      val byteCount = read(name, ByteBuffer.wrap(byteArray))
      return when {
        byteCount != 0 -> String(byteArray, 0, byteCount, StandardCharsets.UTF_8)
        else -> null
      }
    } catch (_: FileSystemException) {
      return null
    }
  }

  private fun UserDefinedFileAttributeView.writeAttributeUtf8(name: String, value: String?) {
    try {
      when {
        value.isNullOrEmpty() -> delete(name)
        else -> write(name, ByteBuffer.wrap(value.toByteArray(StandardCharsets.UTF_8)))
      }
    } catch (_: FileSystemException) {
      // Best-effort only.
    }
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

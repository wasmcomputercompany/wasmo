package com.wasmo

import okio.ByteString
import okio.utf8Size

/**
 * An S3-like object store.
 */
interface ObjectStore {
  /**
   * https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html
   */
  suspend fun put(request: PutObjectRequest): PutObjectResponse

  /**
   * https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html
   */
  suspend fun get(request: GetObjectRequest): GetObjectResponse

  /**
   * https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteObject.html
   */
  suspend fun delete(request: DeleteObjectRequest): DeleteObjectResponse

  /**
   * https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListObjectsV2.html
   */
  suspend fun list(request: ListObjectsRequest): ListObjectsResponse
}

data class PutObjectRequest(
  val key: String,
  val value: ByteString,
) {
  init {
    key.validateKey()
  }
}

data class PutObjectResponse(
  val etag: String,
)

data class GetObjectRequest(
  val key: String,
) {
  init {
    key.validateKey()
  }
}

data class GetObjectResponse(
  val value: ByteString?,
  val etag: String? = value?.etag,
)

data class DeleteObjectRequest(
  val key: String,
) {
  init {
    key.validateKey()
  }
}

object DeleteObjectResponse

/**
 * @param prefix null to list all items, or a string suffixed with "/" to list only items with that
 *   prefix. Use this with [delimiter] to list objects like a hierarchical file system.
 * @param delimiter "/" to flatten results within a common prefix, or null to return all results
 *   with a common directory prefix.
 */
data class ListObjectsRequest(
  val prefix: String? = null,
  val delimiter: String? = null,
  val continuationToken: String? = null,
)

data class ListObjectsResponse(
  val entries: List<Entry>,
  val nextRequest: ListObjectsRequest? = null,
) {
  sealed class Entry

  data class Object(
    val key: String,
    val etag: String,
    val size: Long,
  ) : Entry()

  data class CommonPrefix(
    val prefix: String,
  ) : Entry()
}

/** An object that prefixes all entries with [prefix]. */
class ScopedObjectStore(
  private val delegate: ObjectStore,
  private val prefix: String,
) : ObjectStore {
  init {
    check(prefix.endsWith("/")) { "prefix must end with '/' but was '$prefix'" }
  }

  override suspend fun put(request: PutObjectRequest) =
    delegate.put(request.copy(key = prefix + request.key))

  override suspend fun get(request: GetObjectRequest) =
    delegate.get(request.copy(key = prefix + request.key))

  override suspend fun delete(request: DeleteObjectRequest) =
    delegate.delete(request.copy(key = prefix + request.key))

  override suspend fun list(request: ListObjectsRequest): ListObjectsResponse {
    val result = delegate.list(request.copy(prefix = prefix + (request.prefix ?: "")))
    return result.copy(
      entries = result.entries.map { entry ->
        when (entry) {
          is ListObjectsResponse.CommonPrefix -> entry.copy(
            prefix = entry.prefix.removePrefix(prefix),
          )

          is ListObjectsResponse.Object -> entry.copy(
            key = entry.key.removePrefix(prefix),
          )
        }
      },
      nextRequest = result.nextRequest?.copy(
        prefix = result.nextRequest.prefix?.removePrefix(prefix),
      ),
    )
  }
}

val ByteString.etag: String
  get() = md5().hex()

/**
 * https://www.backblaze.com/docs/cloud-storage-files#file-names
 */
fun String.validateKey() {
  val utf8Size = utf8Size()
  require(utf8Size in 1..1024) {
    "key length must be in 1..1024 but was $utf8Size: $this"
  }

  var pos = 0
  while (pos < length) {
    val codePoint = codePointAt(pos)
    require(codePoint >= ' '.code && codePoint != '\u007f'.code) {
      "key has invalid code point at $pos: 0x${codePoint.toString(radix = 16)}"
    }
    pos += Character.charCount(codePoint)
  }
}

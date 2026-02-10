package com.wasmo

import okio.ByteString

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
)

data class PutObjectResponse(
  val etag: String,
)

data class GetObjectRequest(
  val key: String,
)

data class GetObjectResponse(
  val value: ByteString?,
  val etag: String? = value?.etag,
)

data class DeleteObjectRequest(
  val key: String,
)

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
  private val prefix: String,
  private val delegate: ObjectStore,
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

package wasmo.objectstore

import okio.Buffer
import okio.ByteString
import wit.wasmo.content_type.Types.ContentType
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
   * Replaces the value with a delete marker. It will continue to be returned by [list].
   *
   * https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteObject.html
   */
  suspend fun delete(request: DeleteObjectRequest)

  /**
   * https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListObjectsV2.html
   */
  suspend fun list(request: ListObjectsRequest): ListObjectsResponse
}

fun PutObjectRequest(
  key: String,
  value: ByteString,
  contentType: String? = null,
) = PutObjectRequest(
  key = Key(key),
  value = value,
  contentType = contentType?.toContentType(),
)

fun GetObjectRequest(
  key: String,
) = GetObjectRequest(Key(key))

fun DeleteObjectRequest(
  key: String,
) = DeleteObjectRequest(Key(key))

fun GetObjectResponse(
  value: ByteString?,
  etag: String? = value?.etag,
  contentType: String? = null,
) = GetObjectResponse(
  value = value,
  etag = etag,
  contentType = contentType?.toContentType(),
)

fun ListObjectsRequest(
  prefix: String? = null,
  delimiter: String? = null,
  continuationToken: String? = null,
  unused: Unit = Unit,
) = ListObjectsRequest(
  prefix = prefix,
  delimiter = delimiter,
  continuationToken = continuationToken,
)

fun ListObjectsResponse(
  entries: List<Entry>,
  nextRequest: ListObjectsRequest? = null,
  unused: Unit = Unit,
) = ListObjectsResponse(
  entries = entries,
  nextRequest = nextRequest,
)

fun Entry(
  key: String,
  etag: String,
  size: ULong,
) = Entry.Object(
  EntryObject(
    key = Key(key),
    etag = etag,
    size = size,
  ),
)

fun String.toContentType() = ContentType(this)

/** An object that prefixes all entries with [prefix]. */
class ScopedObjectStore(
  private val delegate: ObjectStore,
  private val prefix: String,
) : ObjectStore {
  init {
    check(prefix.endsWith("/")) { "prefix must end with '/' but was '$prefix'" }
  }

  override suspend fun put(request: PutObjectRequest) =
    delegate.put(request.copy(key = Key(prefix + request.key.value)))

  override suspend fun get(request: GetObjectRequest) =
    delegate.get(request.copy(key = Key(prefix + request.key.value)))

  override suspend fun delete(request: DeleteObjectRequest) =
    delegate.delete(request.copy(key = Key(prefix + request.key.value)))

  override suspend fun list(request: ListObjectsRequest): ListObjectsResponse {
    val result = delegate.list(request.copy(prefix = prefix + (request.prefix ?: "")))
    return result.copy(
      entries = result.entries.map { entry ->
        when (entry) {
          is Entry.CommonPrefix -> entry.copy(
            value = entry.value.removePrefix(prefix),
          )

          is Entry.Object -> entry.copy(
            value = entry.value.copy(
              key = Key(entry.value.key.value.removePrefix(prefix)),
            ),
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
fun Key.validateKey() {
  val buffer = Buffer()
    .writeUtf8(value)

  val utf8Size = buffer.size
  require(utf8Size in 1..1024) {
    "key length must be in 1..1024 but was $utf8Size: $value"
  }

  while (!buffer.exhausted()) {
    val pos = utf8Size - buffer.size
    val codePoint = buffer.readUtf8CodePoint()
    require(codePoint >= ' '.code && codePoint != '\u007f'.code) {
      "key has invalid code point at $pos: 0x${codePoint.toString(radix = 16)}"
    }
  }
}

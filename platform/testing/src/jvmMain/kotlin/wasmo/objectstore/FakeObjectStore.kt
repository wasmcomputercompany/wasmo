package wasmo.objectstore

import java.util.TreeMap
import okio.ByteString

class FakeObjectStore : ObjectStore {
  var nextException: Exception? = null
  private val objects = TreeMap<String, Object>()

  operator fun get(key: String): ByteString? =
    objects[key]?.value

  override suspend fun put(request: PutObjectRequest): PutObjectResponse {
    throwIfNecessary()
    val o = objects.getOrPut(request.key) {
      Object(
        key = request.key,
      )
    }
    o.value = request.value
    o.contentType = request.contentType
    return PutObjectResponse(
      etag = o.value.etag,
    )
  }

  override suspend fun get(request: GetObjectRequest): GetObjectResponse {
    throwIfNecessary()
    val o = objects[request.key]
    return GetObjectResponse(
      value = o?.value,
      contentType = o?.contentType,
    )
  }

  override suspend fun delete(request: DeleteObjectRequest): DeleteObjectResponse {
    throwIfNecessary()
    objects.remove(request.key)
    return DeleteObjectResponse
  }

  fun list(prefix: String): List<ByteString> =
    listObjects(prefix).map { it.value }

  // TODO: honor delimiter.
  // TODO: limit the result count.
  override suspend fun list(request: ListObjectsRequest): ListObjectsResponse {
    throwIfNecessary()
    val objects = listObjects(request.prefix)
    return ListObjectsResponse(
      entries = objects.map {
        ListObjectsResponse.Object(
          key = it.key,
          etag = it.value.etag,
          size = it.value.size.toLong(),
        )
      },
      nextRequest = null,
    )
  }

  private fun listObjects(prefix: String?): List<Object> {
    val map = when {
      prefix != null -> objects.tailMap(prefix)
      else -> objects
    }

    val list = mutableListOf<Object>()

    for ((key, value) in map) {
      if (prefix != null && !key.startsWith(prefix)) break
      list += value
    }

    return list
  }

  private fun throwIfNecessary() {
    val toThrow = nextException
    if (toThrow != null) {
      nextException = null
      throw toThrow
    }
  }

  private class Object(
    val key: String,
  ) {
    var value = ByteString.EMPTY
    var contentType: String? = null
  }
}

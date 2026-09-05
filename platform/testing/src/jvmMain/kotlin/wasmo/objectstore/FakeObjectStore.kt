package wasmo.objectstore

import java.util.TreeMap
import okio.ByteString
import wit.wasmo.contenttype.ContentType
import wit.wasmo.objectstore.DeleteObjectRequest
import wit.wasmo.objectstore.Entry
import wit.wasmo.objectstore.EntryObject
import wit.wasmo.objectstore.GetObjectRequest
import wit.wasmo.objectstore.GetObjectResponse
import wit.wasmo.objectstore.Key
import wit.wasmo.objectstore.ListObjectsRequest
import wit.wasmo.objectstore.ListObjectsResponse
import wit.wasmo.objectstore.PutObjectRequest
import wit.wasmo.objectstore.PutObjectResponse

class FakeObjectStore : ObjectStore {
  var nextException: Exception? = null
  private val objects = TreeMap<Key, Object>(
    { a, b -> a.value.compareTo(b.value) },
  )

  operator fun get(key: String): ByteString? =
    objects[Key(key)]?.value

  override suspend fun put(request: PutObjectRequest): PutObjectResponse {
    throwIfNecessary()
    request.key.validateKey()
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
    request.key.validateKey()
    val o = objects[request.key]
    return GetObjectResponse(
      value = o?.value,
      etag = o?.value?.etag,
      contentType = o?.contentType,
    )
  }

  override suspend fun delete(request: DeleteObjectRequest) {
    throwIfNecessary()
    request.key.validateKey()
    objects.remove(request.key)
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
        Entry.Object(
          value = EntryObject(
            key = it.key,
            etag = it.value.etag,
            size = it.value.size.toULong(),
          ),
        )
      },
      nextRequest = null,
    )
  }

  private fun listObjects(prefix: String?): List<Object> {
    val map = when {
      prefix != null -> objects.tailMap(Key(prefix))
      else -> objects
    }

    val list = mutableListOf<Object>()

    for ((key, value) in map) {
      if (prefix != null && !key.value.startsWith(prefix)) break
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
    val key: Key,
  ) {
    var value = ByteString.EMPTY
    var contentType: ContentType? = null
  }
}

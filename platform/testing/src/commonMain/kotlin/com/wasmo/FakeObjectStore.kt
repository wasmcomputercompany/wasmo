package com.wasmo

import java.util.TreeMap
import okio.ByteString

class FakeObjectStore : ObjectStore {
  private val objects = TreeMap<String, Object>()

  override suspend fun put(request: PutObjectRequest): PutObjectResponse {
    val o = objects.getOrPut(request.key) { Object() }
    o.value = request.value
    return PutObjectResponse(
      etag = o.value.etag,
    )
  }

  override suspend fun get(request: GetObjectRequest): GetObjectResponse {
    val o = objects[request.key]
    return GetObjectResponse(
      value = o?.value,
    )
  }

  override suspend fun delete(request: DeleteObjectRequest): DeleteObjectResponse {
    objects.remove(request.key)
    return DeleteObjectResponse
  }

  // TODO: honor delimiter.
  // TODO: limit the result count.
  override suspend fun list(request: ListObjectsRequest): ListObjectsResponse {
    val prefix = request.prefix
    val map = when {
      prefix != null -> objects.tailMap(prefix)
      else -> objects
    }

    val list = mutableListOf<ListObjectsResponse.Entry>()

    for ((key, value) in map) {
      if (prefix != null && !key.startsWith(prefix)) break
      list += ListObjectsResponse.Object(
        key = key,
        etag = value.value.etag,
        size = value.value.size.toLong(),
      )
    }

    return ListObjectsResponse(
      entries = list,
      nextRequest = null,
    )
  }

  private class Object {
    var value = ByteString.EMPTY
  }
}

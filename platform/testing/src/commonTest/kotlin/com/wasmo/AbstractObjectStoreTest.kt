package com.wasmo

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8

abstract class AbstractObjectStoreTest {
  private val casino = "casino".encodeUtf8()
  private val subway = "subway".encodeUtf8()
  private val freedom = "freedom".encodeUtf8()
  private val juliette = "juliette".encodeUtf8()

  abstract val store: ObjectStore

  @Test
  fun putAndGet() = runTest {
    store.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    store.put(PutObjectRequest("shows/pokerface/s1e2.mp4", subway))

    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e1.mp4")))
      .isEqualTo(GetObjectResponse(casino))
    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e2.mp4")))
      .isEqualTo(GetObjectResponse(subway))
  }

  @Test
  fun list() = runTest {
    store.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    store.put(PutObjectRequest("shows/pokerface/s1e2.mp4", subway))
    store.put(PutObjectRequest("shows/silo/s1e1.mp4", freedom))
    store.put(PutObjectRequest("shows/silo/s1e2.mp4", juliette))

    val response = store.list(ListObjectsRequest())
    assertThat(response.entries).containsExactly(
      ListObjectsResponse.Object("shows/pokerface/s1e1.mp4", casino.etag, casino.size.toLong()),
      ListObjectsResponse.Object("shows/pokerface/s1e2.mp4", subway.etag, subway.size.toLong()),
      ListObjectsResponse.Object("shows/silo/s1e1.mp4", freedom.etag, freedom.size.toLong()),
      ListObjectsResponse.Object("shows/silo/s1e2.mp4", juliette.etag, juliette.size.toLong()),
    )
  }

  @Test
  fun listWithPrefix() = runTest {
    store.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    store.put(PutObjectRequest("shows/pokerface/s1e2.mp4", subway))
    store.put(PutObjectRequest("shows/silo/s1e1.mp4", freedom))
    store.put(PutObjectRequest("shows/silo/s1e2.mp4", juliette))

    val pokerfaceResponse = store.list(ListObjectsRequest(prefix = "shows/pokerface/"))
    assertThat(pokerfaceResponse.entries).containsExactly(
      ListObjectsResponse.Object("shows/pokerface/s1e1.mp4", casino.etag, casino.size.toLong()),
      ListObjectsResponse.Object("shows/pokerface/s1e2.mp4", subway.etag, subway.size.toLong()),
    )
    val siloResponse = store.list(ListObjectsRequest(prefix = "shows/silo/"))
    assertThat(siloResponse.entries).containsExactly(
      ListObjectsResponse.Object("shows/silo/s1e1.mp4", freedom.etag, freedom.size.toLong()),
      ListObjectsResponse.Object("shows/silo/s1e2.mp4", juliette.etag, juliette.size.toLong()),
    )
  }

  @Test
  fun delete() = runTest {
    store.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    store.delete(DeleteObjectRequest("shows/pokerface/s1e1.mp4"))

    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e1.mp4")))
      .isEqualTo(GetObjectResponse(value = null))
    assertThat(store.list(ListObjectsRequest()).entries).isEmpty()
  }
}

package com.wasmo.objectstore

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8

class ScopedObjectStoreTest : AbstractObjectStoreTest() {
  private val casino = "casino".encodeUtf8()
  private val subway = "subway".encodeUtf8()
  private val unscopedStore = FakeObjectStore()
  override val store = ScopedObjectStore(
    delegate = unscopedStore,
    prefix = "shows/pokerface/",
  )

  @Test
  fun scopedPutAndGet() = runTest {
    unscopedStore.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    store.put(PutObjectRequest("s1e2.mp4", subway))

    assertThat(unscopedStore.get(GetObjectRequest("shows/pokerface/s1e1.mp4")))
      .isEqualTo(GetObjectResponse(value = casino))
    assertThat(unscopedStore.get(GetObjectRequest("shows/pokerface/s1e2.mp4")))
      .isEqualTo(GetObjectResponse(value = subway))
    assertThat(unscopedStore.get(GetObjectRequest("s1e1.mp4")))
      .isEqualTo(GetObjectResponse(value = null))
    assertThat(unscopedStore.get(GetObjectRequest("s1e2.mp4")))
      .isEqualTo(GetObjectResponse(value = null))

    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e1.mp4")))
      .isEqualTo(GetObjectResponse(value = null))
    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e2.mp4")))
      .isEqualTo(GetObjectResponse(value = null))
    assertThat(store.get(GetObjectRequest("s1e1.mp4")))
      .isEqualTo(GetObjectResponse(value = casino))
    assertThat(store.get(GetObjectRequest("s1e2.mp4")))
      .isEqualTo(GetObjectResponse(value = subway))
  }

  @Test
  fun scopedList() = runTest {
    unscopedStore.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    store.put(PutObjectRequest("s1e2.mp4", subway))

    assertThat(unscopedStore.list(ListObjectsRequest()).entries)
      .containsExactly(
        ListObjectsResponse.Object("shows/pokerface/s1e1.mp4", casino.etag, casino.size.toLong()),
        ListObjectsResponse.Object("shows/pokerface/s1e2.mp4", subway.etag, subway.size.toLong()),
      )
    assertThat(unscopedStore.list(ListObjectsRequest(prefix = "shows/pokerface/")).entries)
      .containsExactly(
        ListObjectsResponse.Object("shows/pokerface/s1e1.mp4", casino.etag, casino.size.toLong()),
        ListObjectsResponse.Object("shows/pokerface/s1e2.mp4", subway.etag, subway.size.toLong()),
      )
    assertThat(store.list(ListObjectsRequest()).entries)
      .containsExactly(
        ListObjectsResponse.Object("s1e1.mp4", casino.etag, casino.size.toLong()),
        ListObjectsResponse.Object("s1e2.mp4", subway.etag, subway.size.toLong()),
      )
    assertThat(store.list(ListObjectsRequest(prefix = "shows/pokerface/")).entries)
      .isEmpty()
  }

  @Test
  fun scopedDelete() = runTest {
    unscopedStore.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    unscopedStore.put(PutObjectRequest("shows/pokerface/s1e2.mp4", subway))

    store.delete(DeleteObjectRequest("s1e1.mp4"))
    store.delete(DeleteObjectRequest("shows/pokerface/s1e2.mp4")) // Not deleted.

    assertThat(unscopedStore.get(GetObjectRequest("shows/pokerface/s1e1.mp4")))
      .isEqualTo(GetObjectResponse(value = null))
    assertThat(unscopedStore.get(GetObjectRequest("shows/pokerface/s1e2.mp4")))
      .isEqualTo(GetObjectResponse(value = subway))
    assertThat(unscopedStore.list(ListObjectsRequest()).entries).containsExactly(
      ListObjectsResponse.Object("shows/pokerface/s1e2.mp4", subway.etag, subway.size.toLong()),
    )
  }
}

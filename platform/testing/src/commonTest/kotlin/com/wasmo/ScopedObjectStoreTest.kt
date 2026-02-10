package com.wasmo

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
  override val store = FakeObjectStore()
  private val pokerfaceStore = ScopedObjectStore(
    prefix = "shows/pokerface/",
    delegate = store,
  )

  @Test
  fun scopedPutAndGet() = runTest {
    store.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    pokerfaceStore.put(PutObjectRequest("s1e2.mp4", subway))

    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e1.mp4")))
      .isEqualTo(GetObjectResponse(value = casino))
    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e2.mp4")))
      .isEqualTo(GetObjectResponse(value = subway))
    assertThat(store.get(GetObjectRequest("s1e1.mp4")))
      .isEqualTo(GetObjectResponse(value = null))
    assertThat(store.get(GetObjectRequest("s1e2.mp4")))
      .isEqualTo(GetObjectResponse(value = null))

    assertThat(pokerfaceStore.get(GetObjectRequest("shows/pokerface/s1e1.mp4")))
      .isEqualTo(GetObjectResponse(value = null))
    assertThat(pokerfaceStore.get(GetObjectRequest("shows/pokerface/s1e2.mp4")))
      .isEqualTo(GetObjectResponse(value = null))
    assertThat(pokerfaceStore.get(GetObjectRequest("s1e1.mp4")))
      .isEqualTo(GetObjectResponse(value = casino))
    assertThat(pokerfaceStore.get(GetObjectRequest("s1e2.mp4")))
      .isEqualTo(GetObjectResponse(value = subway))
  }

  @Test
  fun scopedList() = runTest {
    store.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    pokerfaceStore.put(PutObjectRequest("s1e2.mp4", subway))

    assertThat(store.list(ListObjectsRequest()).entries)
      .containsExactly(
        ListObjectsResponse.Object("shows/pokerface/s1e1.mp4", casino.etag, casino.size.toLong()),
        ListObjectsResponse.Object("shows/pokerface/s1e2.mp4", subway.etag, subway.size.toLong()),
      )
    assertThat(store.list(ListObjectsRequest(prefix = "shows/pokerface/")).entries)
      .containsExactly(
        ListObjectsResponse.Object("shows/pokerface/s1e1.mp4", casino.etag, casino.size.toLong()),
        ListObjectsResponse.Object("shows/pokerface/s1e2.mp4", subway.etag, subway.size.toLong()),
      )
    assertThat(pokerfaceStore.list(ListObjectsRequest()).entries)
      .containsExactly(
        ListObjectsResponse.Object("s1e1.mp4", casino.etag, casino.size.toLong()),
        ListObjectsResponse.Object("s1e2.mp4", subway.etag, subway.size.toLong()),
      )
    assertThat(pokerfaceStore.list(ListObjectsRequest(prefix = "shows/pokerface/")).entries)
      .isEmpty()
  }

  @Test
  fun scopedDelete() = runTest {
    store.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    store.put(PutObjectRequest("shows/pokerface/s1e2.mp4", subway))

    pokerfaceStore.delete(DeleteObjectRequest("s1e1.mp4"))
    pokerfaceStore.delete(DeleteObjectRequest("shows/pokerface/s1e2.mp4")) // Not deleted.

    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e1.mp4")))
      .isEqualTo(GetObjectResponse(value = null))
    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e2.mp4")))
      .isEqualTo(GetObjectResponse(value = subway))
    assertThat(store.list(ListObjectsRequest()).entries).containsExactly(
      ListObjectsResponse.Object("shows/pokerface/s1e2.mp4", subway.etag, subway.size.toLong()),
    )
  }
}

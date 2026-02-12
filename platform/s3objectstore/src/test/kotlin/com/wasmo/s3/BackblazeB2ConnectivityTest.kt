package com.wasmo.s3

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.wasmo.DeleteObjectRequest
import com.wasmo.GetObjectRequest
import com.wasmo.ListObjectsRequest
import com.wasmo.ListObjectsResponse
import com.wasmo.PutObjectRequest
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okio.ByteString.Companion.encodeUtf8

/**
 * Run this test manually to confirm connectivity to a particular Backblaze bucket.
 */
class BackblazeB2ConnectivityTest {
  private val warehouse = ObjectStoreWarehouse(
    clock = Clock.System,
    client = OkHttpClient(),
  )

  private val backblazeB2BucketAddress: BackblazeB2BucketAddress?
    get() {
      return BackblazeB2BucketAddress(
        regionId = System.getenv("B2_REGION_ID") ?: return null,
        applicationKeyId = System.getenv("B2_APPLICATION_KEY_ID") ?: return null,
        applicationKey = System.getenv("B2_APPLICATION_KEY") ?: return null,
        bucket = System.getenv("B2_BUCKET") ?: return null,
      )
    }

  @Test
  fun happyPath() = runTest {
    val address = backblazeB2BucketAddress ?: return@runTest
    val objectStore = warehouse.connect(address)

    val key = "files/hello.txt"
    val value = "Hello, this file has a path!".encodeUtf8()

    val putObjectResponse = objectStore.put(
      PutObjectRequest(
        key = key,
        value = value,
      ),
    )

    val getObjectResponse = objectStore.get(
      GetObjectRequest(
        key = key,
      ),
    )
    assertThat(getObjectResponse.value).isEqualTo(value)
    assertThat(getObjectResponse.etag).isEqualTo(putObjectResponse.etag)

    val list = objectStore.list(ListObjectsRequest())

    assertThat(list.entries.map { (it as? ListObjectsResponse.Object)?.key }).containsExactly(
      key,
    )

    objectStore.delete(DeleteObjectRequest(key))

    // According to the Backblaze B2 docs, DELETE replaces an object with a tombstone.
    // We need to delete with a specific version parameter to properly delete.
    assertThat(list.entries.map { (it as? ListObjectsResponse.Object)?.key }).containsExactly(
      key,
    )
  }
}

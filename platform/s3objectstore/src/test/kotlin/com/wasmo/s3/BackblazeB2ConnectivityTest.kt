package com.wasmo.s3

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.wasmo.DeleteObjectRequest
import com.wasmo.GetObjectRequest
import com.wasmo.ListObjectsRequest
import com.wasmo.ListObjectsResponse
import com.wasmo.PutObjectRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okio.ByteString.Companion.encodeUtf8

/**
 * Run this test manually to confirm connectivity to a particular Backblaze bucket.
 */
class BackblazeB2ConnectivityTest {
  @Test
  @Ignore
  fun happyPath() {
    val key = "files/hello.txt"
    val value = "Hello, this file has a path!".encodeUtf8()

    runTest {
      val b2ApplicationKeyId = System.getenv("B2_APPLICATION_KEY_ID")
        ?: error("required env B2_APPLICATION_KEY_ID not set")
      val b2ApplicationKey = System.getenv("B2_APPLICATION_KEY")
        ?: error("required env B2_APPLICATION_KEY not set")
      val b2Bucket = System.getenv("B2_BUCKET")
        ?: error("required env B2_BUCKET not set")

      val objectStore = connectB2(
        clock = Clock.System,
        client = OkHttpClient(),
        region = B2Region("ca-east-006"),
        applicationKeyId = b2ApplicationKeyId,
        applicationKey = b2ApplicationKey,
        bucket = b2Bucket,
      )

      val putObjectResponse = objectStore.put(
        PutObjectRequest(
          key = key,
          value = value,
        ),
      )
      println(putObjectResponse)

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
}

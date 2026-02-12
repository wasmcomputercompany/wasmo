package com.wasmo.s3

import com.wasmo.ListObjectsRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient

/**
 * but there is a limit of 100 buckets per account.
 */
class BackblazeB2ConnectivityTest {
  @Test
  @Ignore
  fun happyPath() = runTest {
    val b2ApplicationKeyId = System.getenv("B2_APPLICATION_KEY_ID")
      ?: error("required env B2_APPLICATION_KEY_ID not set")
    val b2ApplicationKey = System.getenv("B2_APPLICATION_KEY")
      ?: error("required env B2_APPLICATION_KEY not set")
    val b2Bucket = System.getenv("B2_BUCKET")
      ?: error("required env B2_BUCKET not set")

    val objectStore = connectB2(
      client = OkHttpClient(),
      region = B2Region.CaEast,
      applicationKeyId = b2ApplicationKeyId,
      applicationKey = b2ApplicationKey,
      bucket = b2Bucket,
    )

    val list = objectStore.list(
      ListObjectsRequest(),
    )

    println(list)
  }
}

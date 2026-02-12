package com.wasmo.s3

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.FakeClock
import com.wasmo.s3.AwsRequestSigV4Signer.Companion.DefaultSignedHeaderNames
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * This test uses golden value from this test:
 * https://github.com/aws/aws-sdk-java-v2/blob/master/core/auth/src/test/java/software/amazon/awssdk/auth/signer/Aws4SignerTest.java
 */
class AwsRequestSigV4SignerTest {
  private val clock = FakeClock()
  private val signer = AwsRequestSigV4Signer(
    clock = clock,
    signedHeaderNames = DefaultSignedHeaderNames + "x-amz-archive-description",
    accessKeyId = "access",
    secretAccessKey = "secret",
    region = "us-east-1",
    service = "demo",
  )

  @BeforeTest
  fun beforeTest() {
    clock.now = Instant.parse("1981-02-16T06:30:00Z")
  }

  @Test
  fun dateTimeFormatting() {
    val now = clock.now.toLocalDateTime(TimeZone.UTC)
    assertThat(AwsRequestSigV4Signer.AmzDateFormat.format(now)).isEqualTo("19810216T063000Z")
    assertThat(AwsRequestSigV4Signer.CredentialDateFormat.format(now.date)).isEqualTo("19810216")
  }

  @Test
  fun postRequest() {
    val request = Request.Builder()
      .url("http://demo.us-east-1.amazonaws.com/".toHttpUrl())
      .post("{\"TableName\": \"foo\"}".toRequestBody())
      .header("x-amz-archive-description", "test  test")
      .header("host", "demo.us-east-1.amazonaws.com")
      .build()

    val signedRequest = signer.sign(request, Protocol.HTTP_1_1)
    assertThat(signedRequest.header("Authorization"))
      .isEqualTo(
        "AWS4-HMAC-SHA256 Credential=access/19810216/us-east-1/demo/aws4_request, " +
          "SignedHeaders=host;x-amz-archive-description;x-amz-date, " +
          "Signature=77fe7c02927966018667f21d1dc3dfad9057e58401cbb9ed64f1b7868288e35a",
      )
  }

  @Test
  fun queryParameterWithEmptyValue() {
    val request = Request.Builder()
      .url("http://demo.us-east-1.amazonaws.com/?Foo".toHttpUrl())
      .post("{\"TableName\": \"foo\"}".toRequestBody())
      .header("x-amz-archive-description", "test  test")
      .header("host", "demo.us-east-1.amazonaws.com")
      .build()

    val signedRequest = signer.sign(request, Protocol.HTTP_1_1)
    assertThat(signedRequest.header("Authorization"))
      .isEqualTo(
        "AWS4-HMAC-SHA256 Credential=access/19810216/us-east-1/demo/aws4_request, " +
          "SignedHeaders=host;x-amz-archive-description;x-amz-date, " +
          "Signature=c45a3ff1f028e83017f3812c06b4440f0b3240264258f6e18cd683b816990ba4",
      )
  }
}

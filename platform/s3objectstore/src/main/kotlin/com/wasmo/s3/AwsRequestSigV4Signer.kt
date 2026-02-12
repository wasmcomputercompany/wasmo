package com.wasmo.s3

import java.util.Locale
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import okio.BufferedSink
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import okio.HashingSink
import okio.blackholeSink
import okio.buffer

/**
 * Implements AWS Signature Version 4 for API requests.
 *
 * https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_sigv.html
 * https://github.com/aws-samples/sigv4-signing-examples/blob/main/no-sdk/java/AWSSigner.java
 */
class AwsRequestSigV4Signer(
  private val clock: Clock,
  private val signedHeaderNames: Set<String> = DefaultSignedHeaderNames,
  private val accessKeyId: String,
  private val secretAccessKey: String,
  private val region: String,
  private val service: String,
) : Interceptor {
  init {
    require(region == region.lowercase(Locale.ROOT))
    require(service == service.lowercase(Locale.ROOT))
  }

  override fun intercept(chain: Interceptor.Chain): Response {
    val signedRequest = sign(
      chain.request(),
      chain.connection()?.protocol() ?: Protocol.HTTP_1_1,
    )
    return chain.proceed(signedRequest)
  }

  fun sign(request: Request, protocol: Protocol): Request {
    val nowUtc = clock.now().toLocalDateTime(TimeZone.UTC)
    val date = CredentialDateFormat.format(value = nowUtc.date)
    val amzDate = AmzDateFormat.format(nowUtc)

    val headersBuilder = request.headers.newBuilder()
    headersBuilder.add("x-amz-date", amzDate)

    val signedHeaders = mutableListOf<Pair<String, String>>()
    for ((name, value) in headersBuilder.build()) {
      signedHeaders += when (val lowercaseName = name.lowercase(Locale.ROOT)) {
        "host" -> {
          val wireName = when (protocol) {
            Protocol.HTTP_2 -> ":authority"
            else -> "host"
          }
          wireName to value.trim()
        }

        in signedHeaderNames -> lowercaseName to value.trim()
        else -> continue
      }
    }

    signedHeaders.sortBy { it.first }
    val signedHeaderNames = signedHeaders.joinToString(separator = ";") { it.first }

    val canonicalRequest = Buffer().apply {
      writeCanonicalRequest(request, signedHeaders, signedHeaderNames)
    }

    val credentialScope = "$date/$region/$service/aws4_request"
    val stringToSign =
      "$Aws4HmacSha256\n$amzDate\n$credentialScope\n${canonicalRequest.sha256().hex()}"

    val signingKey = getSignatureKey(date)
    val signature = stringToSign.encodeUtf8().hmacSha256(signingKey)

    headersBuilder.add(
      "authorization",
      "$Aws4HmacSha256 Credential=$accessKeyId/$credentialScope, SignedHeaders=$signedHeaderNames, Signature=${signature.hex()}",
    )

    return request.newBuilder()
      .headers(headersBuilder.build())
      .build()
  }

  private fun getSignatureKey(date: String): ByteString {
    val secretKey = "AWS4$secretAccessKey".encodeUtf8()
    val dateKey = date.encodeUtf8().hmacSha256(secretKey)
    val regionKey = region.encodeUtf8().hmacSha256(dateKey)
    val serviceKey = service.encodeUtf8().hmacSha256(regionKey)
    return "aws4_request".encodeUtf8().hmacSha256(serviceKey)
  }

  private fun BufferedSink.writeCanonicalRequest(
    request: Request,
    signedHeaders: List<Pair<String, String>>,
    signedHeaderNames: String,
  ) {
    writeUtf8(request.method)
    writeUtf8("\n")

    writeUtf8(request.url.encodedPath)
    writeUtf8("\n")

    writeCanonicalQuery(request.url)
    writeUtf8("\n")

    for ((name, value) in signedHeaders) {
      writeUtf8(name)
      writeUtf8(":")
      writeUtf8(value.replace(Regex("\\s+"), " "))
      writeUtf8("\n")
    }
    writeUtf8("\n")

    writeUtf8(signedHeaderNames)
    writeUtf8("\n")

    val payloadSink = HashingSink.sha256(blackholeSink())
    payloadSink.buffer().use { sink ->
      request.body?.writeTo(sink)
    }
    writeUtf8(payloadSink.hash.hex())
  }

  private fun BufferedSink.writeCanonicalQuery(url: HttpUrl) {
    val encodedQuery = url.encodedQuery ?: return
    val sortedParts = encodedQuery.split("&")
      .map {
        when {
          it.contains("=") -> it
          else -> "$it="
        }
      }
      .sorted()

    var first = true
    for (queryPart in sortedParts) {
      if (first) {
        first = false
      } else {
        writeUtf8("&")
      }
      writeUtf8(queryPart)
    }
  }

  companion object {
    val Aws4HmacSha256 = "AWS4-HMAC-SHA256"
    val CredentialDateFormat = LocalDate.Formats.ISO_BASIC
    val AmzDateFormat = LocalDateTime.Format {
      year()
      monthNumber()
      day()
      char('T')
      hour()
      minute()
      second()
      char('Z')
    }
    val DefaultSignedHeaderNames: Set<String> = setOf(":authority", "host", "x-amz-date")
  }
}

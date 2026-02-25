package com.wasmo.accounts

import com.wasmo.accounts.SessionCookieEncoder.Companion.SessionCookieJoseHeader
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8

/**
 * Encode and decode our session cookies.
 *
 * We do a JWT with a fixed configuration, [SessionCookieJoseHeader]. If ever this is inadequate,
 * we can replace it with something better.
 */
class SessionCookieEncoder(
  private val secret: ByteString,
) {
  fun encode(content: SessionCookie): String {
    val payload = SessionCookieJson.encodeToString(content).encodeUtf8()
    val headerBase64 = SessionCookieJoseHeader.base64Url()
    val payloadBase64 = payload.base64Url()
    val jwsSignature = Buffer()
      .writeUtf8(headerBase64)
      .writeUtf8(payloadBase64)
      .hmacSha256(secret)
    val jwsSignatureBase64 = jwsSignature.base64Url()
    return "$headerBase64.$payloadBase64.$jwsSignatureBase64"
  }

  /** Returns the validated cookie, or null if it's malformed or the signature doesn't match. */
  fun decode(cookie: String): SessionCookie? {
    val parts = cookie.split('.', limit = 3)
    if (parts.size != 3) return null

    val (headerBase64, payloadBase64, jwsSignatureBase64) = parts

    val header = headerBase64.decodeBase64() ?: return null
    val payload = payloadBase64.decodeBase64() ?: return null
    val jwsSignature = jwsSignatureBase64.decodeBase64() ?: return null

    val expectedJwsSignature = Buffer()
      .writeUtf8(headerBase64)
      .writeUtf8(payloadBase64)
      .hmacSha256(secret)

    if (jwsSignature != expectedJwsSignature) return null
    if (header != SessionCookieJoseHeader) return null

    return try {
      SessionCookieJson.decodeFromString(payload.utf8())
    } catch (_: SerializationException) {
      null
    } catch (_: IllegalArgumentException) {
      null
    }
  }

  private companion object {
    val SessionCookieJoseHeader = """{"alg":"HS256","typ":"JWT"}""".encodeUtf8()
    val SessionCookieJson = Json.Default
  }
}

package wasmo.http

import dev.eav.tomlkt.Toml
import kotlinx.serialization.encodeToString
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.encodeUtf8
import wit.wasmo.http.v0_1_0.Header
import wit.wasmo.http.v0_1_0.HttpRequest
import wit.wasmo.http.v0_1_0.HttpResponse

val HttpRequest.httpUrl: HttpUrl
  get() = url.value.toHttpUrl()

inline fun <reified T> HttpResponse(
  toml: Toml,
  code: Int = 200,
  headers: List<Header> = listOf(),
  body: T,
) = HttpResponse(
  code = code.toUInt(),
  headers = headers + Header("content-type", "application/toml"),
  body = toml.encodeToString<T>(body).encodeUtf8(),
)

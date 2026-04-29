package wasmo.http

import dev.eav.tomlkt.Toml
import kotlinx.serialization.encodeToString
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.encodeUtf8

val HttpRequest.httpUrl: HttpUrl
  get() = url.toHttpUrl()

inline fun <reified T> HttpResponse(
  toml: Toml,
  code: Int = 200,
  headers: List<Header> = listOf(),
  body: T,
) = HttpResponse(
  code = code,
  headers = headers + Header("content-type", "application/toml"),
  body = toml.encodeToString<T>(body).encodeUtf8(),
)

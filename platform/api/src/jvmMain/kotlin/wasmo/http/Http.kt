package wasmo.http

import dev.eav.tomlkt.Toml
import kotlinx.serialization.encodeToString
import okhttp3.HttpUrl
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

interface HttpService {
  suspend fun execute(request: HttpRequest): HttpResponse
}

data class HttpRequest(
  val method: String = "GET",
  val url: HttpUrl,
  val headers: List<Header> = listOf(),
  val body: ByteString? = null,
) {
  val contentType: String?
    get() = headers.firstOrNull { it.name.equals(other = "content-type", ignoreCase = true) }?.value
}

data class Header(
  val name: String,
  val value: String,
)

data class HttpResponse(
  val code: Int = 200,
  val headers: List<Header> = listOf(),
  val body: ByteString = ByteString.EMPTY,
) {
  val isSuccessful: Boolean
    get() = code in 200..299

  val contentType: String?
    get() = headers.firstOrNull { it.name.equals(other = "content-type", ignoreCase = true) }?.value

  companion object {
    inline operator fun <reified T> invoke(
      toml: Toml,
      code: Int = 200,
      headers: List<Header> = listOf(),
      body: T,
    ) = HttpResponse(
      code = code,
      headers = headers + Header("content-type", "application/toml"),
      body = toml.encodeToString<T>(body).encodeUtf8(),
    )
  }
}

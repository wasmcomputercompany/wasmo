package wasmo.http

import dev.eav.tomlkt.Toml
import kotlinx.serialization.encodeToString
import okhttp3.HttpUrl
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

interface HttpClient {
  suspend fun execute(request: HttpRequest): HttpResponse
}

data class HttpRequest(
  val method: String,
  val url: HttpUrl,
  val headers: List<Header> = listOf(),
  val body: ByteString? = null,
)

data class Header(
  val name: String,
  val value: String,
)

data class HttpResponse(
  val code: Int = 200,
  val headers: List<Header> = listOf(),
  val body: ByteString,
) {
  val isSuccessful: Boolean
    get() = code in 200..299

  val contentType: String?
    get() = headers.firstOrNull { it.name.equals(other = "Content-Type", ignoreCase = true) }?.value

  companion object {
    inline operator fun <reified T> invoke(
      toml: Toml,
      code: Int = 200,
      headers: List<Header> = listOf(),
      body: T,
    ) = HttpResponse(
      code = code,
      headers = headers + Header("Content-Type", "application/toml"),
      body = toml.encodeToString<T>(body).encodeUtf8(),
    )
  }
}

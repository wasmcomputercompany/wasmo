package com.wasmo.sendemail.postmark

import com.wasmo.sendemail.EmailMessage
import com.wasmo.sendemail.EmailSendFailedException
import com.wasmo.sendemail.SendEmailService
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Adapt the Postmark API.
 * https://postmarkapp.com/developer/api/overview#error-codes
 */
class PostmarkEmailService private constructor(
  private val postmarkApi: PostmarkApi,
) : SendEmailService {

  override suspend fun send(message: EmailMessage) {
    val response = try {
      postmarkApi.sendEmail(
        SendEmailRequest(
          From = message.from,
          To = message.to,
          Subject = message.subject,
          HtmlBody = message.html,
        ),
      )
    } catch (e: Exception) {
      throw EmailSendFailedException("HTTP call failed: $e")
    }

    val body = response.body()
      ?: throw EmailSendFailedException(
        "HTTP call failed, ${response.code()}: ${response.message()}",
      )

    if (body.ErrorCode != 0) {
      throw EmailSendFailedException(
        "Postmark call failed, ${body.ErrorCode}: ${body.Message}",
      )
    }
  }

  class Factory(
    private val credentials: PostmarkCredentials,
    private val client: OkHttpClient,
  ) {
    fun create(): PostmarkEmailService {
      val authenticatedHttpClient = client.newBuilder()
        .addNetworkInterceptor(PostmarkApiInterceptor(credentials.serverToken))
        .build()

      val retrofit = Retrofit.Builder()
        .client(authenticatedHttpClient)
        .baseUrl(credentials.baseUrl)
        .addConverterFactory(PostmarkJson.asConverterFactory("application/json".toMediaType()))
        .build()

      return PostmarkEmailService(
        postmarkApi = retrofit.create<PostmarkApi>(),
      )
    }
  }
}

val PostmarkProductionBaseUrl = "https://api.postmarkapp.com/".toHttpUrl()

val PostmarkJson = Json {
  ignoreUnknownKeys = true
}

data class PostmarkCredentials(
  val baseUrl: HttpUrl,
  val serverToken: String,
)

internal class PostmarkApiInterceptor(
  private val serverToken: String,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
    return chain.proceed(
      chain.request().newBuilder()
        .addHeader("X-Postmark-Server-Token", serverToken)
        .build(),
    )
  }
}

/**
 * https://postmarkapp.com/developer/api/email-api
 */
internal interface PostmarkApi {
  @POST("/email")
  suspend fun sendEmail(
    @Body request: SendEmailRequest,
  ): Response<SendEmailResponse>
}

@Serializable
internal data class SendEmailRequest(
  val From: String,
  val To: String,
  val Subject: String,
  val HtmlBody: String,
  val MessageStream: String = "outbound",
)

@Serializable
internal data class SendEmailResponse(
  val To: String,
  val SubmittedAt: Instant,
  val MessageID: String,
  val ErrorCode: Int,
  val Message: String,
)

package com.wasmo.installedapps

import com.wasmo.framework.ContentTypeDatabase
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.identifiers.ForInstalledApp
import com.wasmo.identifiers.InstalledAppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import wasmo.access.Caller
import wasmo.access.ComputerAccess
import wasmo.http.Header as PlatformHeader
import wasmo.http.HttpRequest
import wasmo.http.HttpResponse
import wasmo.objectstore.GetObjectRequest
import wasmo.objectstore.ObjectStore

/**
 * Attempts to satisfy an HTTP request according to our routing precedence rules.
 */
@Inject
@SingleIn(InstalledAppScope::class)
class RealInstalledAppHttpService(
  private val installedAppService: InstalledAppService,
  private val resourceLoaderFactory: ResourceLoader.Factory,
  private val contentTypeDatabase: ContentTypeDatabase,
  @ForInstalledApp private val objectStore: ObjectStore,
) : InstalledAppHttpService {
  override suspend fun execute(
    caller: Caller,
    request: Request,
  ): Response<ResponseBody> {
    val encodedUrlPath = request.url.encodedPath
    val urlPath = when {
      encodedUrlPath.endsWith("/") -> "${encodedUrlPath}index.html"
      else -> encodedUrlPath
    }

    val publicObject = loadObjectOrNull(urlPath, "www-public")
    if (publicObject != null) return publicObject

    val publicResource = loadResourceOrNull(urlPath, "www-public")
    if (publicResource != null) return publicResource

    if (caller.computerAccess == ComputerAccess.Owner) {
      val privateObject = loadObjectOrNull(urlPath, "www")
      if (privateObject != null) return privateObject

      val privateResource = loadResourceOrNull(urlPath, "www")
      if (privateResource != null) return privateResource

      // TODO: pass the computerAccess to the HTTP service.
      val callHttpServiceResponse = callHttpService(request)
      if (callHttpServiceResponse != null) return callHttpServiceResponse
    }

    throw NotFoundUserException()
  }

  private suspend fun loadResourceOrNull(
    urlPath: String,
    prefix: String,
  ): Response<ResponseBody>? {
    val resourceLoader = resourceLoaderFactory.create()
    val privateResource = resourceLoader.loadOrNull("/$prefix$urlPath")
      ?: return null

    return Response(
      headers = listOf(),
      contentType = contentTypeDatabase[urlPath],
      body = ResponseBody {
        it.write(privateResource)
      },
    )
  }

  private suspend fun loadObjectOrNull(
    urlPath: String,
    prefix: String,
  ): Response<ResponseBody>? {
    val response = objectStore.get(
      GetObjectRequest(key = "$prefix$urlPath"),
    )
    val value = response.value ?: return null
    return Response(
      contentType = response.contentType?.toMediaTypeOrNull()
        ?: contentTypeDatabase[urlPath],
      body = ResponseBody {
        it.write(value)
      },
    )
  }

  private suspend fun callHttpService(request: Request): Response<ResponseBody>? {
    val httpService = installedAppService.app()?.httpService ?: return null
    val httpResponse = httpService.execute(
      request = request.toApplicationHttpRequest(),
    )
    return httpResponse.toOsHttpResponse()
  }

  private fun Request.toApplicationHttpRequest() = HttpRequest(
    method = method,
    url = url.toString(),
    headers = headers.map { PlatformHeader(it.name, it.value) },
    body = body,
  )

  private fun HttpResponse.toOsHttpResponse(): Response<ResponseBody> = Response(
    status = this.code,
    headers = this.headers,
    contentType = this.contentType?.toMediaTypeOrNull(),
    body = ResponseBody { sink -> sink.write(body) },
  )
}

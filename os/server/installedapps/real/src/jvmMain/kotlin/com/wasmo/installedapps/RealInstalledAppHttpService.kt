package com.wasmo.installedapps

import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.identifiers.AppSlug
import com.wasmo.packaging.AppManifest
import com.wasmo.wasm.AppLoader
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import wasmo.app.Platform
import wasmo.http.Header as PlatformHeader
import wasmo.http.HttpRequest
import wasmo.http.HttpResponse

/**
 * Follows the directions specified by the routes in the manifest.
 */
@Inject
@SingleIn(InstalledAppScope::class)
class RealInstalledAppHttpService(
  private val loader: AppLoader,
  private val platform: Platform,
  private val resourceLoaderFactory: ResourceLoader.Factory,
  private val pathMatcher: PathMatcher,
  private val appManifest: AppManifest,
  private val appSlug: AppSlug,
) : InstalledAppHttpService {
  override suspend fun execute(request: Request): Response<ResponseBody> {
    val urlPath = request.url.encodedPath
    val match = appManifest.route.firstNotNullOfOrNull { route ->
      pathMatcher.matchOrNull(route, urlPath)
    }

    if (match is PathMatch.Resource) {
      val resourceLoader = resourceLoaderFactory.create()
      val resource = resourceLoader.loadOrNull(match.path)
        ?: throw NotFoundUserException()
      return Response(
        headers = listOf(),
        body = ResponseBody {
          it.write(resource)
        },
      )
    }

    if (match is PathMatch.ObjectStore) {
      throw NotFoundUserException()
    }

    val loadedApp = loader.load(platform, appSlug)
    val loadedAppHttpService = loadedApp?.httpService
    if (loadedAppHttpService != null) {
      val execute = loadedAppHttpService.execute(request.toPlatformHttpRequest())
      return execute.toHostHttpResponse()
    }

    throw NotFoundUserException()
  }

  private fun Request.toPlatformHttpRequest() = HttpRequest(
    method = method,
    url = url,
    headers = headers.map { PlatformHeader(it.name, it.value) },
    body = body,
  )

  private fun HttpResponse.toHostHttpResponse(): Response<ResponseBody> = Response(
    status = this.code,
    headers = this.headers,
    contentType = this.contentType?.toMediaTypeOrNull(),
    body = ResponseBody { sink -> sink.write(body) },
  )
}

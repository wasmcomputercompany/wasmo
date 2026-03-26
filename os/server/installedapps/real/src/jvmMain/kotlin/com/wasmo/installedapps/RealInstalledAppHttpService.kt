package com.wasmo.installedapps

import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Route
import com.wasmo.wasm.AppLoader
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import wasmo.app.Platform
import wasmo.http.Header as PlatformHeader
import wasmo.http.HttpRequest
import wasmo.http.HttpResponse
import wasmo.objectstore.GetObjectRequest
import wasmo.objectstore.ObjectStore

/**
 * Follows the directions specified by the routes in the manifest.
 */
@Inject
@SingleIn(InstalledAppScope::class)
class RealInstalledAppHttpService(
  private val loader: AppLoader,
  private val platform: Platform,
  @ForInstalledApp private val objectStore: ObjectStore,
  private val appManifest: AppManifest,
  private val wasmoFileAddress: WasmoFileAddress,
) : InstalledAppHttpService {
  override suspend fun execute(request: Request): Response<ResponseBody> {
    val selectedRoute = appManifest.route
      .firstOrNull { route -> pathMatches(route.path, request.url.encodedPath) }

    val resourcePath = selectedRoute?.resource_path
    if (resourcePath != null) {
      return executeResourcePath(
        selectedRoute = selectedRoute,
        resourcePath = resourcePath,
        request = request,
      )
    }

    val objectsKey = selectedRoute?.objects_key
    if (objectsKey != null) {
      throw NotFoundUserException()
    }

    val loadedApp = loader.load(platform, appManifest)
    val loadedAppHttpService = loadedApp?.httpService
    if (loadedAppHttpService != null) {
      val execute = loadedAppHttpService.execute(request.toPlatformHttpRequest())
      return execute.toHostHttpResponse()
    }

    throw NotFoundUserException()
  }

  private suspend fun executeResourcePath(
    selectedRoute: Route,
    resourcePath: String,
    request: Request,
  ): Response<ResponseBody> {
    val wildcardStart = resourcePath.indexOf("/**")
    val key = when {
      wildcardStart != -1 -> {
        val prefix = resourcePath.substring(0, resourcePath.length - 2)
        val suffix = request.url.encodedPath.substring(selectedRoute.path.length - 2)
        prefix + suffix
      }

      else -> resourcePath.substring(0)
    }

    check(key.startsWith("/"))

    return when (wasmoFileAddress) {
      is WasmoFileAddress.FileSystem -> {
        TODO()
      }

      is WasmoFileAddress.Http -> {
        val getObjectResponse = objectStore.get(
          request = GetObjectRequest(
            key = "resources/v${appManifest.version}$key",
          ),
        )

        val responseBody = getObjectResponse.value
          ?: throw NotFoundUserException()

        Response(
          headers = listOf(),
          contentType = getObjectResponse.contentType?.toMediaTypeOrNull(),
          body = ResponseBody {
            it.write(responseBody)
          },
        )
      }
    }
  }

  fun pathMatches(routePath: String, urlPath: String): Boolean {
    return when {
      routePath.endsWith("/**") -> routePath.regionMatches(0, urlPath, 0, routePath.length - 2)
      else -> routePath == urlPath
    }
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

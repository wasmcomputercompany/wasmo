package com.wasmo.installedapps

import com.wasmo.framework.Header
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Route
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.objectstore.GetObjectRequest
import wasmo.objectstore.ObjectStore

/**
 * Follows the directions specified by the routes in the manifest.
 */
@Inject
@SingleIn(InstalledAppScope::class)
class RealInstalledAppHttpService(
  @ForInstalledApp private val objectStore: ObjectStore,
  private val manifest: AppManifest,
) : InstalledAppHttpService {
  override suspend fun execute(request: Request): Response<ResponseBody> {
    val selectedRoute = manifest.route
      .firstOrNull { route -> pathMatches(route.path, request.url.encodedPath) }
      ?: throw NotFoundUserException()

    val resourcePath = selectedRoute.resource_path
    if (resourcePath != null) {
      return executeResourcePath(
        selectedRoute = selectedRoute,
        resourcePath = resourcePath,
        request = request,
      )
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

    val getObjectRequest = GetObjectRequest(
      key = "resources/v${manifest.version}$key",
    )
    val getObjectResponse = objectStore.get(
      getObjectRequest,
    )

    val responseBody = getObjectResponse.value
      ?: throw NotFoundUserException()

    return Response(
      headers = listOf(
        Header("Content-Type", "image/svg+xml"), // TODO: get this from the object store.
      ),
      body = ResponseBody {
        it.write(responseBody)
      },
    )
  }

  fun pathMatches(routePath: String, urlPath: String): Boolean {
    return when {
      routePath.endsWith("/**") -> routePath.regionMatches(0, urlPath, 0, routePath.length - 2)
      else -> routePath == urlPath
    }
  }
}

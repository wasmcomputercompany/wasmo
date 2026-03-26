package com.wasmo.installedapps

import com.wasmo.packaging.Route
import dev.zacsweers.metro.Inject

/**
 * Given a route like this:
 *
 * ```toml
 * [[route]]
 * path = '/images/**'                                                           (nested comment */)
 * resource_path = '/static/pngs/**'                                             (nested comment */)
 * ```
 *
 * This takes an HTTP request for a path like `/images/logo.png` and returns a resource path
 * `/static/pngs/logo.png`.
 */
@Inject
class PathMatcher {
  fun matchOrNull(
    route: Route,
    urlPath: String,
  ): PathMatch? {
    val resourcePath = route.resource_path
    val objectsPath = route.objects_key
    return when {
      resourcePath != null -> {
        val match = matchOrNull(route.path, resourcePath, urlPath) ?: return null
        PathMatch.Resource(match)
      }

      objectsPath != null -> {
        val match = matchOrNull(route.path, objectsPath, urlPath) ?: return null
        PathMatch.ObjectStore(match)
      }

      else -> null
    }
  }

  private fun matchOrNull(
    inputPattern: String,
    outputPattern: String,
    path: String,
  ): String? {
    return when {
      inputPattern.endsWith("/**") -> {
        if (!inputPattern.regionMatches(0, path, 0, inputPattern.length - 2)) return null

        val wildcardStart = outputPattern.indexOf("/**")
        check(wildcardStart != -1) { "unexpected route" }

        val prefix = outputPattern.substring(0, outputPattern.length - 2)
        val suffix = path.substring(inputPattern.length - 2)
        prefix + suffix
      }

      inputPattern == path -> outputPattern

      else -> null
    }
  }
}

interface PathMatch {
  data class Resource(val path: String) : PathMatch
  data class ObjectStore(val path: String) : PathMatch
}

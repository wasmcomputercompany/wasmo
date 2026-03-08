package com.wasmo.packaging

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.ByteString.Companion.decodeHex

/**
 * Validates a manifest against our spec.
 *
 * We do validation here and not in `TOML` parsing because we want to aggregate all errors before
 * reporting any errors.
 *
 * We also want to exactly identify where the problems are, such as `route[3].resource_path` which
 * isn't possible in simple constructor argument checks.
 */
fun AppManifest.check(): List<Issue> {
  return IssueCollector.collect {
    check(this@check)
  }
}

private fun IssueCollector.check(manifest: AppManifest) {
  withContext("target") {
    issueCheck(manifest.target in SupportedTargets) {
      """
      |unsupported target '${manifest.target}'
      |expected one of $SupportedTargets
      """.trimMargin()
    }
  }

  withContext("version") {
    issueCheck(manifest.version >= 1) {
      """
      |unexpected version ${manifest.version}
      |expected a positive integer
      """.trimMargin()
    }
  }

  withContext("slug") {
    issueCheck(manifest.slug.matches(AppSlugRegex)) {
      """
      |unexpected app slug '${manifest.slug}'
      |must be 1-15 characters and match ${AppSlugRegex.pattern}
      """.trimMargin()
    }
  }

  for ((index, resource) in manifest.resource.withIndex()) {
    withContext("resource[$index]") {
      check(resource)
    }
  }

  for ((index, route) in manifest.route.withIndex()) {
    withContext("route[$index]") {
      check(route)
    }
  }

  val launcher = manifest.launcher
  if (launcher != null) {
    withContext("launcher") {
      check(launcher)
    }
  }
}

private fun IssueCollector.check(resource: Resource) {
  withContext("sha256") {
    val sha256 = resource.sha256
    val validSha256 = try {
      sha256 == null || sha256.decodeHex().size == 32
    } catch (_: Exception) {
      false
    }
    issueCheck(validSha256) {
      """
      |unexpected sha256 '${resource.sha256}'
      |must be 64 hex digits (32 bytes)
      """.trimMargin()
    }
  }

  withContext("content_type") {
    val contentType = resource.content_type
    issueCheck(contentType == null || contentType.toMediaTypeOrNull() != null) {
      """
      |unexpected content_type '${resource.content_type}'
      |must be a RFC 2045 media type
      """.trimMargin()
    }
  }

  withContext("resource_path") {
    val resourcePath = resource.resource_path
      ?: "https://example.com/".toHttpUrl().resolve(resource.url)?.encodedPath
    issueCheck(resourcePath != null && resourcePath.removePrefix("/").isNotEmpty()) {
      """
      |unexpected resource path '$resourcePath'
      |must be the non-empty path to download the resource to
      """.trimMargin()
    }
  }
}

private fun IssueCollector.check(route: Route) {
  withContext("path") {
    checkPath(
      path = route.path,
      allowTrailingWildcard = true,
    )
  }

  val pathHasTrailingWildcard = route.path.endsWith("/**")
  val resourcePath = route.resource_path
  val objectsKey = route.objects_key

  issueCheck(resourcePath == null || objectsKey == null) {
    "route may have a resource_path and an objects_key, but not both"
  }

  if (resourcePath != null) {
    withContext("resource_path") {
      checkPath(
        path = resourcePath,
        allowTrailingWildcard = pathHasTrailingWildcard,
        requireTrailingWildcard = pathHasTrailingWildcard,
      )
    }
  }

  if (objectsKey != null) {
    withContext("objects_key") {
      checkPath(
        path = objectsKey,
        allowTrailingWildcard = pathHasTrailingWildcard,
        requireTrailingWildcard = pathHasTrailingWildcard,
      )
    }
  }

  withContext("access") {
    issueCheck(route.access == null || route.access in SupportedAccessValues) {
      """
      |unsupported access '${route.access}'
      |expected one of $SupportedAccessValues
      """.trimMargin()
    }
  }
}

private fun IssueCollector.check(launcher: Launcher) {
  val maskableIconPath = launcher.maskable_icon_path
  if (maskableIconPath != null) {
    withContext("maskable_icon_path") {
      checkPath(
        path = launcher.maskable_icon_path,
      )
    }
  }
}

private fun IssueCollector.checkPath(
  path: String,
  allowTrailingWildcard: Boolean = false,
  requireTrailingWildcard: Boolean = false,
) {
  issueCheck(path.startsWith("/")) {
    "string must start with /"
  }

  issueCheck(!requireTrailingWildcard || path.endsWith("/**")) {
    "string must end with '/**'"
  }

  if (allowTrailingWildcard) {
    issueCheck("*" !in path.removeSuffix("/**")) {
      "string may not contain '*', except in a wildcard at the end"
    }
  } else {
    issueCheck("*" !in path) {
      "string may not contain '*'"
    }
  }
}

const val TargetSdk1 = "https://wasmo.com/sdk/1"
internal val SupportedTargets = setOf(TargetSdk1)
internal val SupportedAccessValues = setOf(
  "public",
  "private",
)

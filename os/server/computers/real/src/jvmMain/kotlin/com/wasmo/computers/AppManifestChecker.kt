package com.wasmo.computers

import com.wasmo.issues.IssueCollector
import com.wasmo.issues.issueCheck
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.ExternalResource
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.Route

/**
 * Validates a manifest against our spec.
 *
 * We do validation here and not in `TOML` parsing because we want to aggregate all errors before
 * reporting any errors.
 *
 * We also want to exactly identify where the problems are, such as `route[3].resource_path` which
 * isn't possible in simple constructor argument checks.
 */
class AppManifestChecker(
  val allowExternalResources: Boolean = false,
) {
  context(issueCollector: IssueCollector)
  fun check(manifest: AppManifest) {
    context(issueCollector.href("target")) {
      issueCheck(manifest.target in SupportedTargets) {
        """
        |unsupported target '${manifest.target}'
        |expected one of $SupportedTargets
        """.trimMargin()
      }
    }

    context(issueCollector.href("version")) {
      issueCheck(manifest.version >= 1) {
        """
        |unexpected version ${manifest.version}
        |expected a positive integer
        """.trimMargin()
      }
    }

    if (allowExternalResources) {
      for ((index, resource) in manifest.external_resource.withIndex()) {
        context(issueCollector.href("external_resource[$index]")) {
          check(resource)
        }
      }
    } else {
      context(issueCollector.href("external_resource")) {
        issueCheck(manifest.external_resource.isEmpty()) {
          "external resources are not permitted for this manifest"
        }
      }
    }

    for ((index, route) in manifest.route.withIndex()) {
      context(issueCollector.href("route[$index]")) {
        check(route)
      }
    }

    val launcher = manifest.launcher
    if (launcher != null) {
      context(issueCollector.href("launcher")) {
        check(launcher)
      }
    }
  }

  context(issueCollector: IssueCollector)
  private fun check(externalResource: ExternalResource) {
    context(issueCollector.href("to")) {
      issueCheck(".." !in externalResource.to.split("/")) {
        "target directory must not contain '..' path traversal operators"
      }

      issueCheck(externalResource.to.startsWith("/")) {
        "target directory must start with '/'"
      }

      for ((index, include) in externalResource.include.withIndex()) {
        context(issueCollector.href("include[$index]")) {
          issueCheck(!include.startsWith("/")) {
            "include must not start with '/'"
          }
        }
      }
    }
  }

  context(issueCollector: IssueCollector)
  private fun check(route: Route) {
    context(issueCollector.href("path")) {
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
      context(issueCollector.href("resource_path")) {
        checkPath(
          path = resourcePath,
          allowTrailingWildcard = pathHasTrailingWildcard,
          requireTrailingWildcard = pathHasTrailingWildcard,
        )
      }
    }

    if (objectsKey != null) {
      context(issueCollector.href("objects_key")) {
        checkPath(
          path = objectsKey,
          allowTrailingWildcard = pathHasTrailingWildcard,
          requireTrailingWildcard = pathHasTrailingWildcard,
        )
      }
    }

    context(issueCollector.href("access")) {
      issueCheck(route.access == null || route.access in SupportedAccessValues) {
        """
        |unsupported access '${route.access}'
        |expected one of $SupportedAccessValues
        """.trimMargin()
      }
    }
  }

  context(issueCollector: IssueCollector)
  private fun check(launcher: Launcher) {
    val maskableIconPath = launcher.maskable_icon_path
    if (maskableIconPath != null) {
      context(issueCollector.href("maskable_icon_path")) {
        checkPath(
          path = maskableIconPath,
        )
      }
    }
  }

  context(issueCollector: IssueCollector)
  private fun checkPath(
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
}

const val TargetSdk1 = "https://wasmo.com/sdk/1"
internal val SupportedTargets = setOf(TargetSdk1)
internal val SupportedAccessValues = setOf(
  "public",
  "private",
)

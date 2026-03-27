package com.wasmo.computers

import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsExactly
import com.wasmo.issues.Issue
import com.wasmo.issues.IssueCollector
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.ExternalResource
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.Route
import kotlin.test.Test

class AppManifestCheckerTest {
  private val manifest = AppManifest(
    target = "https://wasmo.com/sdk/1",
    version = 35,
  )

  @Test
  fun checkManifest() {
    assertThat(
      manifest.copy(target = "https://wasmo.com/sdk/0"),
    ).failsValidation(
      message =
        """
        |unsupported target 'https://wasmo.com/sdk/0'
        |expected one of $SupportedTargets
        """.trimMargin(),
      href = "target",
    )

    assertThat(
      manifest.copy(version = 0),
    ).failsValidation(
      message =
        """
        |unexpected version 0
        |expected a positive integer
        """.trimMargin(),
      href = "version",
    )
  }

  @Test
  fun checkExternalResourceNotPermittedByDefault() {
    assertThat(
      manifest.copy(
        external_resource = listOf(
          ExternalResource(
            from = "../client/build/dist/js/productionExecutable",
            to = "/assets",
            include = listOf("**/*.js", "**/*.js.map"),
          ),
        ),
      ),
    ).failsValidation(
      message = "external resources are not permitted for this manifest",
      href = "external_resource",
    )
  }

  @Test
  fun checkExternalResource() {
    val appManifestChecker = AppManifestChecker(
      allowExternalResources = true,
    )
    assertThat(
      manifest.copy(
        external_resource = listOf(
          ExternalResource(
            from = "../client/build/dist/js/productionExecutable",
            to = "../assets",
            include = listOf("**/*.js", "**/*.js.map"),
          ),
        ),
      ),
    ).failsValidation(
      message = "target directory must not contain '..' path traversal operators",
      href = "external_resource[0].to",
      appManifestChecker = appManifestChecker,
    )
  }

  @Test
  fun checkRoute() {
    assertThat(
      manifest.copy(
        route = listOf(
          Route(
            path = "icon.svg",
          ),
        ),
      ),
    ).failsValidation(
      message = "string must start with /",
      href = "route[0].path",
    )

    assertThat(
      manifest.copy(
        route = listOf(
          Route(
            path = "/icon*.svg",
          ),
        ),
      ),
    ).failsValidation(
      message = "string may not contain '*', except in a wildcard at the end",
      href = "route[0].path",
    )

    assertThat(
      manifest.copy(
        route = listOf(
          Route(
            path = "/icon.svg",
            resource_path = "/icon.svg/**",
          ),
        ),
      ),
    ).failsValidation(
      message = "string may not contain '*'",
      href = "route[0].resource_path",
    )

    assertThat(
      manifest.copy(
        route = listOf(
          Route(
            path = "/app-resources/**",
            resource_path = "/app-resources/",
          ),
        ),
      ),
    ).failsValidation(
      message = "string must end with '/**'",
      href = "route[0].resource_path",
    )

    assertThat(
      manifest.copy(
        route = listOf(
          Route(
            path = "/resources/**",
            resource_path = "/app-resources/**",
            objects_key = "/app-objects/**",
          ),
        ),
      ),
    ).failsValidation(
      message = "route may have a resource_path and an objects_key, but not both",
      href = "route[0]",
    )

    assertThat(
      manifest.copy(
        route = listOf(
          Route(
            path = "/icon.svg",
            objects_key = "icon.svg",
          ),
        ),
      ),
    ).failsValidation(
      message = "string must start with /",
      href = "route[0].objects_key",
    )

    assertThat(
      manifest.copy(
        route = listOf(
          Route(
            path = "/icon.svg",
            access = "secret",
          ),
        ),
      ),
    ).failsValidation(
      message = """
      |unsupported access 'secret'
      |expected one of $SupportedAccessValues
      """.trimMargin(),
      href = "route[0].access",
    )
  }

  @Test
  fun checkLauncher() {
    assertThat(
      manifest.copy(
        launcher = Launcher(
          label = "Recipes",
          maskable_icon_path = "recipes.svg",
        ),
      ),
    ).failsValidation(
      message = "string must start with /",
      href = "launcher.maskable_icon_path",
    )
  }
}

fun Assert<AppManifest>.failsValidation(
  href: String,
  message: String,
  appManifestChecker: AppManifestChecker = AppManifestChecker(),
) {
  transform { manifest ->
    IssueCollector.collect {
      appManifestChecker.check(manifest)
    }
  }.containsExactly(Issue(href = href, message = message))
}

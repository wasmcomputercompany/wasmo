package com.wasmo.packaging

import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsExactly
import com.wasmo.issues.Issue
import com.wasmo.issues.IssueCollector
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
            to = "/assets/../..",
            include = listOf("**/*.js", "**/*.js.map"),
          ),
        ),
      ),
    ).failsValidation(
      message = "target directory must not contain '..' path traversal operators",
      href = "external_resource[0].to",
      appManifestChecker = appManifestChecker,
    )

    assertThat(
      manifest.copy(
        external_resource = listOf(
          ExternalResource(
            from = "../client/build/dist/js/productionExecutable",
            to = "assets",
          ),
        ),
      ),
    ).failsValidation(
      message = "target directory must start with '/'",
      href = "external_resource[0].to",
      appManifestChecker = appManifestChecker,
    )

    assertThat(
      manifest.copy(
        external_resource = listOf(
          ExternalResource(
            from = "/Volumes/media",
            to = "/assets",
            include = listOf("/**/*.mp3"),
          ),
        ),
      ),
    ).failsValidation(
      message = "include must not start with '/'",
      href = "external_resource[0].include[0]",
      appManifestChecker = appManifestChecker,
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

    assertThat(
      manifest.copy(
        launcher = Launcher(
          label = "Recipes",
          home_path = "home",
        ),
      ),
    ).failsValidation(
      message = "string must start with /",
      href = "launcher.home_path",
    )

    assertThat(
      manifest.copy(
        launcher = Launcher(
          label = "Recipes",
          home_path = "//home",
        ),
      ),
    ).failsValidation(
      message = "string must not start with //",
      href = "launcher.home_path",
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

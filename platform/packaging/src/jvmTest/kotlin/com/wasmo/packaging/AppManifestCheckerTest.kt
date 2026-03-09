package com.wasmo.packaging

import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsExactly
import com.wasmo.identifiers.AppSlugRegex
import com.wasmo.issues.Issue
import kotlin.test.Test

class AppManifestCheckerTest {
  private val manifest = AppManifest(
    target = "https://wasmo.com/sdk/1",
    version = 35,
    slug = "recipes",
  )

  @Test
  fun checkManifest() {
    assertThat(
      manifest.copy(target = "https://wasmo.com/sdk/0"),
    ).failsValidation(
      context = "target",
      message =
        """
        |unsupported target 'https://wasmo.com/sdk/0'
        |expected one of $SupportedTargets
        """.trimMargin(),
    )

    assertThat(
      manifest.copy(version = 0),
    ).failsValidation(
      context = "version",
      message =
        """
        |unexpected version 0
        |expected a positive integer
        """.trimMargin(),
    )

    assertThat(
      manifest.copy(slug = "aaaaabbbbbcccccd"),
    ).failsValidation(
      context = "slug",
      message =
        """
        |unexpected app slug 'aaaaabbbbbcccccd'
        |must be 1-15 characters and match ${AppSlugRegex.pattern}
        """.trimMargin(),
    )
  }

  @Test
  fun checkResource() {
    assertThat(
      manifest.copy(
        resource = listOf(
          Resource(
            url = "recipes.zip",
            sha256 = "0102030405060708",
          ),
        ),
      ),
    ).failsValidation(
      context = "resource[0].sha256",
      message =
        """
        |unexpected sha256 '0102030405060708'
        |must be 64 hex digits (32 bytes)
        """.trimMargin(),
    )

    assertThat(
      manifest.copy(
        resource = listOf(
          Resource(
            url = "recipes.zip",
            content_type = "zip",
          ),
        ),
      ),
    ).failsValidation(
      context = "resource[0].content_type",
      message =
        """
        |unexpected content_type 'zip'
        |must be a RFC 2045 media type
        """.trimMargin(),
    )

    assertThat(
      manifest.copy(
        resource = listOf(
          Resource(
            url = "/",
          ),
        ),
      ),
    ).failsValidation(
      context = "resource[0].resource_path",
      message =
        """
        |unexpected resource path '/'
        |must be the non-empty path to download the resource to
        """.trimMargin(),
    )

    assertThat(
      manifest.copy(
        resource = listOf(
          Resource(
            url = "recipes.zip",
            resource_path = ""
          ),
        ),
      ),
    ).failsValidation(
      context = "resource[0].resource_path",
      message =
        """
        |unexpected resource path ''
        |must be the non-empty path to download the resource to
        """.trimMargin(),
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
      context = "route[0].path",
      message = "string must start with /",
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
      context = "route[0].path",
      message = "string may not contain '*', except in a wildcard at the end",
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
      context = "route[0].resource_path",
      message = "string may not contain '*'",
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
      context = "route[0].resource_path",
      message = "string must end with '/**'",
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
      context = "route[0]",
      message = "route may have a resource_path and an objects_key, but not both",
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
      context = "route[0].objects_key",
      message = "string must start with /",
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
      context = "route[0].access",
      message = """
      |unsupported access 'secret'
      |expected one of $SupportedAccessValues
      """.trimMargin(),
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
      context = "launcher.maskable_icon_path",
      message = "string must start with /",
    )
  }
}

fun Assert<AppManifest>.failsValidation(context: String, message: String) {
  transform { manifest -> manifest.check() }
    .containsExactly(Issue(context, message))
}

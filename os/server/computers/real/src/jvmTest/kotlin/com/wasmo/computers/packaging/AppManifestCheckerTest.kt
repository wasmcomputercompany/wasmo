package com.wasmo.computers.packaging

import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsExactly
import com.wasmo.issues.Issue
import com.wasmo.packaging.AppManifest
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
  fun checkResource() {
//    assertThat(
//      manifest.copy(
//        resource = listOf(
//          ExternalResource(
//            url = "recipes.zip",
//            sha256 = "0102030405060708",
//          ),
//        ),
//      ),
//    ).failsValidation(
//      context = "resource[0].sha256",
//      message =
//        """
//        |unexpected sha256 '0102030405060708'
//        |must be 64 hex digits (32 bytes)
//        """.trimMargin(),
//    )

//    assertThat(
//      manifest.copy(
//        resource = listOf(
//          ExternalResource(
//            url = "recipes.zip",
//            content_type = "zip",
//          ),
//        ),
//      ),
//    ).failsValidation(
//      context = "resource[0].content_type",
//      message =
//        """
//        |unexpected content_type 'zip'
//        |must be a RFC 2045 media type
//        """.trimMargin(),
//    )
//
//    assertThat(
//      manifest.copy(
//        resource = listOf(
//          ExternalResource(
//            url = "/",
//          ),
//        ),
//      ),
//    ).failsValidation(
//      context = "resource[0].resource_path",
//      message =
//        """
//        |unexpected resource path '/'
//        |must be the non-empty path to download the resource to
//        """.trimMargin(),
//    )
//
//    assertThat(
//      manifest.copy(
//        resource = listOf(
//          ExternalResource(
//            url = "recipes.zip",
//            resource_path = ""
//          ),
//        ),
//      ),
//    ).failsValidation(
//      context = "resource[0].resource_path",
//      message =
//        """
//        |unexpected resource path ''
//        |must be the non-empty path to download the resource to
//        """.trimMargin(),
//    )
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

fun Assert<AppManifest>.failsValidation(href: String, message: String) {
  transform { manifest -> manifest.check() }
    .containsExactly(Issue(href = href, message = message))
}

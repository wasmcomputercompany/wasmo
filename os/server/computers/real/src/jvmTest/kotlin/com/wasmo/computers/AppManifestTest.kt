package com.wasmo.computers

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.ExternalResource
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.Route
import com.wasmo.packaging.WasmoToml
import kotlin.test.Test

class AppManifestTest {
  @Test
  fun happyPath() {
    val wasmoManifestString = """
      |target = 'https://wasmo.com/sdk/1'
      |version = 35
      |
      |[[external_resource]]
      |from = '../build/dist/js/developmentExecutable'
      |to = '/static'
      |include = ['**/*.js', '**/*.js.map']
      |
      |[[route]]
      |path = '/static/**'
      |resource_path = '/static/**'
      |
      |[launcher]
      |label = 'Recipes'
      |maskable_icon_path = '/static/launcher-icon.svg'
      """.trimMargin()

    val decoded = WasmoToml.decodeFromString(
      AppManifest.serializer(),
      wasmoManifestString,
    )

    assertThat(decoded).isEqualTo(
      AppManifest(
        target = "https://wasmo.com/sdk/1",
        version = 35,
        external_resource = listOf(
          ExternalResource(
            from = "../build/dist/js/developmentExecutable",
            to = "/static",
            include = listOf("**/*.js", "**/*.js.map"),
          ),
        ),
        route = listOf(
          Route(
            path = "/static/**",
            resource_path = "/static/**",
          ),
        ),
        launcher = Launcher(
          label = "Recipes",
          maskable_icon_path = "/static/launcher-icon.svg",
        ),
      ),
    )
  }
}

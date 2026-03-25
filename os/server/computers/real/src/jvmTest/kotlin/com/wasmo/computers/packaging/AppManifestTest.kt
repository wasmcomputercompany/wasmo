package com.wasmo.computers.packaging

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.packaging.AppManifest
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
      |slug = 'recipes'
      |base_url = 'https://example.com/recipes/v35/'
      |
      |[[resource]]
      |url = 'recipes.zip'
      |unzip = true
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
        slug = "recipes",
        external_resource = listOf(
//          ExternalResource(
//            url = "recipes.zip",
//            unzip = true,
//          ),
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

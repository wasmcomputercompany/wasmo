package com.wasmo.packaging

import assertk.assertThat
import assertk.assertions.isEqualTo
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
        base_url = "https://example.com/recipes/v35/",
        resource = listOf(
          Resource(
            url = "recipes.zip",
            unzip = true,
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

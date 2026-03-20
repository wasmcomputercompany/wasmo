package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.framework.ContentTypes
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.packaging.Resource
import com.wasmo.packaging.Route
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.framework.ResponseBodySnapshot
import com.wasmo.testing.framework.snapshot
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.encodeUtf8

class InstalledAppResourceRouteTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val client = tester.newClient()
    val computer = client.createComputer()
    val publishedApp = RecipesApp.PublishedApp.copy(
      manifest = RecipesApp.PublishedApp.manifest.copy(
        resource = RecipesApp.PublishedApp.manifest.resource + listOf(
          Resource(
            url = "cookies.txt",
            resource_path = "desserts/cookies.txt",
            content_type = ContentTypes.TextPlain.toString(),
          ),
        ),
        route = RecipesApp.PublishedApp.manifest.route + listOf(
          Route(
            path = "/sweets/desserts/cookies",
            resource_path = "/desserts/cookies.txt",
          ),
        ),
      ),
      servedResources = RecipesApp.PublishedApp.servedResources + mapOf(
        "https://example.com/recipes/v1/cookies.txt".toHttpUrl() to "cookie recipe!".encodeUtf8(),
      ),
    )
    tester.publishApp(publishedApp)
    val installedApp = computer.installApp(publishedApp)

    val response = client.call().callApp(
      Request(
        method = "GET",
        url = installedApp.url.resolve("/sweets/desserts/cookies")!!,
      ),
    )

    assertThat(response.snapshot()).isEqualTo(
      Response(
        contentType = ContentTypes.TextPlain,
        body = ResponseBodySnapshot("cookie recipe!"),
      ),
    )
  }
}

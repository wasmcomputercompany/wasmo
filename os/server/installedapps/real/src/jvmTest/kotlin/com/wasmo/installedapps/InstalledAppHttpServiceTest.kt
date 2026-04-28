package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Response
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.framework.ResponseBodySnapshot
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okio.ByteString.Companion.encodeUtf8
import wasmo.objectstore.PutObjectRequest

class InstalledAppHttpServiceTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun resourceInWww() = runTest {
    val app = tester.sampleApps.recipes.publishedApp.copy(
      resources = tester.sampleApps.recipes.publishedApp.resources + mapOf(
        "www/resource1.txt" to "This is a resource in www".encodeUtf8(),
      ),
    )
    tester.publishApp(app)

    val owner = tester.newClient()
    val computer = owner.createComputer()
    val installedApp = computer.installApp(app)

    assertThat(owner.call().callApp(url = installedApp.url.resolve("/resource1.txt")!!))
      .isEqualTo(
        Response(
          contentType = "text/plain".toMediaType(),
          body = ResponseBodySnapshot("This is a resource in www"),
        ),
      )

    val anotherUser = tester.newClient()
    assertFailsWith<NotFoundUserException> {
      anotherUser.call().callApp(url = installedApp.url.resolve("/resource1.txt")!!)
    }
  }

  @Test
  fun resourceInWwwPublic() = runTest {
    val app = tester.sampleApps.recipes.publishedApp.copy(
      resources = tester.sampleApps.recipes.publishedApp.resources + mapOf(
        "www-public/resource1.txt" to "This is a resource in www-public".encodeUtf8(),
      ),
    )
    tester.publishApp(app)

    val owner = tester.newClient()
    val computer = owner.createComputer()
    val installedApp = computer.installApp(app)

    assertThat(owner.call().callApp(url = installedApp.url.resolve("/resource1.txt")!!))
      .isEqualTo(
        Response(
          contentType = "text/plain".toMediaType(),
          body = ResponseBodySnapshot("This is a resource in www-public"),
        ),
      )

    val anotherUser = tester.newClient()
    assertThat(anotherUser.call().callApp(url = installedApp.url.resolve("/resource1.txt")!!))
      .isEqualTo(
        Response(
          contentType = "text/plain".toMediaType(),
          body = ResponseBodySnapshot("This is a resource in www-public"),
        ),
      )
  }

  @Test
  fun resourceInWwwPublicTakesPrecedenceOverResourceInWww() = runTest {
    val app = tester.sampleApps.recipes.publishedApp.copy(
      resources = tester.sampleApps.recipes.publishedApp.resources + mapOf(
        "www/resource1.txt" to "This is a resource in www".encodeUtf8(),
        "www-public/resource1.txt" to "This is a resource in www-public".encodeUtf8(),
      ),
    )
    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(app)

    assertThat(installedApp.call("/resource1.txt"))
      .isEqualTo(
        Response(
          contentType = "text/plain".toMediaType(),
          body = ResponseBodySnapshot("This is a resource in www-public"),
        ),
      )
  }

  @Test
  fun objectInWww() = runTest {
    val app = tester.sampleApps.recipes.publishedApp
    tester.publishApp(app)

    val owner = tester.newClient()
    val computer = owner.createComputer()
    val installedApp = computer.installApp(app)

    val recipesApp = installedApp.load() as RecipesApp
    recipesApp.platform.objectStore.put(
      PutObjectRequest(
        key = "www/resource1.txt",
        value = "This is an object in www".encodeUtf8(),
        contentType = "text/sample",
      ),
    )

    assertThat(owner.call().callApp(url = installedApp.url.resolve("/resource1.txt")!!))
      .isEqualTo(
        Response(
          contentType = "text/sample".toMediaType(),
          body = ResponseBodySnapshot("This is an object in www"),
        ),
      )

    val anotherUser = tester.newClient()
    assertFailsWith<NotFoundUserException> {
      anotherUser.call().callApp(url = installedApp.url.resolve("/resource1.txt")!!)
    }
  }

  @Test
  fun objectInWwwPublic() = runTest {
    val app = tester.sampleApps.recipes.publishedApp
    tester.publishApp(app)

    val owner = tester.newClient()
    val computer = owner.createComputer()
    val installedApp = computer.installApp(app)

    val recipesApp = installedApp.load() as RecipesApp
    recipesApp.platform.objectStore.put(
      PutObjectRequest(
        key = "www-public/resource1.txt",
        value = "This is an object in www-public".encodeUtf8(),
        contentType = "text/sample",
      ),
    )

    assertThat(owner.call().callApp(url = installedApp.url.resolve("/resource1.txt")!!))
      .isEqualTo(
        Response(
          contentType = "text/sample".toMediaType(),
          body = ResponseBodySnapshot("This is an object in www-public"),
        ),
      )

    val anotherUser = tester.newClient()
    assertThat(anotherUser.call().callApp(url = installedApp.url.resolve("/resource1.txt")!!))
      .isEqualTo(
        Response(
          contentType = "text/sample".toMediaType(),
          body = ResponseBodySnapshot("This is an object in www-public"),
        ),
      )
  }

  @Test
  fun objectInWwwPublicTakesPrecedenceOverObjectInWww() = runTest {
    val app = tester.sampleApps.recipes.publishedApp
    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(app)

    val recipesApp = installedApp.load() as RecipesApp
    recipesApp.platform.objectStore.put(
      PutObjectRequest(
        key = "www-public/resource1.txt",
        value = "This is an object in www-public".encodeUtf8(),
        contentType = "text/sample",
      ),
    )
    recipesApp.platform.objectStore.put(
      PutObjectRequest(
        key = "www/resource1.txt",
        value = "This is an object in www".encodeUtf8(),
        contentType = "text/standard",
      ),
    )

    assertThat(installedApp.call("/resource1.txt"))
      .isEqualTo(
        Response(
          contentType = "text/sample".toMediaType(),
          body = ResponseBodySnapshot("This is an object in www-public"),
        ),
      )
  }

  @Test
  fun objectTakesPrecedenceOverResource() = runTest {
    val app = tester.sampleApps.recipes.publishedApp.copy(
      resources = tester.sampleApps.recipes.publishedApp.resources + mapOf(
        "www/resource1.txt" to "This is a resource in www".encodeUtf8(),
      ),
    )
    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(app)

    val recipesApp = installedApp.load() as RecipesApp
    recipesApp.platform.objectStore.put(
      PutObjectRequest(
        key = "www/resource1.txt",
        value = "This is an object in www".encodeUtf8(),
        contentType = "text/simple",
      ),
    )

    assertThat(installedApp.call("/resource1.txt"))
      .isEqualTo(
        Response(
          contentType = "text/simple".toMediaType(),
          body = ResponseBodySnapshot("This is an object in www"),
        ),
      )
  }

  @Test
  fun indexHtmlIsServedToRequestsEndingInSlash() = runTest {
    val app = tester.sampleApps.recipes.publishedApp.copy(
      resources = tester.sampleApps.recipes.publishedApp.resources + mapOf(
        "www/index.html" to "This is the default resource".encodeUtf8(),
        "www/hello/index.html" to "This is the default resource for hello/".encodeUtf8(),
      ),
    )
    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(app)

    assertThat(installedApp.call("/"))
      .isEqualTo(
        Response(
          contentType = "text/html".toMediaType(),
          body = ResponseBodySnapshot("This is the default resource"),
        ),
      )

    assertThat(installedApp.call("/hello/"))
      .isEqualTo(
        Response(
          contentType = "text/html".toMediaType(),
          body = ResponseBodySnapshot("This is the default resource for hello/"),
        ),
      )
  }

  @Test
  fun indexHtmlIsNotServedToRequestsNotEndingInSlash() = runTest {
    val app = tester.sampleApps.recipes.publishedApp.copy(
      resources = tester.sampleApps.recipes.publishedApp.resources + mapOf(
        "www/hello/index.html" to "This is the default resource for hello/".encodeUtf8(),
      ),
    )
    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(app)

    assertFailsWith<NotFoundUserException> {
      installedApp.call("/hello")
    }
  }
}

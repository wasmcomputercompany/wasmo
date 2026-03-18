package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import wasmo.objectstore.GetObjectRequest
import wasmo.objectstore.GetObjectResponse
import wasmo.objectstore.PutObjectRequest

class InstalledAppObjectStoreTest {
  private val casino = "casino".encodeUtf8()
  private val subway = "subway".encodeUtf8()

  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun readAndWriteObjects() = runTest {
    tester.publishApp(RecipesApp.PublishedApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(RecipesApp.PublishedApp)

    val recipesApp = installedApp.load() as RecipesApp
    val store = recipesApp.platform.objectStore

    store.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    store.put(PutObjectRequest("shows/pokerface/s1e2.mp4", subway))

    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e1.mp4")))
      .isEqualTo(GetObjectResponse(casino))
    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e2.mp4")))
      .isEqualTo(GetObjectResponse(subway))
  }
}

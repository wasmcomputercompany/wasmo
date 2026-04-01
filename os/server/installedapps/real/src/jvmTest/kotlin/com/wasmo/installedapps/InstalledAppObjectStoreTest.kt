package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.testing.apps.MusicApp
import com.wasmo.testing.apps.SnakeApp
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
    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.getApp(tester.sampleApps.snake.publishedApp)

    val wasmoApp = installedApp.load() as SnakeApp
    val store = wasmoApp.platform.objectStore

    store.put(PutObjectRequest("shows/pokerface/s1e1.mp4", casino))
    store.put(PutObjectRequest("shows/pokerface/s1e2.mp4", subway))

    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e1.mp4")))
      .isEqualTo(GetObjectResponse(casino))
    assertThat(store.get(GetObjectRequest("shows/pokerface/s1e2.mp4")))
      .isEqualTo(GetObjectResponse(subway))
  }

  @Test
  fun independentAppsHaveIndependentStores() = runTest {
    val client = tester.newClient()
    val computer = client.createComputer()

    val installedApp1 = computer.getApp(tester.sampleApps.snake.publishedApp)
    val wasmoApp1 = installedApp1.load() as SnakeApp
    val store1 = wasmoApp1.platform.objectStore

    val installedApp2 = computer.getApp(tester.sampleApps.music.publishedApp)
    val wasmoApp2 = installedApp2.load() as MusicApp
    val store2 = wasmoApp2.platform.objectStore

    store1.put(PutObjectRequest("data.txt", "snake high score".encodeUtf8()))
    store2.put(PutObjectRequest("data.txt", "music playlist".encodeUtf8()))

    assertThat(store1.get(GetObjectRequest("data.txt")))
      .isEqualTo(GetObjectResponse("snake high score".encodeUtf8()))
    assertThat(store2.get(GetObjectRequest("data.txt")))
      .isEqualTo(GetObjectResponse("music playlist".encodeUtf8()))
  }

  @Test
  fun independentComputersHaveIndependentStores() = runTest {
    val client = tester.newClient()
    val computer1 = client.createComputer()
    val computer2 = client.createComputer()

    val installedApp1 = computer1.getApp(tester.sampleApps.snake.publishedApp)
    val wasmoApp1 = installedApp1.load() as SnakeApp
    val store1 = wasmoApp1.platform.objectStore

    val installedApp2 = computer2.getApp(tester.sampleApps.snake.publishedApp)
    val wasmoApp2 = installedApp2.load() as SnakeApp
    val store2 = wasmoApp2.platform.objectStore

    store1.put(PutObjectRequest("data.txt", "snake high score on computer 1 is 500".encodeUtf8()))
    store2.put(PutObjectRequest("data.txt", "snake high score on computer 2 is 900".encodeUtf8()))

    assertThat(store1.get(GetObjectRequest("data.txt")))
      .isEqualTo(GetObjectResponse("snake high score on computer 1 is 500".encodeUtf8()))
    assertThat(store2.get(GetObjectRequest("data.txt")))
      .isEqualTo(GetObjectResponse("snake high score on computer 2 is 900".encodeUtf8()))
  }
}

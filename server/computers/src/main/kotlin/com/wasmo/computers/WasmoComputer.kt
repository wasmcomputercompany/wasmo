package com.wasmo.computers

import com.wasmo.api.AppManifest
import com.wasmo.objectstore.ObjectStore
import okhttp3.HttpUrl

interface WasmoComputer {
  val url: HttpUrl
  val objectStore: ObjectStore
  val appLoader: AppLoader

  suspend fun installApp(manifest: AppManifest)
}

interface ComputerStore {
  fun create(slug: String): WasmoComputer
  fun get(slug: String): WasmoComputer
}

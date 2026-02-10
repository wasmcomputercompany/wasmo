package com.wasmo.computers

import com.wasmo.ObjectStore
import com.wasmo.api.AppManifest
import okhttp3.HttpUrl

interface WasmoComputer {
  val url: HttpUrl
  val objectStore: ObjectStore

  suspend fun installApp(manifest: AppManifest)
}

interface ComputerStore {
  fun create(slug: String): WasmoComputer
  fun get(slug: String): WasmoComputer
}

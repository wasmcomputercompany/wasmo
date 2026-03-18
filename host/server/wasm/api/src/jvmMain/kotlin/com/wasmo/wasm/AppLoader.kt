package com.wasmo.wasm

import com.wasmo.packaging.AppManifest
import wasmo.app.Platform
import wasmo.app.WasmoApp

interface AppLoader {
  suspend fun load(
    platform: Platform,
    manifest: AppManifest,
  ): WasmoApp?
}

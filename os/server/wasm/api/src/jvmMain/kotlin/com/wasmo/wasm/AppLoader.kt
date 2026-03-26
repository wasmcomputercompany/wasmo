package com.wasmo.wasm

import com.wasmo.identifiers.AppSlug
import wasmo.app.Platform
import wasmo.app.WasmoApp

interface AppLoader {
  suspend fun load(
    platform: Platform,
    appSlug: AppSlug,
  ): WasmoApp?
}

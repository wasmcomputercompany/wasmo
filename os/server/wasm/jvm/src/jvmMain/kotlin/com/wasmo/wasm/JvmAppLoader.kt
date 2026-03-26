package com.wasmo.wasm

import com.wasmo.identifiers.AppSlug
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.app.Platform
import wasmo.app.WasmoApp

/**
 * This app loader requires the `WamsoApp.Factory` instance is callable in-process.
 */
@Inject
@SingleIn(AppScope::class)
class JvmAppLoader(
  private val factories: Map<AppSlug, WasmoApp.Factory>,
) : AppLoader {
  override suspend fun load(
    platform: Platform,
    appSlug: AppSlug,
  ): WasmoApp? {
    return factories[appSlug]?.create(platform)
  }
}

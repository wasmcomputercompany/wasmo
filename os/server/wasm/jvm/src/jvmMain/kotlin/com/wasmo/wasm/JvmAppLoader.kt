package com.wasmo.wasm

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.app.Platform
import wasmo.app.WasmoApp

/**
 * This app loader requires the `WamsoApp.Factory` instance is callable in-process.
 */
@Inject
@SingleIn(OsScope::class)
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

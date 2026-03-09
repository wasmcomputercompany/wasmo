package com.wasmo.computers

import com.wasmo.identifiers.AppSlug
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Tracks how we identify resources in our [ObjectStore].
 */
@Inject
@SingleIn(AppScope::class)
class ObjectStoreKeyFactory {
  fun wasm(appSlug: AppSlug, appVersion: Long) = "apps/${appSlug.value}/v$appVersion/app.wasm"
}

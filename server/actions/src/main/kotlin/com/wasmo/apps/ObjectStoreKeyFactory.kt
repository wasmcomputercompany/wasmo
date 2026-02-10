package com.wasmo.apps

/**
 * Tracks how we identify resources in our [ObjectStore].
 */
class ObjectStoreKeyFactory {
  fun wasm(appSlug: String, appVersion: Long) = "apps/$appSlug/v$appVersion/app.wasm"
}

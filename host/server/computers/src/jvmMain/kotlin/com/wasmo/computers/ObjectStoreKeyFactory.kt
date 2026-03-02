package com.wasmo.computers

import com.wasmo.api.AppSlug

/**
 * Tracks how we identify resources in our [ObjectStore].
 */
class ObjectStoreKeyFactory {
  fun wasm(appSlug: AppSlug, appVersion: Long) = "apps/${appSlug.value}/v$appVersion/app.wasm"
}

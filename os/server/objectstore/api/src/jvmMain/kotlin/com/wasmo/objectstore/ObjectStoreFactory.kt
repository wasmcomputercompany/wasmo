package com.wasmo.objectstore

import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.objectstore.ObjectStore

@Inject
@SingleIn(OsScope::class)
class ObjectStoreFactory(
  private val connectors: Set<ObjectStoreConnector>,
) {
  fun open(address: ObjectStoreAddress): ObjectStore {
    return connectors.firstNotNullOfOrNull { it.tryConnect(address) }
      ?: error("no connector for $address")
  }
}

package com.wasmo.objectstore.filesystem

import com.wasmo.identifiers.OsScope
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreConnector
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import wasmo.objectstore.ObjectStore

@BindingContainer
object FileSystemObjectStoreBindings {
  @Provides
  @SingleIn(OsScope::class)
  @IntoSet
  internal fun provideObjectStoreConnector(): ObjectStoreConnector = object : ObjectStoreConnector {
    override fun tryConnect(address: ObjectStoreAddress): ObjectStore? {
      if (address !is FileSystemObjectStoreAddress) return null
      return FileSystemObjectStore(address.path)
    }
  }
}

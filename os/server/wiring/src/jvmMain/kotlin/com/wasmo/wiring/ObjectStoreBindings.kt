package com.wasmo.wiring

import com.wasmo.identifiers.ForOs
import com.wasmo.identifiers.OsScope
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreFactory
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import wasmo.objectstore.ObjectStore

@BindingContainer
abstract class ObjectStoreBindings {
  companion object {
    @Provides
    @ForOs
    @SingleIn(OsScope::class)
    fun provideObjectStore(
      address: ObjectStoreAddress,
      objectStoreFactory: ObjectStoreFactory,
    ): ObjectStore = objectStoreFactory.open(address)
  }
}

package com.wasmo.objectstore

import wasmo.objectstore.FakeObjectStore
import wasmo.objectstore.ObjectStore

class FakeObjectStoreTest : AbstractObjectStoreTest() {
  override val store: ObjectStore = FakeObjectStore()
}

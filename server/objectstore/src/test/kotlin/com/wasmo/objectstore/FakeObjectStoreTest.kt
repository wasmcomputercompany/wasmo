package com.wasmo.objectstore

class FakeObjectStoreTest : AbstractObjectStoreTest() {
  override val store: ObjectStore = FakeObjectStore()
}

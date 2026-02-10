package com.wasmo

class FakeObjectStoreTest : AbstractObjectStoreTest() {
  override val store: ObjectStore = FakeObjectStore()
}

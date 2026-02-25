package com.wasmo.objectstore.filesystem

import com.wasmo.objectstore.AbstractObjectStoreTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

class FileSystemObjectStoreTest : AbstractObjectStoreTest() {
  override val store = FileSystemObjectStore(
    fileSystem = FakeFileSystem(),
    path = "/objects".toPath(),
  )
}

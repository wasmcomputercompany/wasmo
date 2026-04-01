package com.wasmo.objectstore.filesystem

import com.wasmo.objectstore.AbstractObjectStoreTest
import com.wasmo.support.tokens.newToken
import kotlin.test.AfterTest
import okio.FileSystem

class FileSystemObjectStoreTest : AbstractObjectStoreTest() {
  private val temp = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / newToken()

  override val store = FileSystemObjectStore(
    path = temp,
  )

  @AfterTest
  fun afterTest() {
    FileSystem.SYSTEM.deleteRecursively(temp)
  }
}

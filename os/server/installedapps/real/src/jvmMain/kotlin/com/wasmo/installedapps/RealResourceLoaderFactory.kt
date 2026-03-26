package com.wasmo.installedapps

import com.wasmo.identifiers.WasmoFileAddress
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(InstalledAppScope::class)
class RealResourceLoaderFactory(
  private val wasmoFileAddress: WasmoFileAddress,
  private val fileSystemResourceLoaderFactory: FileSystemResourceLoader.Factory,
  private val objectStoreResourceLoader: ObjectStoreResourceLoader,
) : ResourceLoader.Factory {
  override fun create(
  ): ResourceLoader {
    return when (wasmoFileAddress) {
      is WasmoFileAddress.FileSystem -> fileSystemResourceLoaderFactory.create(wasmoFileAddress)
      is WasmoFileAddress.Http -> objectStoreResourceLoader
    }
  }
}

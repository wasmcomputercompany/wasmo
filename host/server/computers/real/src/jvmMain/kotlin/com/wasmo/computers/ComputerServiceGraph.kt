package com.wasmo.computers

import com.wasmo.db.Computer
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ForHost
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Qualifier
import dev.zacsweers.metro.SingleIn
import wasmo.downloader.Downloader
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.ScopedObjectStore

@GraphExtension(
  scope = ComputerScope::class,
)
interface ComputerServiceGraph {
  val service: ComputerService

  @Provides
  @ForComputer
  @SingleIn(ComputerScope::class)
  fun provideObjectStore(
    slug: ComputerSlug,
    @ForHost objectStore: ObjectStore,
  ): ObjectStore = ScopedObjectStore(
    delegate = objectStore,
    prefix = "$slug/",
  )

  @Provides
  @SingleIn(ComputerScope::class)
  fun provideComputerSlug(
    computer: Computer,
  ): ComputerSlug = computer.slug

  @Provides
  @SingleIn(ComputerScope::class)
  fun provideComputerId(
    computer: Computer,
  ): ComputerId = computer.id

  @Binds
  fun bindDownloader(real: RealDownloader): Downloader

  @Binds
  fun bindComputerService(real: RealComputerService): ComputerService

  @Binds
  fun bindManifestLoader(real: RealManifestLoader): ManifestLoader

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides computer: Computer,
    ): ComputerServiceGraph
  }
}

abstract class ComputerScope private constructor()

@Qualifier
annotation class ForComputer

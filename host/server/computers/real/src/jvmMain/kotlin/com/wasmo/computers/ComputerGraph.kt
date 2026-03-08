package com.wasmo.computers

import com.wasmo.api.ComputerSlug
import com.wasmo.identifiers.ComputerId
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
interface ComputerGraph {
  val computer: WasmoComputer

  @Provides
  @ForComputer
  @SingleIn(ComputerScope::class)
  fun provideObjectStore(
    slug: ComputerSlug,
    @ForHost objectStore: ObjectStore,
  ): ObjectStore = ScopedObjectStore(
    delegate = objectStore,
    prefix = "${slug.value}/",
  )

  @Binds
  fun bindDownloader(real: RealDownloader): Downloader

  @Binds
  fun bindComputer(real: RealWasmoComputer): WasmoComputer

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides computerId: ComputerId,
      @Provides slug: ComputerSlug,
    ): ComputerGraph
  }
}

abstract class ComputerScope private constructor()

@Qualifier
annotation class ForComputer

package com.wasmo.computers

import com.wasmo.app.db.Computer
import com.wasmo.downloader.RealDownloader
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerScope
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ForComputer
import com.wasmo.identifiers.ForOs
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import wasmo.downloader.Downloader
import wasmo.http.HttpService
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
    @ForOs objectStore: ObjectStore,
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

  @Provides
  @SingleIn(ComputerScope::class)
  fun provideRealDownloader(
    httpService: HttpService,
    @ForComputer objectStore: ObjectStore,
  ): RealDownloader = RealDownloader(
    httpService = httpService,
    objectStore = objectStore,
  )

  @Binds
  fun bindDownloader(real: RealDownloader): Downloader

  @Binds
  fun bindComputerService(real: RealComputerService): ComputerService

  @Binds
  fun bindInstallerFactory(real: RealInstallerFactory): ResourceInstaller.Factory

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides computer: Computer,
    ): ComputerServiceGraph
  }
}

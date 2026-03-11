package com.wasmo.installedapps

import com.wasmo.db.InstalledApp
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ForHost
import com.wasmo.packaging.AppManifest
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Qualifier
import dev.zacsweers.metro.SingleIn
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.ScopedObjectStore

@GraphExtension(
  scope = InstalledAppScope::class,
)
interface InstalledAppServiceGraph {
  val service: InstalledAppService

  @Provides
  @ForInstalledApp
  @SingleIn(InstalledAppScope::class)
  fun provideObjectStore(
    computerSlug: ComputerSlug,
    appSlug: AppSlug,
    @ForHost objectStore: ObjectStore,
  ): ObjectStore = ScopedObjectStore(
    delegate = objectStore,
    prefix = "$computerSlug/$appSlug/",
  )

  @Provides
  @SingleIn(InstalledAppScope::class)
  fun provideAppSlug(
    installedApp: InstalledApp,
  ): AppSlug = installedApp.slug

  @Provides
  @SingleIn(InstalledAppScope::class)
  fun provideAppManifest(
    installedApp: InstalledApp,
  ): AppManifest = installedApp.manifest_data

  @Binds
  fun bindInstalledAppService(real: RealInstalledAppService): InstalledAppService

  @Binds
  fun bindInstalledAppHttpService(real: RealInstalledAppHttpService): InstalledAppHttpService

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides computerSlug: ComputerSlug,
      @Provides installedApp: InstalledApp,
    ): InstalledAppServiceGraph
  }
}

abstract class InstalledAppScope private constructor()

@Qualifier
annotation class ForInstalledApp

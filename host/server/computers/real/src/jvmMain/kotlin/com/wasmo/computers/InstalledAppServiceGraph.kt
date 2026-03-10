package com.wasmo.computers

import com.wasmo.db.InstalledApp
import com.wasmo.identifiers.AppSlug
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
    appSlug: AppSlug,
    @ForComputer objectStore: ObjectStore,
  ): ObjectStore = ScopedObjectStore(
    delegate = objectStore,
    prefix = "$appSlug/",
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

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides installedApp: InstalledApp,
    ): InstalledAppServiceGraph
  }
}

abstract class InstalledAppScope private constructor()

@Qualifier
annotation class ForInstalledApp

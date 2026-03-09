package com.wasmo.testing.installedapp

import com.wasmo.computers.InstalledAppScope
import com.wasmo.identifiers.AppSlug
import com.wasmo.testing.apps.PublishedApp
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@GraphExtension(
  scope = InstalledAppScope::class,
)
interface InstalledAppTesterGraph {
  val installedAppTester: InstalledAppTester

  @Provides
  @SingleIn(InstalledAppScope::class)
  fun provideAppSlug(publishedApp: PublishedApp): AppSlug = AppSlug(publishedApp.manifest.slug)

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides publishedApp: PublishedApp,
    ): InstalledAppTesterGraph
  }
}

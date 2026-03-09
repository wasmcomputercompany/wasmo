package com.wasmo.testing.installedapp

import com.wasmo.computers.InstalledAppScope
import com.wasmo.identifiers.AppSlug
import com.wasmo.testing.apps.TestApp
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
  fun provideAppSlug(testApp: TestApp): AppSlug = testApp.slug

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides testApp: TestApp,
    ): InstalledAppTesterGraph
  }
}

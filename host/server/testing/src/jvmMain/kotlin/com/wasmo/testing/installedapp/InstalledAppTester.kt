package com.wasmo.testing.installedapp

import com.wasmo.computers.ComputerUrlFactory
import com.wasmo.computers.InstalledAppScope
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.testing.apps.PublishedApp
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okhttp3.HttpUrl

/**
 * Tests an app installed on a specific computer.
 */
@Inject
@SingleIn(InstalledAppScope::class)
class InstalledAppTester(
  val publishedApp: PublishedApp,
  val computerUrlFactory: ComputerUrlFactory,
  val computerSlug: ComputerSlug,
  val slug: AppSlug,
) {
  val url: HttpUrl
    get() = computerUrlFactory.appUrl(slug)
  val iconUrl: HttpUrl
    get() = url.resolve("/maskable-icon.svg")!!
}

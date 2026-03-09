package com.wasmo.testing.installedapp

import com.wasmo.computers.InstalledAppScope
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.testing.apps.TestApp
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Tests an app installed on a specific computer.
 */
@Inject
@SingleIn(InstalledAppScope::class)
class InstalledAppTester(
  val testApp: TestApp,
  val computerSlug: ComputerSlug,
  val slug: AppSlug,
)

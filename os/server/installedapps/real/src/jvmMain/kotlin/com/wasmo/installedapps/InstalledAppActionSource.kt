package com.wasmo.installedapps

import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
object InstalledAppActionSource {
  @Provides
  @ElementsIntoSet
  @SingleIn(OsScope::class)
  fun provideActionRegistrations(
    installedAppActionsFactory: InstalledAppActions.Factory,
    hostnamePatterns: HostnamePatterns,
  ): List<ActionRegistration> = listOf(
    ActionRegistration.Http(
      HttpRequestPattern(host = hostnamePatterns.appRegex),
    ) { userAgent, _, request ->
      val action = installedAppActionsFactory.create(userAgent).callAppAction
      action.call(request)
    },
  )
}

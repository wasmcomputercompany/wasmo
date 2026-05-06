package com.wasmo.website

import com.wasmo.framework.ActionRegistration
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
abstract class WebsiteBindings {
  @Binds
  abstract fun bindServerOsHtmlFactory(real: RealServerOsHtml.Factory): ServerOsHtml.Factory

  companion object {
    @Provides
    @ElementsIntoSet
    @SingleIn(OsScope::class)
    fun provideActionRegistrations(
      hostnamePatterns: HostnamePatterns,
    ): List<ActionRegistration> = listOf(
      pageRegistration(
        host = hostnamePatterns.computerRegex,
        path = "/",
      ),

      ActionRegistration.StaticResources(
        host = hostnamePatterns.computerRegex,
        path = "/",
        basePackage = "static",
      ),

      pageRegistration(
        host = hostnamePatterns.osHostname,
        path = "/",
      ),

      pageRegistration(
        host = hostnamePatterns.osHostname,
        path = "/build-yours",
      ),

      pageRegistration(
        host = hostnamePatterns.osHostname,
        path = "/invite/{code}",
      ),

      pageRegistration(
        host = hostnamePatterns.osHostname,
        path = "/sign-up",
      ),

      ActionRegistration.StaticResources(
        host = hostnamePatterns.osHostname,
        path = "/",
        basePackage = "static",
      ),

      ActionRegistration.Http(
        action = FallbackHttpAction::class,
      ),
    )

    private fun pageRegistration(
      host: Regex,
      path: String,
    ) = ActionRegistration.Http(
      host = host,
      path = path,
      method = "GET",
      action = OsPage::class,
    )
  }
}

package com.wasmo.website

import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
abstract class WebsiteBindings {
  companion object {
    @Provides
    @ElementsIntoSet
    @SingleIn(OsScope::class)
    fun provideActionRegistrations(
      hostnamePatterns: HostnamePatterns,
    ): List<ActionRegistration> = listOf(
      ActionRegistration.Http(
        HttpRequestPattern(
          host = hostnamePatterns.computerRegex,
          path = "/",
        ),
        action = OsPage::class,
      ),

      ActionRegistration.StaticResources(
        host = hostnamePatterns.computerRegex,
        pathPrefix = "/",
        basePackage = "static",
      ),

      pageRegistration(
        hostnamePatterns = hostnamePatterns,
        path = "/",
      ),

      pageRegistration(
        hostnamePatterns = hostnamePatterns,
        path = "/build-yours",
      ),

      pageRegistration(
        hostnamePatterns = hostnamePatterns,
        path = "/invite/{code}",
      ),

      pageRegistration(
        hostnamePatterns = hostnamePatterns,
        path = "/sign-up",
      ),

      ActionRegistration.StaticResources(
        host = Regex(Regex.escape(hostnamePatterns.osHostname)),
        pathPrefix = "/",
        basePackage = "static",
      ),

      ActionRegistration.Http(
        HttpRequestPattern.AllRequests,
        FallbackHttpAction::class,
      ),
    )

    private fun pageRegistration(
      hostnamePatterns: HostnamePatterns,
      path: String,
    ) = ActionRegistration.Http(
      HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = path,
        method = "GET",
      ),
      action = OsPage::class,
    )
  }
}

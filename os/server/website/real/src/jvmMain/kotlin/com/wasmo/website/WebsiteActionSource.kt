package com.wasmo.website

import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.asResponse
import com.wasmo.framework.decodeUrl
import com.wasmo.framework.redirect
import com.wasmo.framework.toHttpUrl
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
object WebsiteActionSource {
  @Provides
  @ElementsIntoSet
  @SingleIn(OsScope::class)
  fun provideActionRegistrations(
    websiteActionsFactory: WebsiteActions.Factory,
    hostnamePatterns: HostnamePatterns,
    deployment: Deployment,
  ): List<ActionRegistration> = listOf(
    ActionRegistration.Http(
      HttpRequestPattern(
        host = hostnamePatterns.computerRegex,
        path = "/",
      ),
    ) { userAgent, url, _ ->
      val action = websiteActionsFactory.create(userAgent).osPage
      action.get(url).response
    },

    ActionRegistration.StaticResources(
      host = hostnamePatterns.computerRegex,
      pathPrefix = "/",
      basePackage = "static",
    ),

    pageRegistration(
      websiteActionsFactory = websiteActionsFactory,
      hostnamePatterns = hostnamePatterns,
      path = "/",
    ),

    pageRegistration(
      websiteActionsFactory = websiteActionsFactory,
      hostnamePatterns = hostnamePatterns,
      path = "/build-yours",
    ),

    pageRegistration(
      websiteActionsFactory = websiteActionsFactory,
      hostnamePatterns = hostnamePatterns,
      path = "/invite/{code}",
    ),

    pageRegistration(
      websiteActionsFactory = websiteActionsFactory,
      hostnamePatterns = hostnamePatterns,
      path = "/sign-up",
    ),

    ActionRegistration.StaticResources(
      host = Regex(Regex.escape(hostnamePatterns.osHostname)),
      pathPrefix = "/",
      basePackage = "static",
    ),

    fallbackActionRegistration(deployment),
  )

  private fun pageRegistration(
    websiteActionsFactory: WebsiteActions.Factory,
    hostnamePatterns: HostnamePatterns,
    path: String,
  ) = ActionRegistration.Http(
    HttpRequestPattern(
      host = hostnamePatterns.osHostname,
      path = path,
      method = "GET",
    ),
  ) { userAgent, url, _ ->
    val callGraph = websiteActionsFactory.create(userAgent)
    callGraph.osPage.get(url).response
  }

  private fun fallbackActionRegistration(
    deployment: Deployment,
  ): ActionRegistration.Http {
    val rootUrl = deployment.baseUrl.toString().decodeUrl()
    return ActionRegistration.Http(
      HttpRequestPattern.AllRequests,
    ) { _, url, _ ->
      when {
        url.topPrivateDomain != rootUrl.topPrivateDomain || url.subdomain != rootUrl.subdomain ->
          redirect(rootUrl.toHttpUrl())

        else -> NotFoundUserException().asResponse()
      }
    }
  }
}

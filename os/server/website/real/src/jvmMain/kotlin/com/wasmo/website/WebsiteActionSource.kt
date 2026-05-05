package com.wasmo.website

import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.ActionSource
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.asResponse
import com.wasmo.framework.decodeUrl
import com.wasmo.framework.redirect
import com.wasmo.framework.toHttpUrl
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class WebsiteActionSource(
  private val websiteActionsFactory: WebsiteActions.Factory,
  private val hostnamePatterns: HostnamePatterns,
  deployment: Deployment,
) : ActionSource {
  private val rootUrl = deployment.baseUrl.toString().decodeUrl()

  override val order: Int
    get() = 100

  context(binder: ActionSource.Binder)
  override fun bindActions() {
    binder.register(
      ActionRegistration.Http(
        HttpRequestPattern(
          host = hostnamePatterns.computerRegex,
          path = "/",
        ),
      ) { userAgent, url, _ ->
        val action = websiteActionsFactory.create(userAgent).osPage
        action.get(url).response
      },
    )

    // TODO: change the web app to always fetch static resources from the root URL.
    binder.register(
      ActionRegistration.StaticResources(
        host = hostnamePatterns.computerRegex,
        pathPrefix = "/",
        basePackage = "static",
      ),
    )

    val osPagePaths = listOf(
      "/",
      "/build-yours",
      "/invite/{code}",
      "/sign-up",
    )
    for (path in osPagePaths) {
      binder.register(
        ActionRegistration.Http(
          HttpRequestPattern(
            host = hostnamePatterns.osHostname,
            path = path,
            method = "GET",
          ),
        ) { userAgent, url, _ ->
          val callGraph = websiteActionsFactory.create(userAgent)
          callGraph.osPage.get(url).response
        },
      )
    }

    binder.register(
      ActionRegistration.StaticResources(
        host = Regex(Regex.escape(hostnamePatterns.osHostname)),
        pathPrefix = "/",
        basePackage = "static",
      ),
    )

    binder.register(
      ActionRegistration.Http(
        HttpRequestPattern.AllRequests,
      ) { _, url, _ ->
        when {
          url.topPrivateDomain != rootUrl.topPrivateDomain || url.subdomain != rootUrl.subdomain ->
            redirect(rootUrl.toHttpUrl())

          else -> NotFoundUserException().asResponse()
        }
      },
    )
  }
}

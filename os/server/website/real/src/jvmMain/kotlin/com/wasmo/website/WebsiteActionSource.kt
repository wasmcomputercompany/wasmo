package com.wasmo.website

import com.wasmo.framework.ActionSource
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
    binder.host(hostnamePatterns.computerRegex) {
      route("/") {
        httpAction { userAgent, url, _ ->
          val action = websiteActionsFactory.create(userAgent).osPage
          action.get(url).response
        }
      }

      // TODO: change the web app to always fetch static resources from the root URL.
      staticResources("/", "static")
    }

    binder.host(hostnamePatterns.osHostname) {
      val osPagePaths = listOf(
        "/",
        "/build-yours",
        "/invite/{code}",
        "/sign-up",
      )
      for (path in osPagePaths) {
        route(path, "GET") {
          httpAction { userAgent, url, _ ->
            val callGraph = websiteActionsFactory.create(userAgent)
            callGraph.osPage.get(url).response
          }
        }
      }

      staticResources("/", "static")
    }

    binder.routeAll {
      httpAction { _, url, _ ->
        when {
          url.topPrivateDomain != rootUrl.topPrivateDomain || url.subdomain != rootUrl.subdomain ->
            redirect(rootUrl.toHttpUrl())

          else -> NotFoundUserException().asResponse()
        }
      }
    }
  }
}

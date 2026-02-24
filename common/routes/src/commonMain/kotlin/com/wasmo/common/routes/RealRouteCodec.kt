package com.wasmo.common.routes

import com.wasmo.api.routes.AdminRoute
import com.wasmo.api.routes.AfterCheckoutRoute
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.ComputersRoute
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.NotFoundRoute
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.routes.TeaserRoute
import com.wasmo.api.routes.Url

class RealRouteCodec(
  private val context: RoutingContext,
) : RouteCodec {

  override fun decode(url: Url): Route {
    val subdomain = url.subdomain
    if (subdomain != null) {
      return ComputerHomeRoute(subdomain)
    }

    if (url.path.size == 1) {
      return when (url.path.single()) {
        "" -> {
          when {
            context.hasComputers -> ComputersRoute
            context.hasInvite -> BuildYoursRoute
            else -> TeaserRoute
          }
        }

        "admin" -> AdminRoute
        "build-yours" -> BuildYoursRoute
        "computers" -> ComputersRoute
        "teaser" -> TeaserRoute
        else -> NotFoundRoute
      }
    }

    if (url.path.size == 2) {
      return when (url.path[0]) {
        "invite" -> InviteRoute(url.path[1])
        "after-checkout" -> AfterCheckoutRoute(url.path[1])
        else -> NotFoundRoute
      }
    }

    return NotFoundRoute
  }

  override fun encode(route: Route): Url {
    return when (route) {
      AdminRoute -> context.root.copy(
        path = listOf("admin"),
      )

      is AfterCheckoutRoute -> context.root.copy(
        path = listOf("after-checkout", route.checkoutSessionId),
      )

      BuildYoursRoute -> when {
        context.hasInvite && !context.hasComputers -> context.root
        else -> context.root.copy(path = listOf("build-yours"))
      }

      is ComputerHomeRoute -> context.root.copy(
        subdomain = route.name,
      )

      ComputersRoute -> when {
        context.hasComputers -> context.root
        else -> context.root.copy(path = listOf("computers"))
      }

      is InviteRoute -> context.root.copy(
        path = listOf("invite", route.code),
      )

      TeaserRoute -> when {
        !context.hasComputers && !context.hasInvite -> context.root
        else -> context.root.copy(path = listOf("teaser"))
      }

      NotFoundRoute -> context.root.copy(path = listOf("not-found"))
    }
  }
}

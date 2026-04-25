package com.wasmo.common.routes

import com.wasmo.api.routes.AdminRoute
import com.wasmo.api.routes.AfterCheckoutRoute
import com.wasmo.api.routes.AppRoute
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.HomeRoute
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.NotFoundRoute
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.routes.SignUpRoute
import com.wasmo.api.routes.Url
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class RealRouteCodec(
  @Assisted private val routingContext: RoutingContext,
) : RouteCodec {
  override fun decode(url: Url): Route {
    val subdomain = url.subdomain
    if (subdomain != null) {
      return when (val dash = subdomain.indexOf('-')) {
        -1 -> ComputerHomeRoute(ComputerSlug(subdomain))
        else -> AppRoute(
          appSlug = AppSlug(subdomain.take(dash)),
          computerSlug = ComputerSlug(subdomain.substring(dash + 1)),
          path = url.path,
          query = url.query,
        )
      }
    }

    if (url.path.size == 1) {
      return when (url.path.single()) {
        "" -> HomeRoute
        "admin" -> AdminRoute
        "build-yours" -> BuildYoursRoute
        "sign-up" -> SignUpRoute
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
      AdminRoute -> routingContext.root.copy(
        path = listOf("admin"),
      )

      is AfterCheckoutRoute -> routingContext.root.copy(
        path = listOf("after-checkout", route.checkoutSessionId),
      )

      is AppRoute -> routingContext.root.copy(
        subdomain = "${route.appSlug.value}-${route.computerSlug.value}",
        path = route.path,
        query = route.query,
      )

      BuildYoursRoute -> routingContext.root.copy(path = listOf("build-yours"))

      is ComputerHomeRoute -> routingContext.root.copy(
        subdomain = route.slug.value,
      )

      is InviteRoute -> routingContext.root.copy(
        path = listOf("invite", route.code),
      )

      HomeRoute -> routingContext.root

      NotFoundRoute -> routingContext.root.copy(path = listOf("not-found"))

      SignUpRoute -> routingContext.root.copy(
        path = listOf("sign-up"),
      )
    }
  }

  @AssistedFactory
  interface Factory : RouteCodec.Factory {
    override fun create(routingContext: RoutingContext): RealRouteCodec
  }
}

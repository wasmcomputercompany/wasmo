package com.wasmo.common.routes

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.api.routes.AdminRoute
import com.wasmo.api.routes.AfterCheckoutRoute
import com.wasmo.api.routes.AppRoute
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.HomeRoute
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.NotFoundRoute
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.routes.SignOutRoute
import com.wasmo.framework.Url
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import kotlin.test.Test

class RealRouteCodecTest {
  private val root = Url(scheme = "https", topPrivateDomain = "wasmo.com")
  private val routeCodec = RealRouteCodec(
    routingContext = RoutingContext(
      rootUrl = "https://wasmo.com/",
    ),
  )

  @Test
  fun encode() {
    val computerSlug = ComputerSlug("jessewilson99")
    val appSlug = AppSlug("recipes")

    assertThat(routeCodec.encode(ComputerHomeRoute(computerSlug)))
      .isEqualTo(root.copy(subdomain = "jessewilson99"))
    assertThat(
      routeCodec.encode(
        AppRoute(computerSlug, appSlug, listOf("breakfast")),
      ),
    ).isEqualTo(
      root.copy(
        subdomain = "recipes-jessewilson99",
        path = listOf("breakfast"),
      ),
    )
    assertThat(routeCodec.encode(InviteRoute("1234")))
      .isEqualTo(root.copy(path = listOf("invite", "1234")))
    assertThat(routeCodec.encode(AdminRoute))
      .isEqualTo(root.copy(path = listOf("admin")))
    assertThat(routeCodec.encode(HomeRoute))
      .isEqualTo(root)
    assertThat(routeCodec.encode(BuildYoursRoute))
      .isEqualTo(root.copy(path = listOf("build-yours")))
    assertThat(routeCodec.encode(AfterCheckoutRoute("5678")))
      .isEqualTo(root.copy(path = listOf("after-checkout", "5678")))
    assertThat(routeCodec.encode(NotFoundRoute))
      .isEqualTo(root.copy(path = listOf("not-found")))
    assertThat(routeCodec.encode(SignOutRoute))
      .isEqualTo(root.copy(path = listOf("sign-out")))
  }

  @Test
  fun decode() {
    val computerSlug = ComputerSlug("jessewilson99")
    val appSlug = AppSlug("recipes")

    assertThat(routeCodec.decode(root.copy(subdomain = "jessewilson99")))
      .isEqualTo(ComputerHomeRoute(computerSlug))
    assertThat(
      routeCodec.decode(
        root.copy(subdomain = "recipes-jessewilson99", path = listOf("breakfast")),
      ),
    ).isEqualTo(AppRoute(computerSlug, appSlug, listOf("breakfast")))
    assertThat(routeCodec.decode(root.copy(path = listOf("invite", "1234"))))
      .isEqualTo(InviteRoute("1234"))
    assertThat(routeCodec.decode(root.copy(path = listOf("admin"))))
      .isEqualTo(AdminRoute)
    assertThat(routeCodec.decode(root.copy(path = listOf("build-yours"))))
      .isEqualTo(BuildYoursRoute)
    assertThat(routeCodec.decode(root.copy(path = listOf("after-checkout", "5678"))))
      .isEqualTo(AfterCheckoutRoute("5678"))
    assertThat(routeCodec.decode(root.copy(path = listOf("not-found"))))
      .isEqualTo(NotFoundRoute)
    assertThat(routeCodec.decode(root.copy(path = listOf("sign-out"))))
      .isEqualTo(SignOutRoute)
  }
}

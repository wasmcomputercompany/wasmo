package com.wasmo.common.routes

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.api.ComputerSlug
import com.wasmo.api.routes.AdminRoute
import com.wasmo.api.routes.AfterCheckoutRoute
import com.wasmo.api.routes.AppRoute
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.ComputerListRoute
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.NotFoundRoute
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.routes.TeaserRoute
import com.wasmo.api.routes.Url
import com.wasmo.identifiers.AppSlug
import kotlin.test.Test

class RealRouteCodecTest {
  private val root = Url(scheme = "https", topPrivateDomain = "wasmo.com")
  private val unauthenticated = RealRouteCodec(
    routingContext = RoutingContext(
      rootUrl = "https://wasmo.com/",
      hasComputers = false,
      hasInvite = false,
      isAdmin = false,
    ),
  )

  @Test
  fun encodeUnauthenticated() {
    val computerSlug = ComputerSlug("jessewilson99")
    val appSlug = AppSlug("recipes")

    assertThat(unauthenticated.encode(ComputerHomeRoute(computerSlug)))
      .isEqualTo(root.copy(subdomain = "jessewilson99"))
    assertThat(
      unauthenticated.encode(
        AppRoute(computerSlug, appSlug, listOf("breakfast")),
      ),
    ).isEqualTo(root.copy(subdomain = "recipes-jessewilson99", path = listOf("breakfast")))
    assertThat(unauthenticated.encode(InviteRoute("1234")))
      .isEqualTo(root.copy(path = listOf("invite", "1234")))
    assertThat(unauthenticated.encode(AdminRoute))
      .isEqualTo(root.copy(path = listOf("admin")))
    assertThat(unauthenticated.encode(TeaserRoute))
      .isEqualTo(root)
    assertThat(unauthenticated.encode(BuildYoursRoute))
      .isEqualTo(root.copy(path = listOf("build-yours")))
    assertThat(unauthenticated.encode(ComputerListRoute))
      .isEqualTo(root.copy(path = listOf("computers")))
    assertThat(unauthenticated.encode(AfterCheckoutRoute("5678")))
      .isEqualTo(root.copy(path = listOf("after-checkout", "5678")))
    assertThat(unauthenticated.encode(NotFoundRoute))
      .isEqualTo(root.copy(path = listOf("not-found")))
  }

  @Test
  fun decodeUnauthenticated() {
    val computerSlug = ComputerSlug("jessewilson99")
    val appSlug = AppSlug("recipes")

    assertThat(unauthenticated.decode(root.copy(subdomain = "jessewilson99")))
      .isEqualTo(ComputerHomeRoute(computerSlug))
    assertThat(
      unauthenticated.decode(
        root.copy(subdomain = "recipes-jessewilson99", path = listOf("breakfast")),
      ),
    ).isEqualTo(AppRoute(computerSlug, appSlug, listOf("breakfast")))
    assertThat(unauthenticated.decode(root.copy(path = listOf("invite", "1234"))))
      .isEqualTo(InviteRoute("1234"))
    assertThat(unauthenticated.decode(root.copy(path = listOf("admin"))))
      .isEqualTo(AdminRoute)
    assertThat(unauthenticated.decode(root.copy(path = listOf("teaser"))))
      .isEqualTo(TeaserRoute)
    assertThat(unauthenticated.decode(root.copy(path = listOf("build-yours"))))
      .isEqualTo(BuildYoursRoute)
    assertThat(unauthenticated.decode(root.copy(path = listOf("computers"))))
      .isEqualTo(ComputerListRoute)
    assertThat(unauthenticated.decode(root.copy(path = listOf("after-checkout", "5678"))))
      .isEqualTo(AfterCheckoutRoute("5678"))
    assertThat(unauthenticated.decode(root.copy(path = listOf("not-found"))))
      .isEqualTo(NotFoundRoute)
  }
}

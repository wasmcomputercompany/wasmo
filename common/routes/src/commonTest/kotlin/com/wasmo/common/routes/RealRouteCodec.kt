package com.wasmo.common.routes

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class RealRouteCodecTest {
  private val root = Url(topPrivateDomain = "wasmo.com")
  private val unauthenticated = RealRouteCodec(
    context = RealRouteCodec.Context(
      root = root,
      hasComputers = false,
      hasInvite = false,
      isAdmin = false,
    ),
  )

  @Test
  fun encodeUnauthenticated() {
    assertThat(unauthenticated.encode(ComputerHomeRoute("jessewilson99")))
      .isEqualTo(root.copy(subdomain = "jessewilson99"))
    assertThat(unauthenticated.encode(InviteRoute("1234")))
      .isEqualTo(root.copy(path = listOf("invite", "1234")))
    assertThat(unauthenticated.encode(AdminRoute))
      .isEqualTo(root.copy(path = listOf("admin")))
    assertThat(unauthenticated.encode(TeaserRoute))
      .isEqualTo(root)
    assertThat(unauthenticated.encode(BuildYoursRoute))
      .isEqualTo(root.copy(path = listOf("build-yours")))
    assertThat(unauthenticated.encode(ComputersRoute))
      .isEqualTo(root.copy(path = listOf("computers")))
    assertThat(unauthenticated.encode(AfterCheckoutRoute("5678")))
      .isEqualTo(root.copy(path = listOf("after-checkout", "5678")))
    assertThat(unauthenticated.encode(NotFoundRoute))
      .isEqualTo(root.copy(path = listOf("not-found")))
  }

  @Test
  fun decodeUnauthenticated() {
    assertThat(unauthenticated.decode(root.copy(subdomain = "jessewilson99")))
      .isEqualTo(ComputerHomeRoute("jessewilson99"))
    assertThat(unauthenticated.decode(root.copy(path = listOf("invite", "1234"))))
      .isEqualTo(InviteRoute("1234"))
    assertThat(unauthenticated.decode(root.copy(path = listOf("admin"))))
      .isEqualTo(AdminRoute)
    assertThat(unauthenticated.decode(root.copy(path = listOf("teaser"))))
      .isEqualTo(TeaserRoute)
    assertThat(unauthenticated.decode(root.copy(path = listOf("build-yours"))))
      .isEqualTo(BuildYoursRoute)
    assertThat(unauthenticated.decode(root.copy(path = listOf("computers"))))
      .isEqualTo(ComputersRoute)
    assertThat(unauthenticated.decode(root.copy(path = listOf("after-checkout", "5678"))))
      .isEqualTo(AfterCheckoutRoute("5678"))
    assertThat(unauthenticated.decode(root.copy(path = listOf("not-found"))))
      .isEqualTo(NotFoundRoute)
  }
}

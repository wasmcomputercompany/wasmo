package com.wasmo.api.routes

sealed interface Route

data class ComputerHomeRoute(
  val slug: String,
) : Route

data class InviteRoute(
  val code: String,
) : Route

data object AdminRoute : Route

data object TeaserRoute : Route

data object BuildYoursRoute : Route

data object ComputersRoute : Route

data class AfterCheckoutRoute(
  val checkoutSessionId: String,
) : Route

data object NotFoundRoute : Route


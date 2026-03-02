package com.wasmo.api.routes

sealed interface Route

data class ComputerHomeRoute(
  val slug: String,
) : Route

data class AppRoute(
  val computerSlug: String,
  val appSlug: String,
  val path: List<String> = listOf(""),
  val query: List<QueryParameter>? = listOf(),
) : Route

data class InviteRoute(
  val code: String,
) : Route

data object AdminRoute : Route

data object TeaserRoute : Route

data object BuildYoursRoute : Route

data object ComputerListRoute : Route

data class AfterCheckoutRoute(
  val checkoutSessionId: String,
) : Route

data object NotFoundRoute : Route


package com.wasmo.api.routes

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug

sealed interface Route

data class ComputerHomeRoute(
  val slug: ComputerSlug,
) : Route

data class AppRoute(
  val computerSlug: ComputerSlug,
  val appSlug: AppSlug,
  val path: List<String> = listOf(""),
  val query: List<QueryParameter>? = listOf(),
) : Route

data class InviteRoute(
  val code: String,
) : Route

data object AdminRoute : Route

data object SignUpRoute : Route

data object HomeRoute : Route

data object BuildYoursRoute : Route

data class AfterCheckoutRoute(
  val checkoutSessionId: String,
) : Route

data object NotFoundRoute : Route

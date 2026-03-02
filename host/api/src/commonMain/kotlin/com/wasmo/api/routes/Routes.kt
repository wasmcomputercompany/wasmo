package com.wasmo.api.routes

import com.wasmo.api.AppSlugRegex
import com.wasmo.api.ComputerSlugRegex

sealed interface Route

data class ComputerHomeRoute(
  val slug: String,
) : Route {
  init {
    require(slug.matches(ComputerSlugRegex)) {
      "unexpected computer: $slug"
    }
  }
}

data class AppRoute(
  val computerSlug: String,
  val appSlug: String,
  val path: List<String> = listOf(""),
  val query: List<QueryParameter>? = listOf(),
) : Route {
  init {
    require(computerSlug.matches(ComputerSlugRegex)) {
      "unexpected computer: $computerSlug"
    }
    require(appSlug.matches(AppSlugRegex)) {
      "unexpected app: $appSlug"
    }
  }
}

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

package com.wasmo.journal.app

import com.wasmo.support.router.Router

sealed interface Route {
  data object NotFound : Route
  data object Admin : Route
  data class EditEntry(val token: String) : Route

  companion object : Router.RouteCodec<Route> {
    private val adminRouteRegex = Regex("/admin")
    private val editRouteRegex = Regex("/admin/entries/([^/]+)")

    override fun encode(route: Route): String {
      return when (route) {
        Admin -> "/admin"
        is EditEntry -> "/admin/entries/${route.token}"
        is NotFound -> "/admin"
      }
    }

    override fun decode(path: String): Route {
      editRouteRegex.matchEntire(path)?.let { match ->
        return EditEntry(match.groups[1]!!.value)
      }

      adminRouteRegex.matchEntire(path)?.let { match ->
        return Admin
      }

      return NotFound
    }
  }
}

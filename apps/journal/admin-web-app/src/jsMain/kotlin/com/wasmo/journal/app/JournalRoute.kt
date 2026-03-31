package com.wasmo.journal.app

import com.wasmo.journal.app.util.Router

sealed interface JournalRoute {
  data object NotFoundRoute : JournalRoute
  data object AdminRoute : JournalRoute
  data class EditEntryRoute(val token: String) : JournalRoute

  companion object : Router.RouteCodec<JournalRoute> {
    private val adminRouteRegex = Regex("/admin")
    private val editRouteRegex = Regex("/admin/entries/([^/]+)")

    override fun encode(route: JournalRoute): String {
      return when (route) {
        AdminRoute -> "/admin"
        is EditEntryRoute -> "/admin/entries/${route.token}"
        is NotFoundRoute -> "/admin"
      }
    }

    override fun decode(path: String): JournalRoute {
      editRouteRegex.matchEntire(path)?.let { match ->
        return EditEntryRoute(match.groups[1]!!.value)
      }

      adminRouteRegex.matchEntire(path)?.let { match ->
        return AdminRoute
      }

      return NotFoundRoute
    }
  }
}

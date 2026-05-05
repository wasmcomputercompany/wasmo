package com.wasmo.ktor

import com.wasmo.framework.ActionRegistration
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.routing.routing

@Inject
@SingleIn(OsScope::class)
class ActionRouter(
  private val application: Application,
  private val actionBinderFactory: KtorActionBinder.Factory,
  private val actionRegistrations: Set<ActionRegistration>,
) {
  fun createRoutes() {
    application.install(CallLogging)
    application.routing {
      // Register actions from most precise to least precise.
      //  - RPCs (these don't collide with anything else)
      //  - HTTPs with a path and host
      //  - HTTPs with a path
      //  - other HTTPs
      //  - Static Resources
      val sortedActionRegistrations = actionRegistrations.toList().sortedBy {
        when (it) {
          is ActionRegistration.Rpc<*, *> -> 0
          is ActionRegistration.Http -> {
            when {
              it.pattern.host != null && it.pattern.path != null -> 1
              it.pattern.host != null -> 2
              else -> 3
            }
          }

          is ActionRegistration.StaticResources -> 4
        }
      }

      for (actionRegistration in sortedActionRegistrations) {
        actionBinderFactory.create(this).register(actionRegistration)
      }
    }
  }
}

package com.wasmo.ktor

import com.wasmo.framework.ActionSource
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
  private val actionSources: Set<ActionSource>,
) {
  fun createRoutes() {
    application.install(CallLogging)
    application.routing {
      for (source in actionSources.toList().sortedBy { it.order }) {
        context(actionBinderFactory.create(this)) {
          source.bindActions()
        }
      }
    }
  }
}

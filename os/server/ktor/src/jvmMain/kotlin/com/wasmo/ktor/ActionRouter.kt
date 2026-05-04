package com.wasmo.ktor

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
  private val httpActionBinderFactory: KtorHttpActionBinder.Factory,
  private val httpActionSources: Set<HttpActionSource>,
) {
  fun createRoutes() {
    application.install(CallLogging)
    application.routing {
      context(httpActionBinderFactory.create(this)) {
        for (source in httpActionSources.toList().sortedBy { it.order }) {
          source.bindActions()
        }
      }
    }
  }
}

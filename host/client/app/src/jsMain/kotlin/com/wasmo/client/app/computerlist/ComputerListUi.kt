package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.toURL
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

class ComputerListUi(
  val routeCodec: RouteCodec,
  val router: Router,
  val computerListSnapshot: ComputerListSnapshot,
) : Ui {
  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    ComputerListScreen(
      attrs = attrs,
      items = computerListSnapshot.items.map {
        Item(
          slug = it.slug,
          iframeSrc = routeCodec.encode(ComputerHomeRoute(it.slug)).toURL().href,
        )
      },
    ) { event ->
      when (event) {
        is ComputerListEvent.ClickComputer -> {
          router.goTo(
            ComputerHomeRoute(slug = event.slug),
            TransitionDirection.PUSH,
          )
        }
      }
    }
  }

  @Inject
  @SingleIn(AppScope::class)
  class Factory(
    val routeCodec: RouteCodec,
    val router: Router,
    val computerListSnapshot: ComputerListSnapshot?,
  ) {
    fun create() = ComputerListUi(
      routeCodec = routeCodec,
      router = router,
      computerListSnapshot = computerListSnapshot
        ?: error("unexpected call of ComputerListUi.Factory.create(), snapshot is absent"),
    )
  }
}

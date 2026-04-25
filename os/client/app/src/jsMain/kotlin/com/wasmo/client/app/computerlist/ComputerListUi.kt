package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.toURL
import com.wasmo.client.app.home.HomeEvent
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

@AssistedInject
class ComputerListUi private constructor(
  private val routeCodec: RouteCodec,
  private val router: Router,
  computerListSnapshot: ComputerListSnapshot?,
) : Ui {
  private val computerListSnapshot: ComputerListSnapshot = computerListSnapshot
    ?: error("unexpected call of ComputerListUi.Factory.create(), snapshot is absent")

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    HomeScreenWithComputerList(
      attrs = attrs,
      scrimVisible = false,
      menuModel = null,
      items = computerListSnapshot.items.map {
        Item(
          slug = it.slug,
          iframeSrc = routeCodec.encode(ComputerHomeRoute(it.slug)).toURL().href,
        )
      },
    ) { event ->
      when (event) {
        is HomeEvent.ClickComputer -> {
          router.goTo(
            ComputerHomeRoute(slug = event.slug),
            TransitionDirection.PUSH,
          )
        }
        else -> error("Unexpected event: $event")
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(): ComputerListUi
  }
}

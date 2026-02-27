package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.Url
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

class ComputerListUi(
  val router: Router,
) : Ui {
  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    ComputerListScreen(
      attrs = attrs,
      items = listOf(
        ComputerListItem(
          slug = "jesse99",
          url = Url(
            scheme = "https",
            topPrivateDomain = "wasmo.com",
            subdomain = "jesse99",
          ),
        ),
        ComputerListItem(
          slug = "rounds",
          url = Url(
            scheme = "https",
            topPrivateDomain = "wasmo.com",
            subdomain = "rounds",
          ),
        ),
      ),
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

  class Factory(
    val router: Router,
  ) {
    fun create() = ComputerListUi(
      router = router,
    )
  }
}

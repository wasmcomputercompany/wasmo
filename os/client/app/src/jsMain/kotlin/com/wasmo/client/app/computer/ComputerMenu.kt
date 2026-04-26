package com.wasmo.client.app.computer

import androidx.compose.runtime.Composable
import com.wasmo.compose.Menu
import com.wasmo.compose.MenuItem
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLDivElement

@Composable
fun ComputerMenu(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  model: ComputerMenuModel?,
  eventListener: (ComputerMenuEvent) -> Unit,
) {
  Menu(
    attrs = attrs,
    visible = model != null,
    onDismiss = {
      eventListener(ComputerMenuEvent.ClickDismiss)
    },
    content = {
      MenuItem(
        label = "Sign Out",
        onClick = {
          eventListener(ComputerMenuEvent.ClickSignOut)
        },
      )
      MenuItem(
        label = "Install App",
        onClick = {
          eventListener(ComputerMenuEvent.ClickInstallApp)
        },
      )
      MenuItem(
        label = "Settings",
        onClick = {
          eventListener(ComputerMenuEvent.ClickSettings)
        },
      )
    },
  )
}

sealed interface ComputerMenuEvent {
  object ClickDismiss : ComputerMenuEvent
  object ClickInstallApp : ComputerMenuEvent
  object ClickSettings : ComputerMenuEvent
  object ClickSignOut : ComputerMenuEvent
}

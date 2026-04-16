package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import com.wasmo.compose.Menu
import com.wasmo.compose.MenuItem
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLDivElement

class HomeMenuModel

@Composable
fun HomeMenu(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  model: HomeMenuModel?,
  eventListener: (HomeEvent) -> Unit,
) {
  Menu(
    attrs = attrs,
    visible = model != null,
    onDismiss = {
      eventListener(HomeEvent.ClickDismissMenu)
    },
    content = {
      MenuItem(
        label = "Sign Up",
        onClick = {
          eventListener(HomeEvent.ClickSignUp)
        },
      )
      MenuItem(
        label = "Sign In",
        onClick = {
          eventListener(HomeEvent.ClickSignIn)
        },
      )
    },
  )
}

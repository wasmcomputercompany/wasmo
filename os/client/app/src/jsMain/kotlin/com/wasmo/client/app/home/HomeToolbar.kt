package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import com.wasmo.compose.Toolbar
import com.wasmo.compose.ToolbarImageButton
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.px

@Composable
fun HomeToolbar(
  eventListener: (HomeEvent) -> Unit,
) {
  Toolbar(
    attrs = {
      classes("ScreenContentWidth")
      style {
        marginBottom(32.px)
      }
    },
    right = { toolbarChildAttrs ->
      ToolbarImageButton(
        attrs = toolbarChildAttrs,
        image40x64Path = "/assets/menu40x64.svg",
        altLabel = "Menu",
        onClick = {
          eventListener(HomeEvent.ClickShowMenu)
        },
      )
    },
  )
}


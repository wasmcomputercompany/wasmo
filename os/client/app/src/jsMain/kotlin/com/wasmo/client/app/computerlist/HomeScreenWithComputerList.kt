package com.wasmo.client.app.computerlist

import androidx.compose.runtime.Composable
import com.wasmo.client.app.home.HomeEvent
import com.wasmo.client.app.home.HomeMenuModel
import com.wasmo.client.app.home.HomeScreen
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLDivElement

@Composable
fun HomeScreenWithComputerList(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  scrimVisible: Boolean,
  menuModel: HomeMenuModel?,
  items: List<Item>,
  eventListener: (HomeEvent) -> Unit,
) {
  HomeScreen(
    attrs = attrs,
    scrimVisible = scrimVisible,
    menuModel = menuModel,
    eventListener = eventListener,
    content = { homeScreenChildAttrs ->
      ComputerList(
        attrs = homeScreenChildAttrs,
        items = items,
        eventListener = eventListener,
      )
    },
  )
}

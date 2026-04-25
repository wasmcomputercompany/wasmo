package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import com.wasmo.client.app.teaser.Teaser
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLDivElement

@Composable
fun HomeScreenWithTeaser(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  showSignUp: Boolean,
  scrimVisible: Boolean,
  menuModel: HomeMenuModel?,
  eventListener: (HomeEvent) -> Unit,
) {
  HomeScreen(
    attrs = attrs,
    scrimVisible = scrimVisible,
    menuModel = menuModel,
    eventListener = eventListener,
    content = { homeScreenChildAttrs ->
      Teaser(
        attrs = homeScreenChildAttrs,
        showSignUp = showSignUp,
        eventListener = eventListener,
      )
    },
  )
}


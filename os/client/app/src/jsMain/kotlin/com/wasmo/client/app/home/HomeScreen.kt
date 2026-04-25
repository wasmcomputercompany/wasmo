package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import com.wasmo.client.app.computerlist.ComputerList
import com.wasmo.client.app.computerlist.Item
import com.wasmo.client.app.teaser.Teaser
import com.wasmo.compose.OverlayContainer
import com.wasmo.identifiers.ComputerSlug
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

@Composable
fun HomeScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  menuModel: HomeMenuModel,
  items: List<Item>,
  teaser: Boolean,
  showSignUp: Boolean,
  eventListener: (HomeEvent) -> Unit,
) {
  HomeScreen(
    attrs = attrs,
    menuModel = menuModel,
    eventListener = eventListener,
    content = { homeScreenChildAttrs ->
      if (items.isNotEmpty()) {
        ComputerList(
          attrs = homeScreenChildAttrs,
          items = items,
          eventListener = eventListener,
        )
      } else if (teaser) {
        Teaser(
          attrs = homeScreenChildAttrs,
          showSignUp = showSignUp,
          eventListener = eventListener,
        )
      }
    },
  )
}

@Composable
fun HomeScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  menuModel: HomeMenuModel,
  eventListener: (HomeEvent) -> Unit,
  content: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit,
) {
  OverlayContainer(
    attrs = {
      style {
        width(100.percent)
        height(100.percent)
        boxSizing("border-box")
      }
      attrs()
    },
    showScrim = menuModel.visible,
    onClickScrim = {
      eventListener(HomeEvent.ClickScrim)
    },
    overlay = { zstackChildAttrs ->
      HomeMenu(
        attrs = zstackChildAttrs,
        model = menuModel,
        eventListener = eventListener,
      )
    },
  ) { zstackChildAttrs ->
    Div(
      attrs = {
        classes("HomeScreen")
        style {
          width(100.percent)
          height(100.percent)
          display(DisplayStyle.Flex)
          flexDirection(FlexDirection.Column)
          alignItems(AlignItems.Center)
          justifyContent(JustifyContent.Center)
        }
        zstackChildAttrs()
      },
    ) {
      HomeToolbar(
        eventListener = eventListener,
      )

      content {
        style {
          flex(100, 100, 0.px)
        }
      }
    }
  }
}

sealed interface HomeEvent {
  object SignUp : HomeEvent
  object ClickScrim : HomeEvent
  object ClickShowMenu : HomeEvent
  object ClickDismissMenu : HomeEvent
  object ClickSignUp : HomeEvent
  object ClickSignIn : HomeEvent
  object ClickSignOut : HomeEvent
  data class ClickComputer(val slug: ComputerSlug) : HomeEvent
}

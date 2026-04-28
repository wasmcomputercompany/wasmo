package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import com.wasmo.client.app.computerlist.ComputerList
import com.wasmo.client.app.computerlist.Item
import com.wasmo.client.app.computerlist.NewComputer
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
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.overflowY
import org.jetbrains.compose.web.css.paddingBottom
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
  showNewComputer: Boolean,
  eventListener: (HomeEvent) -> Unit,
) {
  HomeScreen(
    attrs = attrs,
    menuModel = menuModel,
    eventListener = eventListener,
    content = { homeScreenChildAttrs ->
      if (items.isNotEmpty()) {
        ComputerList(
          attrs = {
            style {
              paddingBottom(24.px)
            }
            homeScreenChildAttrs()
          },
          items = items,
          eventListener = eventListener,
        )
      }

      if (showNewComputer) {
        NewComputer(
          attrs = {
            classes("ScreenContentWidth")
            style {
              paddingBottom(48.px)
            }
            homeScreenChildAttrs()
          },
          eventListener = eventListener,
        )
      }

      if (items.isEmpty() && !showNewComputer) {
        Teaser(
          attrs = {
            classes("ScreenContentWidth")
            homeScreenChildAttrs()
          },
        )
      }
    },
  )
}

/**
 * This supports:
 *
 *  * Toolbar + Menu
 *  * Scrim for menus and dialogs
 *  * Vertically-center content if it's shorter than the screen, scroll it otherwise
 */
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
          display(DisplayStyle.Flex)
          flexDirection(FlexDirection.Column)
          justifyContent(JustifyContent.Center)
          alignItems(AlignItems.Stretch)
        }
        zstackChildAttrs()
      },
    ) {
      Div(
        attrs = {
          style {
            minHeight(100.percent)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
            overflowY("scroll")
          }
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
}

sealed interface HomeEvent {
  object ClickScrim : HomeEvent
  object ClickShowMenu : HomeEvent
  object ClickDismissMenu : HomeEvent
  object ClickSignUp : HomeEvent
  object ClickSignIn : HomeEvent
  object ClickSignOut : HomeEvent
  object ClickNewComputer : HomeEvent
  data class ClickComputer(val slug: ComputerSlug) : HomeEvent
}

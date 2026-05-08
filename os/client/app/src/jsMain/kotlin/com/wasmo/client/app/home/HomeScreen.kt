package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import com.wasmo.client.app.computerlist.ComputerList
import com.wasmo.client.app.computerlist.Item
import com.wasmo.client.app.computerlist.NewComputer
import com.wasmo.client.app.teaser.Teaser
import com.wasmo.compose.Column
import com.wasmo.compose.OverlayContainer
import com.wasmo.compose.PageLayout
import com.wasmo.identifiers.ComputerSlug
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Section
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
      Column(
        attrs = {
          style {
            alignSelf(AlignSelf.Center)
          }
          homeScreenChildAttrs()
        },
      ) {
        if (items.isNotEmpty()) {
          Section {
            ComputerList(
              items = items,
              eventListener = eventListener,
            )
          }
        }

        if (showNewComputer) {
          Section {
            NewComputer(
              attrs = {
                classes("ContentWidth")
              },
              eventListener = eventListener,
            )
          }
        }

        if (items.isEmpty() && !showNewComputer) {
          Teaser(
            attrs = {
              classes("ContentWidth")
            },
          )
        }
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
    PageLayout(
      attrs = {
        classes("HomeScreen")
        zstackChildAttrs()
      },
      header = { headerAttrs ->
        HomeToolbar(
          attrs = headerAttrs,
          eventListener = eventListener,
        )
      },
      content = { contentAttrs ->
        content(contentAttrs)
      },
    )
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

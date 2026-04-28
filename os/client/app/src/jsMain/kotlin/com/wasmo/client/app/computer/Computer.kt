package com.wasmo.client.app.computer

import androidx.compose.runtime.Composable
import com.wasmo.api.ComputerSnapshot
import com.wasmo.compose.Icon
import com.wasmo.compose.LauncherIconList
import com.wasmo.compose.LauncherScreen
import com.wasmo.compose.OverlayContainer
import com.wasmo.compose.Toolbar
import com.wasmo.compose.ToolbarImageButton
import com.wasmo.compose.ToolbarTitle
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

@Composable
fun Computer(
  attrs: AttrsScope<HTMLElement>.() -> Unit = {},
  scrimVisible: Boolean,
  snapshot: ComputerSnapshot,
  eventListener: (ComputerEvent) -> Unit,
  overlays: @Composable DOMScope<HTMLDivElement>.(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) -> Unit = {},
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
    showScrim = scrimVisible,
    onClickScrim = {
      eventListener(ComputerEvent.ClickScrim)
    },
    overlay = { zstackChildAttrs ->
      overlays(zstackChildAttrs)
    },
  ) { zstackChildAttrs ->
    LauncherScreen(
      attrs = zstackChildAttrs,
    ) {
      ComputerToolbar(
        slug = snapshot.slug,
        eventListener = eventListener,
      )

      LauncherIconList {
        for (app in snapshot.apps) {
          Icon(app.launcherLabel, app.maskableIconUrl) {
            eventListener(ComputerEvent.ClickApp(app.slug))
          }
        }
      }
    }
  }
}

@Composable
fun ComputerToolbar(
  attrs: AttrsScope<HTMLElement>.() -> Unit = {},
  slug: ComputerSlug,
  eventListener: (ComputerEvent) -> Unit,
) {
  Toolbar(
    attrs = {
      classes("ScreenContentWidth")
      style {
        marginBottom(32.px)
      }
      attrs()
    },
    title = { toolbarChildAttrs ->
      ToolbarTitle(attrs = toolbarChildAttrs) {
        Text(slug.value)
      }
    },
    right = { toolbarChildAttrs ->
      ToolbarImageButton(
        attrs = toolbarChildAttrs,
        image40x64Path = "/assets/menu40x64.svg",
        altLabel = "Menu",
        onClick = {
          eventListener(ComputerEvent.ClickShowMenu)
        },
      )
    },
  )
}

sealed interface ComputerEvent {
  data class ClickApp(
    val app: AppSlug,
  ) : ComputerEvent

  object ClickShowMenu : ComputerEvent
  object ClickScrim : ComputerEvent
  object ClickDismissMenu : ComputerEvent
}

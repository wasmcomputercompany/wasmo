package com.wasmo.client.app.computer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.routes.AppRoute
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import com.wasmo.compose.Menu
import com.wasmo.compose.MenuItem
import com.wasmo.compose.OverlayContainer
import com.wasmo.compose.Toolbar
import com.wasmo.compose.ToolbarImageButton
import com.wasmo.compose.ToolbarTitle
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.launcher.Icon
import com.wasmo.launcher.LauncherIconList
import com.wasmo.launcher.LauncherScreen
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

@AssistedInject
class ComputerUi(
  @Assisted private val slug: ComputerSlug,
  private val router: Router,
  computerSnapshot: ComputerSnapshot?,
) : Ui {

  private val computerSnapshot: ComputerSnapshot = computerSnapshot
    ?: error("unexpected call of ComputerUi.Factory.create(), snapshot is absent")

  private var menuVisible by mutableStateOf(false)

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
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
      showScrim = menuVisible,
      onClickScrim = {
        menuVisible = !menuVisible
      },
      overlay = { zstackChildAttrs ->
        LauncherMenu(
          attrs = zstackChildAttrs,
        )
      },
    ) { zstackChildAttrs ->
      LauncherScreen(
        attrs = zstackChildAttrs,
      ) {
        LauncherToolbar()

        LauncherIconList {
          for (app in computerSnapshot.apps) {
            Icon(app.launcherLabel, app.maskableIconUrl) {
              router.goTo(AppRoute(slug, app.slug), TransitionDirection.PUSH)
            }
          }
        }
      }
    }
  }

  @Composable
  private fun LauncherMenu(
    attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  ) {
    Menu(
      attrs = attrs,
      visible = menuVisible,
      onDismiss = {
        menuVisible = false
      },
      content = {
        MenuItem(
          label = "Install App",
          onClick = {},
        )
        MenuItem(
          label = "Settings",
          onClick = {},
        )
      },
    )
  }

  @Composable
  private fun LauncherToolbar() {
    Toolbar(
      attrs = {
        style {
          marginBottom(8.px)
        }
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
            menuVisible = !menuVisible
          },
        )
      },
    )
  }

  @AssistedFactory
  interface Factory {
    fun create(slug: ComputerSlug): ComputerUi
  }
}

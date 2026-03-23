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
import com.wasmo.identifiers.ComputerSlug
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import org.jetbrains.compose.web.attributes.AttrsScope
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
  private var installAppDialogVisible by mutableStateOf(false)

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    Computer(
      attrs = attrs,
      scrimVisible = menuVisible || installAppDialogVisible,
      eventListener = { event ->
        when (event) {
          ComputerEvent.ClickShowMenu -> {
            menuVisible = true
          }

          ComputerEvent.ClickDismissMenu -> menuVisible = false
          is ComputerEvent.ClickApp -> {
            router.goTo(AppRoute(slug, event.app), TransitionDirection.PUSH)
          }

          ComputerEvent.ClickScrim -> {
            installAppDialogVisible = false
            menuVisible = false
          }
        }
      },
      overlays = { computerChildAttrs ->
        ComputerMenu(
          attrs = computerChildAttrs,
          visible = menuVisible,
          eventListener = { event ->
            when (event) {
              ComputerMenuEvent.ClickDismiss -> {
                menuVisible = false
              }

              ComputerMenuEvent.ClickInstallApp -> {
                menuVisible = false
                installAppDialogVisible = true
              }

              ComputerMenuEvent.ClickSettings -> {
                menuVisible = false
              }
            }
          },
        )
        InstallAppDialog(
          attrs = computerChildAttrs,
          visible = installAppDialogVisible,
          eventListener = { event ->
            when (event) {
              InstallAppDialogEvent.ClickDismiss -> {
                installAppDialogVisible = false
              }

              is InstallAppDialogEvent.ClickInstall -> {
                installAppDialogVisible = false
              }
            }
          },
        )
      },
      snapshot = computerSnapshot,
    )
  }

  @AssistedFactory
  interface Factory {
    fun create(slug: ComputerSlug): ComputerUi
  }
}

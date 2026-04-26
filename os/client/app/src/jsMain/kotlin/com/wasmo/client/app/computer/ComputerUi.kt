package com.wasmo.client.app.computer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.routes.HomeRoute
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.SignOutRoute
import com.wasmo.api.routes.decodeUrl
import com.wasmo.client.app.FormState
import com.wasmo.client.app.data.ComputerDataService
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import com.wasmo.common.logging.Logger
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

@AssistedInject
class ComputerUi(
  @Assisted private val slug: ComputerSlug,
  private val coroutineScope: CoroutineScope,
  private val logger: Logger,
  private val router: Router,
  private val routeCodec: RouteCodec,
  private val computerDataService: ComputerDataService,
  computerSnapshot: ComputerSnapshot?,
) : Ui {
  private val computerSnapshot: ComputerSnapshot = computerSnapshot
    ?: error("unexpected call of ComputerUi.Factory.create(), snapshot is absent")

  private var menuModel by mutableStateOf<ComputerMenuModel?>(null)
  private var installAppDialogModel by mutableStateOf<InstallAppDialogModel?>(null)

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    Computer(
      attrs = attrs,
      scrimVisible = menuModel != null || installAppDialogModel != null,
      eventListener = ::onComputerEvent,
      overlays = { computerChildAttrs ->
        ComputerMenu(
          attrs = computerChildAttrs,
          model = menuModel,
          eventListener = ::onComputerMenuEvent,
        )
        InstallAppDialog(
          attrs = computerChildAttrs,
          model = installAppDialogModel,
          eventListener = ::onInstallAppDialogEvent,
        )
      },
      snapshot = computerSnapshot,
    )
  }

  private fun onComputerEvent(event: ComputerEvent) {
    when (event) {
      ComputerEvent.ClickShowMenu -> {
        menuModel = ComputerMenuModel()
      }

      ComputerEvent.ClickDismissMenu -> menuModel = null
      is ComputerEvent.ClickApp -> {
        val appSnapshot = computerSnapshot.apps.single { it.slug == event.app }
        router.goTo(
          route = routeCodec.decode(appSnapshot.homeUrl.decodeUrl()),
          transitionDirection = TransitionDirection.PUSH,
        )
      }

      ComputerEvent.ClickScrim -> {
        installAppDialogModel = null
        menuModel = null
      }
    }
  }

  private fun onComputerMenuEvent(event: ComputerMenuEvent) {
    when (event) {
      ComputerMenuEvent.ClickDismiss -> {
        menuModel = null
      }

      ComputerMenuEvent.ClickSignOut -> {
        router.goTo(SignOutRoute, TransitionDirection.PUSH)
      }

      ComputerMenuEvent.ClickMyComputers -> {
        router.goTo(HomeRoute, TransitionDirection.POP)
      }

      ComputerMenuEvent.ClickInstallApp -> {
        menuModel = null
        installAppDialogModel = InstallAppDialogModel(FormState.Ready)
      }

      ComputerMenuEvent.ClickSettings -> {
        menuModel = null
      }
    }
  }

  private fun onInstallAppDialogEvent(event: InstallAppDialogEvent) {
    when (event) {
      InstallAppDialogEvent.ClickDismiss -> {
        installAppDialogModel = null
      }

      is InstallAppDialogEvent.ClickInstall -> {
        installAppDialogModel = InstallAppDialogModel(FormState.Busy)
        coroutineScope.launch {
          try {
            computerDataService.install(event.appUrl, AppSlug(event.slug))
            installAppDialogModel = null
          } catch (e: Exception) {
            logger.info("failed to install app", e)
            installAppDialogModel = InstallAppDialogModel(FormState.Ready)
          }
        }
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(slug: ComputerSlug): ComputerUi
  }
}

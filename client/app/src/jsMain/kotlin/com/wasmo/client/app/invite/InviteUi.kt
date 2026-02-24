package com.wasmo.client.app.invite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.WasmoApi
import com.wasmo.client.app.Environment
import com.wasmo.client.app.FormState
import com.wasmo.client.app.LocalFormState
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import com.wasmo.common.logging.Logger
import com.wasmo.common.routes.BuildYoursRoute
import com.wasmo.passkeys.PasskeyAuthenticator
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

class InviteUi(
  val router: Router,
  val passkeyAuthenticator: PasskeyAuthenticator,
  val wasmoApi: WasmoApi,
  val logger: Logger,
  val environment: Environment,
  val accountSnapshot: AccountSnapshot,
  val inviteTicket: InviteTicket,
) : Ui {
  var inviteState by mutableStateOf(InviteState.Ready)

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    val scope = rememberCoroutineScope()

    var formState by remember { mutableStateOf(FormState.Ready) }
    CompositionLocalProvider(LocalFormState provides formState) {
      InviteScreen(
        attrs = attrs,
        inviteState = inviteState,
      ) { event ->
        when (event) {
          InviteEvent.ClickAccept -> {
            inviteState = InviteState.ClientBusy
            scope.launch {
              try {
                val passkeyRegistration = passkeyAuthenticator.register(
                  user = environment.passkeyUser,
                  challenge = accountSnapshot.nextChallenge,
                )
                inviteState = InviteState.ServerBusy
                wasmoApi.registerPasskey(
                  RegisterPasskeyRequest(passkeyRegistration),
                )
                router.goTo(BuildYoursRoute, TransitionDirection.REPLACE)
              } catch (e: Throwable) {
                logger.info("passkey failed", e)
                inviteState = InviteState.Failed
                delay(2.seconds)
              } finally {
                inviteState = InviteState.Ready
              }
            }
          }
        }
      }
    }
  }

  class Factory(
    val router: Router,
    val passkeyAuthenticator: PasskeyAuthenticator,
    val wasmoApi: WasmoApi,
    val logger: Logger,
    val environment: Environment,
  ) {
    fun create(
      accountSnapshot: AccountSnapshot,
      inviteTicket: InviteTicket,
    ) = InviteUi(
      router = router,
      passkeyAuthenticator = passkeyAuthenticator,
      wasmoApi = wasmoApi,
      logger = logger,
      environment = environment,
      accountSnapshot = accountSnapshot,
      inviteTicket = inviteTicket,
    )
  }
}

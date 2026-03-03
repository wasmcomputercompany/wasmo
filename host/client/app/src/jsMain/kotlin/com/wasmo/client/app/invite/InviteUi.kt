package com.wasmo.client.app.invite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.InviteTicket
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.WasmoApi
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.client.app.Environment
import com.wasmo.client.app.FormState
import com.wasmo.client.app.LocalFormState
import com.wasmo.client.app.data.AccountDataService
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import com.wasmo.common.logging.Logger
import com.wasmo.passkeys.PasskeyAuthenticator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

class InviteUi(
  val router: Router,
  val passkeyAuthenticator: PasskeyAuthenticator,
  val accountDataService: AccountDataService,
  val wasmoApi: WasmoApi,
  val logger: Logger,
  val environment: Environment,
  val inviteTicket: InviteTicket,
) : Ui {
  private val initialInviteState: InviteState
    get() = when {
      inviteTicket.claimed -> InviteState.ReadyToSignIn
      else -> InviteState.ReadyToAccept
    }

  private var inviteState by mutableStateOf(initialInviteState)

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
            scope.acceptInvitation()
          }
        }
      }
    }
  }

  private fun CoroutineScope.acceptInvitation() {
    inviteState = InviteState.ClientBusy
    launch {
      try {
        if (!inviteTicket.claimed) {
          val passkeyRegistration = passkeyAuthenticator.register(
            user = environment.passkeyUser,
            challenge = accountDataService.accountSnapshot.nextChallenge,
          )
          inviteState = InviteState.ServerBusy
          val response = wasmoApi.registerPasskey(
            RegisterPasskeyRequest(
              registration = passkeyRegistration,
              inviteCode = inviteTicket.code,
            ),
          )
          accountDataService.receiveAccountSnapshot(response.account)
        } else {
          val passkeyAuthentication = passkeyAuthenticator.authenticate(
            challenge = accountDataService.accountSnapshot.nextChallenge,
          )
          inviteState = InviteState.ServerBusy
          val response = wasmoApi.authenticatePasskey(
            AuthenticatePasskeyRequest(
              authentication = passkeyAuthentication,
              inviteCode = inviteTicket.code,
            ),
          )
          accountDataService.receiveAccountSnapshot(response.account)
        }

        router.goTo(BuildYoursRoute, TransitionDirection.REPLACE)
      } catch (e: Throwable) {
        logger.info("passkey failed", e)
        inviteState = InviteState.Failed
        delay(2.seconds)
        inviteState = initialInviteState
      }
    }
  }

  @Inject
  @SingleIn(AppScope::class)
  class Factory(
    val router: Router,
    val passkeyAuthenticator: PasskeyAuthenticator,
    val accountDataService: AccountDataService,
    val wasmoApi: WasmoApi,
    val logger: Logger,
    val environment: Environment,
  ) {
    fun create(
      inviteTicket: InviteTicket,
    ) = InviteUi(
      router = router,
      passkeyAuthenticator = passkeyAuthenticator,
      accountDataService = accountDataService,
      wasmoApi = wasmoApi,
      logger = logger,
      environment = environment,
      inviteTicket = inviteTicket,
    )
  }
}

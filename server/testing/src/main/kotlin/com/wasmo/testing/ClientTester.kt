package com.wasmo.testing

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.ConfirmEmailAddressAction
import com.wasmo.accounts.LinkEmailAddressAction
import com.wasmo.accounts.RealAccountStore
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.app.db.WasmoDbService
import com.wasmo.computers.ComputerStore
import com.wasmo.computers.CreateComputerAction
import com.wasmo.computers.InstallAppAction
import com.wasmo.passkeys.AuthenticatePasskeyAction
import com.wasmo.passkeys.Challenger
import com.wasmo.passkeys.PasskeyLinker
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.passkeys.RealPasskeyChecker
import com.wasmo.passkeys.RegisterPasskeyAction
import kotlin.time.Clock
import okhttp3.HttpUrl

class ClientTester(
  private val clock: Clock,
  private val service: WasmoDbService,
  private val clientAuthenticator: ClientAuthenticator,
  private val computerStore: ComputerStore,
  private val baseUrl: HttpUrl,
  val challenger: Challenger,
) {
  val authenticatorDatabase = RealAuthenticatorDatabase()

  val accountStoreFactory = RealAccountStore.Factory(
    challenger = challenger,
    authenticatorDatabase = authenticatorDatabase,
    passkeyQueries = service.passkeyQueries,
  )

  val passkeyChecker = RealPasskeyChecker(
    challenger = challenger,
    baseUrl = baseUrl,
  )

  val passkeyLinkerFactory = PasskeyLinker.Factory(
    cookieQueries = service.cookieQueries,
  )

  fun linkEmailAddressAction() = LinkEmailAddressAction(
    client = clientAuthenticator.get(),
  )

  fun confirmEmailAddressAction() = ConfirmEmailAddressAction(
    client = clientAuthenticator.get(),
  )

  fun registerPasskeyAction() = RegisterPasskeyAction(
    clock = clock,
    accountStoreFactory = accountStoreFactory,
    client = clientAuthenticator.get(),
    passkeyChecker = passkeyChecker,
    passkeyQueries = service.passkeyQueries,
  )

  fun register(passkey: FakePasskey) = registerPasskeyAction().register(
    request = RegisterPasskeyRequest(
      registration = passkey.registration(
        challenge = challenger.create(),
        origin = baseUrl.toString(),
      ),
    ),
  )

  fun authenticatePasskeyAction() = AuthenticatePasskeyAction(
    accountStoreFactory = accountStoreFactory,
    client = clientAuthenticator.get(),
    passkeyChecker = passkeyChecker,
    passkeyLinkerFactory = passkeyLinkerFactory,
    passkeyQueries = service.passkeyQueries,
  )

  fun authenticate(passkey: FakePasskey) = authenticatePasskeyAction().authenticate(
    request = AuthenticatePasskeyRequest(
      authentication = passkey.authentication(
        challenge = challenger.create(),
        origin = baseUrl.toString(),
      ),
    ),
  )

  fun createComputerAction() = CreateComputerAction(
    client = clientAuthenticator.get(),
    computerStore = computerStore,
  )

  fun installAppAction() = InstallAppAction(
    client = clientAuthenticator.get(),
    computerStore = computerStore,
  )

  fun createComputer(slug: String): ComputerTester {
    createComputerAction().createComputer(
      request = CreateComputerRequest(
        slug = slug,
      ),
    )

    return ComputerTester(
      slug = slug,
    )
  }
}

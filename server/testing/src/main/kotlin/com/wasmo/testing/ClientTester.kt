package com.wasmo.testing

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.ConfirmEmailAddressAction
import com.wasmo.accounts.LinkEmailAddressAction
import com.wasmo.api.CreateComputerRequest
import com.wasmo.computers.ComputerStore
import com.wasmo.computers.CreateComputerAction
import com.wasmo.computers.InstallAppAction

class ClientTester(
  val clientAuthenticatorFactory: ClientAuthenticator.Factory,
  val computerStore: ComputerStore,
) {
  val userAgent = FakeUserAgent()

  val clientAuthenticator: ClientAuthenticator
    get() = clientAuthenticatorFactory.create(userAgent)
      .also { it.updateSessionCookie() }

  fun linkEmailAddressAction() = LinkEmailAddressAction(
    client = clientAuthenticator.get(),
  )

  fun confirmEmailAddressAction() = ConfirmEmailAddressAction(
    client = clientAuthenticator.get(),
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

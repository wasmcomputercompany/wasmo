package com.wasmo.ktor

import com.wasmo.accounts.AccountsActions
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.passkeys.PasskeyActions
import com.wasmo.computers.ComputersActions
import com.wasmo.emails.EmailsActions
import com.wasmo.framework.UserAgent
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstalledAppActions
import com.wasmo.stripe.StripeActions
import com.wasmo.website.WebsiteActions
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class NewCallGraphFactory(
  private val clientAuthenticatorFactory: ClientAuthenticator.Factory,
  private val callGraphFactory: CallGraph.Factory,
) :
  AccountsActions.Factory,
  ComputersActions.Factory,
  EmailsActions.Factory,
  InstalledAppActions.Factory,
  PasskeyActions.Factory,
  StripeActions.Factory,
  WebsiteActions.Factory {
  override fun create(userAgent: UserAgent): CallGraph {
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    clientAuthenticator.updateSessionCookie()
    return callGraphFactory.create(clientAuthenticator.get())
  }
}

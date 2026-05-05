package com.wasmo.emails

import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class EmailsActionSource(
  private val emailsActionsFactory: EmailsActions.Factory,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 0

  context(binder: Binder)
  override fun bindActions() {
    binder.register(
      ActionRegistration.Rpc<ConfirmEmailAddressRequest, ConfirmEmailAddressResponse>(
        HttpRequestPattern(
          host = hostnamePatterns.osHostname,
          path = "/confirm-email-address",
        ),
      ) { userAgent, request, _ ->
        val action = emailsActionsFactory.create(userAgent).confirmEmailAddressRpc
        action.confirm(request)
      },
    )

    binder.register(
      ActionRegistration.Rpc<LinkEmailAddressRequest, LinkEmailAddressResponse>(
        HttpRequestPattern(
          host = hostnamePatterns.osHostname,
          path = "/link-email-address",
        ),
      ) { userAgent, request, _ ->
        val action = emailsActionsFactory.create(userAgent).linkEmailAddressRpc
        action.link(request)
      },
    )
  }
}

package com.wasmo.emails

import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
object EmailsActionSource {
  @Provides
  @ElementsIntoSet
  @SingleIn(OsScope::class)
  fun provideActionRegistrations(
    emailsActionsFactory: EmailsActions.Factory,
    hostnamePatterns: HostnamePatterns,
  ): List<ActionRegistration> = listOf(
    ActionRegistration.Rpc<ConfirmEmailAddressRequest, ConfirmEmailAddressResponse>(
      HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/confirm-email-address",
      ),
    ) { userAgent, request, _ ->
      val action = emailsActionsFactory.create(userAgent).confirmEmailAddressRpc
      action.confirm(request)
    },

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

package com.wasmo.emails

import com.wasmo.framework.UserAgent

interface EmailsActions {
  val confirmEmailAddressRpc: ConfirmEmailAddressRpc
  val linkEmailAddressRpc: LinkEmailAddressRpc

  interface Factory {
    fun create(userAgent: UserAgent): EmailsActions
  }
}

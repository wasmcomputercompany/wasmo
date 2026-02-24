package com.wasmo.accounts

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody

interface AppPageFactory {
  fun create(
    accountSnapshot: AccountSnapshot,
    inviteTicket: InviteTicket? = null,
  ): Response<ResponseBody>
}

package com.wasmo.website

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.HostPage
import com.wasmo.api.InviteTicket
import com.wasmo.deployment.Deployment
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody

interface ServerHostPage : HostPage {
  val deployment: Deployment
  val response: Response<ResponseBody>

  interface Factory {
    fun create(
      accountSnapshot: AccountSnapshot,
      inviteTicket: InviteTicket? = null,
      computerSnapshot: ComputerSnapshot? = null,
      computerListSnapshot: ComputerListSnapshot? = null,
    ): ServerHostPage
  }
}

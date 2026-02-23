package com.wasmo.accounts.invite

import com.wasmo.accounts.Client
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.website.AppPageFactory

class InvitePage(
  val appPageFactory: AppPageFactory,
  val client: Client,
) {
  fun invite(code: String): Response<ResponseBody> {
    return appPageFactory.create()
  }
}

package com.wasmo.website.home

import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.website.AppPageFactory

class HomePage(
  private val appPageFactory: AppPageFactory,
) {
  fun get(): Response<ResponseBody> {
    return appPageFactory.create()
  }
}

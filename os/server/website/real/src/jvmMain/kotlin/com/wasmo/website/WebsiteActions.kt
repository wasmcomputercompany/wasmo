package com.wasmo.website

import com.wasmo.framework.UserAgent

interface WebsiteActions {
  val osPage: OsPage

  interface Factory {
    fun create(userAgent: UserAgent): WebsiteActions
  }
}

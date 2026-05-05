package com.wasmo.computers

import com.wasmo.framework.UserAgent

interface StripeActions {
  val afterCheckoutPage: AfterCheckoutPage

  interface Factory {
    fun create(userAgent: UserAgent): StripeActions
  }
}

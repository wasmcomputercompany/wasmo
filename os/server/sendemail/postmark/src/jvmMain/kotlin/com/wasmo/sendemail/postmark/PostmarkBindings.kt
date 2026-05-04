package com.wasmo.sendemail.postmark

import com.wasmo.identifiers.OsScope
import com.wasmo.sendemail.SendEmailService
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
object PostmarkBindings {
  @Provides
  @SingleIn(OsScope::class)
  internal fun provideSendEmailService(
    factory: PostmarkEmailService.Factory,
  ): SendEmailService = factory.create()
}

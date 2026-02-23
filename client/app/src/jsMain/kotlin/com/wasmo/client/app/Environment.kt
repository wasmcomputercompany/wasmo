package com.wasmo.client.app

interface Environment {
  val passkeyUser: String

  /** If non-null, this string will be displayed in a banner across the top of each page. */
  val warningLabel: String?

  val showSignUp: Boolean
}

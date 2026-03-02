package com.wasmo.client.app.computerlist

import com.wasmo.api.ComputerSlug
import com.wasmo.api.routes.Url

data class ComputerListItem(
  val slug: ComputerSlug,
  val url: Url,
)

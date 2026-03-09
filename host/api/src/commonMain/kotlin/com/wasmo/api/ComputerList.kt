package com.wasmo.api

import com.wasmo.identifiers.ComputerSlug
import kotlinx.serialization.Serializable

@Serializable
data class ComputerListSnapshot(
  val items: List<ComputerListItem> = listOf(),
)

@Serializable
data class ComputerListItem(
  val slug: ComputerSlug,
)

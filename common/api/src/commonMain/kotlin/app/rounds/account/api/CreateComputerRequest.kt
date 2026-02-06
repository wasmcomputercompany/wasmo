package app.rounds.account.api

import kotlinx.serialization.Serializable

@Serializable
data class CreateComputerRequest(
  val slug: String,
)


@Serializable
data class CreateComputerResponse(
  val url: String,
)

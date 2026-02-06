package app.rounds.account.actions

import app.rounds.account.api.CreateComputerRequest
import app.rounds.account.api.CreateComputerResponse
import app.rounds.framework.Response

class CreateComputerAction() {
  fun createComputer(
    request: CreateComputerRequest,
  ): Response<CreateComputerResponse> {
    return Response(
      body = CreateComputerResponse(url = ""),
    )
  }
}

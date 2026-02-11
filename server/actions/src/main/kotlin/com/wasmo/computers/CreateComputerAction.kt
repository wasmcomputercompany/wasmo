package com.wasmo.computers

import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.CreateComputerResponse
import com.wasmo.framework.Response

class CreateComputerAction(
  private val computerStore: ComputerStore,
) {
  fun createComputer(
    request: CreateComputerRequest,
  ): Response<CreateComputerResponse> {
    val computer = computerStore.create(
      slug = request.slug,
    )

    return Response(
      body = CreateComputerResponse(
        url = computer.url.toString(),
      ),
    )
  }
}

package com.publicobject.wasmcomputer.computer.actions

import com.publicobject.wasmcomputer.account.api.CreateComputerRequest
import com.publicobject.wasmcomputer.account.api.CreateComputerResponse
import com.publicobject.wasmcomputer.framework.Response

class CreateComputerAction() {
  fun createComputer(
    request: CreateComputerRequest,
  ): Response<CreateComputerResponse> {
    return Response(
      body = CreateComputerResponse(url = ""),
    )
  }
}

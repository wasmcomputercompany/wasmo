package com.publicobject.wasmcomputer.computer.actions

import com.publicobject.wasmcomputer.api.CreateComputerRequest
import com.publicobject.wasmcomputer.api.CreateComputerResponse
import com.publicobject.wasmcomputer.app.db.WasmComputerDbService
import com.publicobject.wasmcomputer.framework.Response
import kotlin.time.Clock

class CreateComputerAction(
  private val clock: Clock,
  private val service: WasmComputerDbService,
) {
  fun createComputer(
    request: CreateComputerRequest,
  ): Response<CreateComputerResponse> {
    return service.transactionWithResult(noEnclosing = true) {
      service.computerQueries.insertComputer(
        created_at = clock.now(),
        slug = request.slug,
      ).executeAsOne()

      Response(
        body = CreateComputerResponse(
          url = "/computer/${request.slug}",
        ),
      )
    }
  }
}

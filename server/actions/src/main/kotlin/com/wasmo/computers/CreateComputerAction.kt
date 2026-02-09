package com.wasmo.computers

import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.CreateComputerResponse
import com.wasmo.app.db.WasmoDbService
import com.wasmo.framework.Response
import kotlin.time.Clock

class CreateComputerAction(
  private val clock: Clock,
  private val service: WasmoDbService,
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

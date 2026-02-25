package com.wasmo.hello.server

import com.wasmo.hello.api.GreetRequest
import com.wasmo.hello.api.GreetResponse
import com.wasmo.hello.db.HelloDbService
import kotlin.time.Clock

class GreetAction(
  private val clock: Clock,
  private val helloDbService: HelloDbService,
) {
  suspend fun greet(
    request: GreetRequest,
  ): GreetResponse {
    helloDbService.personQueries.insertPerson(
      created_at = clock.now(),
      name = request.name,
    )

    val recentPersons = helloDbService.personQueries.selectRecentPersons(
      limit = 10L,
    ).executeAsList()

    return GreetResponse(
      recentNames = recentPersons.map { it.name },
    )
  }
}

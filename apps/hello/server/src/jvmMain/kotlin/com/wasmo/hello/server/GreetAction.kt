package com.wasmo.hello.server

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.wasmo.hello.api.GreetRequest
import com.wasmo.hello.api.GreetResponse
import com.wasmo.hello.db.HelloDb
import kotlin.time.Clock

class GreetAction(
  private val clock: Clock,
  private val helloDb: HelloDb,
) {
  suspend fun greet(
    request: GreetRequest,
  ): GreetResponse {
    helloDb.personQueries.insertPerson(
      created_at = clock.now(),
      name = request.name,
    )

    val recentPersons = helloDb.personQueries.selectRecentPersons(
      limit = 10L,
    ).awaitAsList()

    return GreetResponse(
      recentNames = recentPersons.map { it.name },
    )
  }
}

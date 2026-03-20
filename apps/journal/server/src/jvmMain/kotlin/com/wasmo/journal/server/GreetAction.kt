package com.wasmo.journal.server

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.wasmo.journal.api.GreetRequest
import com.wasmo.journal.api.GreetResponse
import com.wasmo.journal.db.JournalDb
import kotlin.time.Clock

class GreetAction(
  private val clock: Clock,
  private val journalDb: JournalDb,
) {
  suspend fun greet(
    request: GreetRequest,
  ): GreetResponse {
    journalDb.personQueries.insertPerson(
      created_at = clock.now(),
      name = request.name,
    )

    val recentPersons = journalDb.personQueries.selectRecentPersons(
      limit = 10L,
    ).awaitAsList()

    return GreetResponse(
      recentNames = recentPersons.map { it.name },
    )
  }
}

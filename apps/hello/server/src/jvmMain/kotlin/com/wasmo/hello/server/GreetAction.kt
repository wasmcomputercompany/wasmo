package com.wasmo.hello.server

import com.wasmo.hello.api.GreetRequest
import com.wasmo.hello.api.GreetResponse
import kotlin.time.Clock
import wasmo.sql.SqlDatabase

class GreetAction(
  private val clock: Clock,
  private val sqlDatabase: SqlDatabase,
) {
  suspend fun greet(
    request: GreetRequest,
  ): GreetResponse {
    sqlDatabase.newConnection().use { connection ->
      connection.execute(
        sql =
          """
          |INSERT INTO Person(
          |  created_at,
          |  name
          |)
          |VALUES (
          |  ?,
          |  ?
          |);
          """.trimMargin(),
        bindParameters = {
          bindString(0, clock.now().toString())
          bindString(1, request.name)
        },
      )

      val namesIterator = connection.executeQuery(
        sql = """
          |SELECT name FROM Person
          |ORDER BY created_at DESC
          |LIMIT ?;
          """.trimMargin(),
        bindParameters = {
          bindLong(0, 10L)
        },
      )

      val recentNames = buildList {
        while (true) {
          val row = namesIterator.next() ?: break
          val name = row.getString(0) ?: continue
          add(name)
        }
      }

      return GreetResponse(
        recentNames = recentNames,
      )
    }
  }
}

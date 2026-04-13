package com.wasmo.sql

import wasmo.sql.SqlConnection

suspend fun SqlConnection.createTableBalances() {
  execute(
    """
    CREATE TABLE Balances (
      id TEXT,
      amount BIGINT
    )
    """,
  )
}

suspend fun SqlConnection.insertBalances(vararg balances: Balance) {
  for (balance in balances) {
    execute("""INSERT INTO Balances (id, amount) VALUES ($1, $2)""") {
      bindString(0, balance.id)
      bindS64(1, balance.amount)
    }
  }
}

suspend fun SqlConnection.updateBalances(vararg balances: Balance) {
  for (balance in balances) {
    val rowCount = execute("""UPDATE Balances SET amount = $1 WHERE id = $2""") {
      bindString(1, balance.id)
      bindS64(0, balance.amount)
    }
    check(rowCount == 1L) { "expected 1 row updated for ${balance.id}" }
  }
}

suspend fun SqlConnection.allBalances(): List<Balance> {
  return buildList {
    executeQuery("""SELECT id, amount FROM Balances""").use { rowIterator ->
      while (true) {
        val row = rowIterator.next() ?: break
        add(
          Balance(
            id = row.getString(0)!!,
            amount = row.getS64(1)!!,
          ),
        )
      }
    }
  }
}

data class Balance(
  val id: String,
  val amount: Long,
)

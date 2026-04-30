SQL Service
===========

We offer a mechanism to create and access [PostgreSQL] databases.


Beware of Connection Pooling
----------------------------

Note that our database connections are pooled, and this pooling can eaks connection state between
SQL calls.

For example, the following two calls _look_ independent, but unfortunately they are not independent.

```kotlin
sqlDatabase.newConnection().use { connection ->
  connection.execute("SET TIME ZONE 'America/Toronto'")
}

sqlDatabase.newConnection().use { connection ->
  connection.executeQuery("SELECT current_setting('TIMEZONE')").use { rowIterator ->
    val row = rowIterator.next()!!
    assertThat(row.getString(0)).isEqualTo("Etc/UTC")
  }
}
```


[PostgreSQL]: https://www.postgresql.org/

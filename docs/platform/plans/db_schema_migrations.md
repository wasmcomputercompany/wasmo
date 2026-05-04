DB schema migrations
--------------------

We'd like database creation and migrations to happen automatically.

 - We don't support schema downgrade. Don't wipe automatically, but throw an exception.
 - We already have `Migrate.kt`, but need the current schema version.

### Plan for now

 - Schema version is tracked in a single-row table, so it can be updated transactionally as part of migrations.
 - `Migrate.kt` gains a method `ensureSchemaVersion(targetVersion=CURRENT_SCHEMA_VERSION)` which:
   1. First, runs the following SQL unconditionally (idempotent, establishes schema version 0 if the db is empty):
```sql
CREATE TABLE IF NOT EXISTS DatabaseSchemaVersion (
  id INTEGER NOT NULL PRIMARY KEY DEFAULT 1,
  version INTEGER NOT NULL
);

ALTER TABLE DatabaseSchemaVersion DROP CONSTRAINT IF EXISTS only_one_row;

ALTER TABLE DatabaseSchemaVersion ADD CONSTRAINT only_one_row CHECK (
  id = 1
);

INSERT INTO DatabaseSchemaVersion (version)
VALUES (0)
ON CONFLICT (id) DO NOTHING;
```
   2. Then,
     - If `version` from the table `< CURRENT_SCHEMA_VERSION`, run `migrate(oldVersion=version, newVersion=CURRENT_SCHEMA_VERSION)`.
     - If `version > CURRENT_SCHEMA_VERSION`, wipe the database (drop, recreate DatabaseSchemaVersion table at version 0, run migrations).
     - Else, do nothing.
 - `WasmoService.startWasmoService()` calls `ensureSchemaVersion(CURRENT_SCHEMA_VERSION)` right after
   obtaining `wasmoDb` ([code](https://github.com/wasmcomputercompany/wasmo/blob/8c0da2da837a94fe5f7c66640eb51ca2f8dc5140/os/server/ktor/src/jvmMain/kotlin/com/wasmo/ktor/WasmoService.kt#L60)).

### Future options

 - The duplication between migrations, `DbFoo` and `FooQueries.kt` is error prone (e.g. typos are only found at runtime). It'd be nice to have a Kotlin DSL or similar to type less and to find more errors at compile time.
 - We could use a library, e.g. [Exposed](https://www.jetbrains.com/help/exposed/get-started-with-exposed.html#define-table-object).


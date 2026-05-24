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
      - If `version > CURRENT_SCHEMA_VERSION`, throw `IllegalStateException`.
      - Else, do nothing.
 - `WasmoService.startWasmoService()` calls `ensureSchemaVersion(CURRENT_SCHEMA_VERSION)` right after
   obtaining `wasmoDb` ([code](https://github.com/wasmcomputercompany/wasmo/blob/8c0da2da837a94fe5f7c66640eb51ca2f8dc5140/os/server/ktor/src/jvmMain/kotlin/com/wasmo/ktor/WasmoService.kt#L60)).

### Absurd schema upgrades

absurd is installed as part of the Wasmo DB as part of the automatic schema migrations.

 - Wasmo schema DB version 2 includes absurd schema version 0.3.0 (like `absurdctl init --ref 0.3.0`)
 - Wasmo schema DB version 3 includes an absurd queue named `default` (like `absurdctl create-queue default`)

For future upgrades, we'll have to generate Wasmo DB schema migration SQL through a command like
the below, but that requires our PRs to land in absurd, first.

```bash
export PGDATABASE="postgresql://postgres:password@localhost:5432/wasmo_homelab"
TARGET_WASMO_DB_SCHEMA_VERSION=3
absurdctl migrate --dump-sql > os/server/db/src/jvmMain/resources/migrations/v${TARGET_WASMO_DB_SCHEMA_VERSION}__absurd-$(absurdctl schema-version --target).sql
```

### Future options

 - The duplication between migrations, `DbFoo` and `FooQueries.kt` is error prone (e.g. typos are only found at runtime). It'd be nice to have a Kotlin DSL or similar to type less and to find more errors at compile time.
 - We could use a library, e.g. [Exposed](https://www.jetbrains.com/help/exposed/get-started-with-exposed.html#define-table-object).


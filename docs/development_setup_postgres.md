Development Setup: Postgres
===========================

Create the database:

```bash
$ docker pull postgres:18.1
$ docker container create \
  --name wasmcomputer-postgres-db \
  --env POSTGRES_PASSWORD=password \
  --publish 5432:5432 \
  postgres:18.1
```

Run the database server:

```bash
$ docker container start wasmcomputer-postgres-db
```

Set up the Postgres CLI:

```bash
$ brew install libpq
```

Drop the local database:

```bash
$ export PGPASSWORD=password
$ psql "host=localhost user=postgres" \
  -c "DROP DATABASE wasmcomputer_development"
```

Create local databases:

```bash
$ export PGPASSWORD=password
$ psql "host=localhost user=postgres" \
  -c "CREATE DATABASE wasmcomputer_development WITH ENCODING = 'UTF8'"
$ psql "host=localhost user=postgres" \
  -c "CREATE DATABASE wasmcomputer_test WITH ENCODING = 'UTF8'"
```

Build migrations `.sql` files:

```bash
$ ../gradlew --project-dir .. server:db:generateMainWasmComputerDbMigrations
```

Run all migrations:

```bash
$ export PGPASSWORD=password
$ find ../server/db/build/resources/main/migrations \
  -name '*.sql' \
  | sort --version-sort \
  | xargs -n 1 \
  psql "host=localhost dbname=wasmcomputer_development user=postgres" -a -f
```

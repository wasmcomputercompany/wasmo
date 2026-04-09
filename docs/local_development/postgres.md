Local Development Postgres
==========================

Create the database:

```bash
$ docker pull postgres:18.1
$ docker container create \
  --name wasmo-postgres-db \
  --env POSTGRES_PASSWORD=password \
  --publish 5432:5432 \
  postgres:18.1
```

Run the database server:

```bash
$ docker container start wasmo-postgres-db
```

Set up the Postgres CLI:

```bash
$ brew install libpq
```

Drop the local database:

```bash
$ export PGPASSWORD=password
$ psql "host=localhost user=postgres" \
  -c "DROP DATABASE wasmo_development"
```

Create local databases:

```bash
$ export PGPASSWORD=password
$ psql "host=localhost user=postgres" \
  -c "CREATE DATABASE wasmo_development WITH ENCODING = 'UTF8'"
$ psql "host=localhost user=postgres" \
  -c "CREATE DATABASE wasmo_test WITH ENCODING = 'UTF8'"
$ psql "host=localhost user=postgres" \
  -c "CREATE DATABASE absurd_test WITH ENCODING = 'UTF8'"
$ psql "host=localhost user=postgres" \
  -c "CREATE DATABASE journal_test WITH ENCODING = 'UTF8'"
```

Build migrations `.sql` files:

```bash
$ cd ../..
$ ./gradlew :os:server:db:generateMainWasmoDbMigrations
```

Run all migrations:

```bash
$ cd ../..
$ export PGPASSWORD=password
$ find ./os/server/db/build/resources/main/migrations \
  -name '*.sql' \
  | sort --version-sort \
  | xargs -n 1 \
  psql "host=localhost dbname=wasmo_development user=postgres" -a -f
```

Database Migrations
-------------------

Run an individual migration:

```bash
$ cd ../..
$ ./gradlew :os:server:db:generateMainWasmoDbMigrations
```

Replace _XXX_ with the migration number.

```bash
$ cd ../..
$ export PGPASSWORD=password
$ psql "host=localhost dbname=wasmo_development user=postgres" -a -f \
     ./os/server/db/build/resources/main/migrations/vXXX__db.sql
```

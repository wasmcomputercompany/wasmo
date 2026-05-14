Local Development PostgreSQL
============================

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

Set up the PostgreSQL CLI:

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
```


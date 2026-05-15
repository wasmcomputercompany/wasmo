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
  -c "CREATE USER wasmo_development WITH PASSWORD 'password' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT"
$ psql "host=localhost user=postgres" \
  -c "CREATE DATABASE wasmo_development WITH ENCODING = 'UTF8'"
$ psql "host=localhost user=postgres" \
  -c "GRANT ALL PRIVILEGES ON DATABASE wasmo_development TO wasmo_development"
$ psql "host=localhost user=postgres" \
  -c "ALTER DATABASE wasmo_development OWNER TO wasmo_development"
```

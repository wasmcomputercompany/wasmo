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
$ echo 'export PATH="/usr/local/opt/libpq/bin:$PATH"' >> ~/.zshrc
$ source ~/.zshrc
```

And create your local databases:

```bash
$ export PGPASSWORD=password
$ psql "host=localhost user=postgres" \
  -c "CREATE USER wasmo_homelab WITH PASSWORD 'password' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT"
$ psql "host=localhost user=postgres" \
  -c "CREATE DATABASE wasmo_homelab WITH ENCODING = 'UTF8'"
$ psql "host=localhost user=postgres" \
  -c "GRANT ALL PRIVILEGES ON DATABASE wasmo_homelab TO wasmo_homelab"
$ psql "host=localhost user=postgres" \
  -c "ALTER DATABASE wasmo_homelab OWNER TO wasmo_homelab"
```

💀Factory Reset
----------------

To delete everything and start from scratch:

```bash
$ docker container stop wasmo-postgres-db
$ docker container rm wasmo-postgres-db
```

You’ll need to follow the steps above to bring it back online.

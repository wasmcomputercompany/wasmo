Postgres Runbook
================

Our databases are on Planetscale.

wasmo.dev
---------

 * [PlanetScale admin](https://app.planetscale.com/jesse-wasmo/wasmo-dev)

Connect with danger:

```bash
$ export PGPASSWORD=$WASMO_DEV_PGPASSWORD
$ psql "sslmode=require host=gcp-northamerica-northeast1-1.pg.psdb.cloud dbname=wasmo_dev user=pscale_api_uh85t8q0waqt.hkqtmgf3pdzi"
```

Create migrations:

```bash
$ ../gradlew --project-dir .. :host:server:db:generateMainWasmoDbMigrations
```

To execute an individual migration, replace _XXX_ with the migration number.

```bash
$ export PGPASSWORD=$WASMO_DEV_PGPASSWORD
$ psql "sslmode=require host=gcp-northamerica-northeast1-1.pg.psdb.cloud dbname=wasmo_dev user=pscale_api_uh85t8q0waqt.hkqtmgf3pdzi" -a -f \
     ../host/server/db/build/resources/main/migrations/vXXX__db.sql
```


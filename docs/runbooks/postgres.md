Postgres
========

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
$ cd ../..
$ ./gradlew :host:server:db:generateMainWasmoDbMigrations
```

To execute an individual migration, replace _XXX_ with the migration number.

```bash
$ cd ../..
$ export PGPASSWORD=$WASMO_DEV_PGPASSWORD
$ psql "sslmode=require host=gcp-northamerica-northeast1-1.pg.psdb.cloud dbname=wasmo_dev user=pscale_api_uh85t8q0waqt.hkqtmgf3pdzi" -a -f \
     ./host/server/db/build/resources/main/migrations/vXXX__db.sql
```

wasmo.com
---------

 * [PlanetScale admin](https://app.planetscale.com/jesse-wasmo/wasmo-com)

Connect with danger:

```bash
$ export PGPASSWORD=`pbpaste`
$ psql "sslmode=require host=gcp-northamerica-northeast1-1.pg.psdb.cloud dbname=wasmo_com user=pscale_api_eu3kxhe4lp41.7q408njs9kb7"
```

Create migrations:

```bash
$ cd ../..
$ ./gradlew :host:server:db:generateMainWasmoDbMigrations
```

To execute an individual migration, replace _XXX_ with the migration number.

```bash
$ cd ../..
$ export PGPASSWORD=`pbpaste`
$ psql "sslmode=require host=gcp-northamerica-northeast1-1.pg.psdb.cloud dbname=wasmo_com user=pscale_api_eu3kxhe4lp41.7q408njs9kb7" -a -f \
     ./host/server/db/build/resources/main/migrations/vXXX__db.sql
```

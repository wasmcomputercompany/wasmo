Homelab
=======

This is our distribution for running Wasmo on your own hardware.

Volumes
-------

Typically you'll mount `~/Library/Wasmo` on the host to `/wasmo` in the container.

Here's some interesting file paths within the container:

| Directory                   | What it's for                                                        |
|:----------------------------|:---------------------------------------------------------------------|
| `/homelab-foundation`       | Static configuration files                                           |
| ` '- postgresql.conf`       | Homelab's standard Postgresql config                                 |
| `/usr`                      |                                                                      |
| ` '- bin`                   |                                                                      |
| `     '- gosu`              | The [gosu] program, like `su`                                        |
| ` '- libexec`               |                                                                      |
| `     '- postgresql18`      |                                                                      |
| `         '- initdb`        | Executable to initialize a database directory                        |
| `         '- postgres`      | Executable to run the database daemon                                |
| `/wasmo`                    | All OS-managed data                                                  |
| ` '- objectstore`           | Object store                                                         |
| ` '- postgresql`            | Postgresql databases (all versions)                                  |
| `     '- 18`                | Postgresql v18.x                                                     |
| `         '- pg_hba.conf`   | Postgres authorized users. This file is created by `initdb`.         |
| `         '- pg_ident.conf` | Maps host users to database users. This file is created by `initdb`. |

Build
-----

Build the foundation image.

```bash
$ cd ../..
$ export DOCKER_DEFAULT_PLATFORM=linux/amd64
$ cd os/distributions/homelab/src/main/foundation
$ docker build -t wasmo/homelab-foundation .
```

Build Homelab upon it:

```bash
$ cd ../..
$ ./gradlew os:distributions:homelab:jibDockerBuild
```

Run
---

```bash
$ export WASMO_DATA=$HOME/.wasmo/homelab
$ mkdir -p $WASMO_DATA
$ docker run \
  --publish 5432:5432 \
  --publish 8080:8080 \
  --mount type=bind,src=$WASMO_DATA,dst=/wasmo \
  wasmo/homelab \
  --container
```

Implementation
--------------

We use [Jib] to build Docker images.

We use [gosu] to execute our Postgresql server as the postgres user in the container.


[Jib]: https://github.com/GoogleContainerTools/jib/

[gosu]: https://github.com/tianon/gosu/

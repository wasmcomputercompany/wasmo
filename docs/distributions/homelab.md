Homelab
=======

This is our distribution for running Wasmo on your own hardware.

We build for `linux/amd64` and `linux/arm64`.

Volumes
-------

Typically you'll mount `~/.wasmo` on the host to `/wasmo` in the container.

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

Build the foundation image for the host's architecture.

```bash
$ cd ../..
$ docker buildx build \
  --tag wasmo/homelab-foundation \
  os/distributions/homelab/src/main/foundation
```

Build Homelab upon it:

```bash
$ cd ../..
$ ./gradlew os:distributions:homelab:jibDockerBuild -Pdocker.platforms=local
```

Run
---

```bash
$ export WASMO_DATA=$HOME/.wasmo/homelab
$ mkdir -p $WASMO_DATA
$ docker run \
  --publish 54400:54400 \
  --mount type=bind,src=$WASMO_DATA,dst=/wasmo \
  wasmo/homelab
```

Debug
-----

This is the same as above, but it dangerously exposes the JVM debugger and Postgresql ports:

```bash
$ export WASMO_DATA=$HOME/.wasmo/homelab
$ mkdir -p $WASMO_DATA
$ docker run \
  --publish 54400:54400 \
  --publish 54401:54401 \
  --publish 54402:54402 \
  --mount type=bind,src=$WASMO_DATA,dst=/wasmo \
  wasmo/homelab
```

Publish to Docker Hub
---------------------

We can't use Docker Desktop for [multi-platform Docker builds]. Instead, we publish each build step
to Docker Hub.

```bash
$ cd ../..
$ docker buildx build \
  --platform linux/amd64,linux/arm64 \
  --tag wasmo/homelab-foundation \
  os/distributions/homelab/src/main/foundation
$ ./gradlew os:distributions:homelab:jib
```

Implementation
--------------

We use [Jib] to build Docker images.

We use [gosu] to execute our Postgresql server as the `postgres` user in the container.


[Jib]: https://github.com/GoogleContainerTools/jib/

[gosu]: https://github.com/tianon/gosu/

[multi-platform Docker builds]: https://docs.docker.com/build/building/multi-platform/

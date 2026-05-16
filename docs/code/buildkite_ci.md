CI
==

We do CI on [Buildkite].

Docker
------

Our CI image prepares [Docker Compose] with Postgresql in one container, and everything else in
another:

* JDK
* Gradle
* NodeJS: to run Kotlin/JS tests
* Chrome: to run Kotlin/JS tests

See [wasmo/ci image Dockerfile](../../wasmo-build/ci/Dockerfile).


Building & Publishing Images
----------------------------

CI needs `amd64` images even though MacBooks run `aarch64`.

```bash
$ export DOCKER_DEFAULT_PLATFORM=linux/amd64
$ cd ../../wasmo-build/ci
$ docker build -t wasmo/ci .
$ docker push wasmo/ci
```


Local Execution
---------------

Rebuild the container if necessary.

```bash
$ cd ../..
$ cd wasmo-build/ci
$ docker build -t wasmo/ci .
$ docker push wasmo/ci
```

Run a command as it runs in the Buildkite Docker Compose.

```bash
$ cd ../..
$ docker-compose \
  -f .buildkite/docker-compose.yml \
  run ci-build \
  gradle :jvmTest
```

Consider keeping a separate clone of the Wasmo repo for doing Linux x64 builds. Our Gradle plugins
get grumpy if the architecture changes between builds.


[Buildkite]: https://buildkite.com/wasmo/
[Docker Compose]: https://buildkite.com/resources/plugins/buildkite-plugins/docker-compose-buildkite-plugin/

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
* Rust: our reference platform for WebAssembly

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

After pushing an image, update the SHA256 in [docker-compose.yml](../../.buildkite/docker-compose.yml)
so future builds will use this new image.


Local Execution
---------------

Rebuild the container if necessary.

```bash
$ cd ../..
$ cd wasmo-build/ci
$ docker build -t wasmo/ci .
$ docker push wasmo/ci
```

Remove the pinned sha256 from `.buildkite/docker-compose.yml`:

```diff
-image: wasmo/ci@sha256:fffcd3e123de5ca944aa7d1f0551d371991f5b8cb8deb41a484741ce0eeb8e48
+image: wasmo/ci
```

Next run the build:

```bash
$ cd ../..
$ docker-compose \
  -f .buildkite/docker-compose.yml \
  run ci-build \
  gradle :jvmTest -Pwasmo.build.environment=ci
```

Consider keeping a separate clone of the Wasmo repo for doing Linux x64 builds. Our Gradle plugins
get grumpy if the architecture changes between builds.


[Buildkite]: https://buildkite.com/wasmo/
[Docker Compose]: https://buildkite.com/resources/plugins/buildkite-plugins/docker-compose-buildkite-plugin/

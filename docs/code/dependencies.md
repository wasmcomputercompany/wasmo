Dependencies
============

If there's a discrepancy between another file and here, that's a bug.

If a version is hard-coded in a configuration file somewhere in this repo, please link that file
from here so we can keep it all in sync.


Kotlin Dependencies
-------------------

We don't list these independently.

 * [Versions TOML](../../gradle/libs.versions.toml)


Submodule Dependencies
----------------------

 * [Pico](../../submodules/pico)


[Alpine Linux 3.2.3](https://alpinelinux.org/releases/), 2025-12-03
-------------------------------------------------------------------

We use Alpine Linux as our Docker base image.

### References:

* [CI Dockerfile](../../wasmo-build/ci/Dockerfile)
* [Homelab Dockerfile](../../os/distributions/homelab/src/main/foundation/Dockerfile)


[Gradle 9.5.1](https://gradle.org/releases/), 2026-05-12
--------------------------------------------------------

Run this to update Gradle-managed files including the binary `gradle-wrapper.jar` file:

```bash
$ cd ../..
$ ./gradlew wrapper --gradle-version 9.5.1
```

### References:

* [CI Dockerfile](../../wasmo-build/ci/Dockerfile)
* [Gradle Wrapper](../../gradle/wrapper/gradle-wrapper.properties)


[NodeJS 26.1.0](https://nodejs.org/en/blog/release), 2025-05-07
---------------------------------------------------------------

We do not use NodeJS at runtime, but we use it to build and test Kotlin/JS in CI.

### References:

* [CI Dockerfile](../../wasmo-build/ci/Dockerfile)
* [Gradle NodeJsPlugin](../../build.gradle.kts)


[OpenJDK (Eclipse Temurin) 25.0.3](https://adoptium.net/temurin/releases/), 2026-04-21
--------------------------------------------------------------------------------------

This is listed in our Docker base image.

```bash
export JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/temurin-$VERSION/Contents/Home/"
```

### References:

* [CI Dockerfile](../../wasmo-build/ci/Dockerfile)
* [Homelab Dockerfile](../../os/distributions/homelab/src/main/foundation/Dockerfile)


[Postgresql 18.1](https://www.postgresql.org/docs/release/), 2025-11-13
-----------------------------------------------------------------------

### References:

* [CI Docker Compose](../../.buildkite/docker-compose.yml)
* [Local Development Docs](../../docs/local_development/postgresql.md)

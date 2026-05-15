# Wasmo CI
#
# This file is derived from Gradle's Dockerfile
# Copyright 2025 Gradle Inc.
# https://www.apache.org/licenses/LICENSE-2.0
# See https://github.com/gradle/docker-gradle/blob/master/jdk21-alpine/Dockerfile

FROM eclipse-temurin:21-jdk-alpine

CMD ["gradle"]

ENV GRADLE_HOME=/opt/gradle

RUN set -o errexit -o nounset \
    && echo "Adding gradle user and group" \
    && addgroup --system --gid 1000 gradle \
    && adduser --system --ingroup gradle --uid 1000 --shell /bin/ash gradle \
    && mkdir /home/gradle/.gradle \
    && chown -R gradle:gradle /home/gradle \
    && chmod -R o+rwx /home/gradle \
    \
    && echo "Symlinking root Gradle cache to gradle Gradle cache" \
    && ln -s /home/gradle/.gradle /root/.gradle

VOLUME /home/gradle/.gradle

WORKDIR /home/gradle

RUN set -o errexit -o nounset \
    && apk add --no-cache \
      # common utilities
      make \
      curl \
      tar \
      xz \
      \
    && echo "Testing common utilities" \
    && which awk \
    && which curl \
    && which cut \
    && which grep \
    && which gunzip \
    && which sha256sum \
    && which sed \
    && which tar \
    && which tr \
    && which unzip

ENV GRADLE_VERSION=9.5.1
ARG GRADLE_DOWNLOAD_SHA256=bafc141b619ad6350fd975fc903156dd5c151998cc8b058e8c1044ab5f7b031f
RUN \
    --mount=type=cache,target=/downloads,rw \
    set -o errexit -o nounset \
    && if [ ! -e "/downloads/gradle-$GRADLE_VERSION-bin.zip" ]; then \
          echo "Downloading Gradle $GRADLE_VERSION" ; \
          curl --fail --silent --show-error --location --output "/downloads/gradle-$GRADLE_VERSION-bin.zip" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" ; \
       fi \
    && echo "Checking Gradle download hash" \
    && echo "$GRADLE_DOWNLOAD_SHA256 /downloads/gradle-$GRADLE_VERSION-bin.zip" | sha256sum -c - \
    \
    && echo "Installing Gradle" \
    && unzip "/downloads/gradle-$GRADLE_VERSION-bin.zip" \
    && mv "gradle-$GRADLE_VERSION" "$GRADLE_HOME/" \
    && ln -s "$GRADLE_HOME/bin/gradle" /usr/bin/gradle

USER gradle

RUN set -o errexit -o nounset \
    && echo "Testing Gradle installation" \
    && gradle --stacktrace --debug --version

USER root

# Install Node.
# Version must match 'build.gradle.kts'
ENV NODE_VERSION=26.1.0
RUN \
    --mount=type=cache,target=/downloads,rw \
    alpineArch="$(apk --print-arch)" \
    && case "${alpineArch##*-}" in x86_64) export ARCH='x64';; aarch64) export ARCH='arm64';; *) ;; esac \
    && if [ ! -e "/downloads/node-v$NODE_VERSION-linux-$ARCH-musl.tar.xz" ]; then \
          echo "Downloading node $NODE_VERSION $ARCH" ; \
          curl --fail --silent --show-error --location --output "/downloads/node-v$NODE_VERSION-linux-$ARCH-musl.tar.xz" --compressed "https://unofficial-builds.nodejs.org/download/release/v$NODE_VERSION/node-v$NODE_VERSION-linux-$ARCH-musl.tar.xz" ; \
       fi \
    && tar -xJf "/downloads/node-v$NODE_VERSION-linux-$ARCH-musl.tar.xz" -C /usr/local --strip-components=1 --no-same-owner \
    && ln -s /usr/local/bin/node /usr/local/bin/nodejs \
    # Remove unused OpenSSL headers to save ~34MB. See this NodeJS issue: https://github.com/nodejs/node/issues/46451
    && find /usr/local/include/node/openssl/archs -mindepth 1 -maxdepth 1 ! -name "$OPENSSL_ARCH" -exec rm -rf {} \; \
    && node --version \
    && npm --version \
    && rm -rf /tmp/*

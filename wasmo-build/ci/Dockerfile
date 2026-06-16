# Wasmo CI
#
# This file includes code from Gradle's Dockerfile
# Copyright 2025 Gradle Inc.
# https://www.apache.org/licenses/LICENSE-2.0
# See https://github.com/gradle/docker-gradle/blob/master/jdk21-alpine/Dockerfile
#
# This file includes code from Node's Dockerfile
# Copyright (c) 2015 Joyent, Inc.
# Copyright (c) 2015 Node.js contributors
# https://github.com/nodejs/docker-node/blob/main/LICENSE
# See https://github.com/nodejs/docker-node/blob/main/26/alpine3.23/Dockerfile
#
# This file includes code from Rust's Dockerfile
# Copyright (c) The Rust Project Contributors.
# https://github.com/rust-lang/docker-rust/blob/master/LICENSE-APACHE
# See https://github.com/rust-lang/docker-rust/blob/master/stable/alpine3.23/Dockerfile

FROM eclipse-temurin:25-alpine-3.23

RUN set -o errexit -o nounset \
    && echo "Installing utilities with apk" \
    && apk add --no-cache \
      make \
      curl \
      tar \
      xz \
      \
    && echo "Testing utilities" \
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

# Rust.
RUN apk add --no-cache \
        ca-certificates \
        musl-dev \
        gcc
ENV RUSTUP_HOME=/usr/local/rustup \
    CARGO_HOME=/usr/local/cargo \
    PATH=/usr/local/cargo/bin:$PATH \
    RUST_VERSION=1.96.0
RUN set -eux; \
    \
    arch="$(apk --print-arch)"; \
    case "$arch" in \
        'x86_64') \
            rustArch='x86_64-unknown-linux-musl'; \
            rustupSha256='9cd3fda5fd293890e36ab271af6a786ee22084b5f6c2b83fd8323cec6f0992c1'; \
            ;; \
        'aarch64') \
            rustArch='aarch64-unknown-linux-musl'; \
            rustupSha256='88761caacddb92cd79b0b1f939f3990ba1997d701a38b3e8dd6746a562f2a759'; \
            ;; \
        'ppc64le') \
            rustArch='powerpc64le-unknown-linux-musl'; \
            rustupSha256='e15d033af90b7a55d170aac2d82cc28ddd96dbfcdda7c6d4eb8cb064a99c4646'; \
            ;; \
        *) \
            echo >&2 "unsupported architecture: $arch"; \
            exit 1; \
            ;; \
    esac; \
    \
    url="https://static.rust-lang.org/rustup/archive/1.29.0/${rustArch}/rustup-init"; \
    wget "$url"; \
    echo "${rustupSha256} *rustup-init" | sha256sum -c -; \
    \
    chmod +x rustup-init; \
    ./rustup-init -y --no-modify-path --profile minimal --default-toolchain $RUST_VERSION --default-host ${rustArch}; \
    rm rustup-init; \
    chmod -R a+w $RUSTUP_HOME $CARGO_HOME; \
    \
    rustup --version; \
    cargo --version; \
    rustc --version
RUN echo "Adding Rust WebAssembly tools" \
    && rustup target add wasm32-wasip2 \
    && cargo install --locked wasm-tools \
    \
    && echo "Seeding the Rust cache" \
    && cd /root/ \
    && cargo new sample_project \
    && cd /root/sample_project \
    && cargo add wit-bindgen \
    && cargo build --target=wasm32-wasip2 --release \
    && rm -rf /root/sample_project

# Gradle.
ENV GRADLE_VERSION=9.5.1
ARG GRADLE_DOWNLOAD_SHA256=bafc141b619ad6350fd975fc903156dd5c151998cc8b058e8c1044ab5f7b031f
ENV GRADLE_HOME=/opt/gradle
VOLUME /home/gradle/.gradle
WORKDIR /home/gradle
RUN --mount=type=cache,target=/downloads,rw \
    set -o errexit -o nounset \
    \
    && echo "Adding gradle user and group" \
    && addgroup --system --gid 1000 gradle \
    && adduser --system --ingroup gradle --uid 1000 --shell /bin/ash gradle \
    && mkdir /home/gradle/.gradle \
    && chown -R gradle:gradle /home/gradle \
    && chmod -R o+rwx /home/gradle \
    \
    && echo "Symlinking root Gradle cache to gradle Gradle cache" \
    && ln -s /home/gradle/.gradle /root/.gradle \
    \
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

# Node.
ENV NODE_VERSION=26.1.0
RUN --mount=type=cache,target=/downloads,rw \
    set -o errexit -o nounset \
    \
    && alpineArch="$(apk --print-arch)" \
    && case "${alpineArch##*-}" in \
        x86_64) \
            export ARCH='x64'; \
            export OPENSSL_ARCH=linux-'x86_64'; \
            export NODE_DOWNLOAD_SHA256='e89b5c628c7058673c6c3b363f8d2219e211cd37dd4cb9204b4208110d7bbe78'; \
            ;; \
        aarch64) \
            export ARCH='arm64'; \
            export OPENSSL_ARCH='linux-aarch64'; \
            export NODE_DOWNLOAD_SHA256='b8504211d22d83f35764536482a782930ab09c2a1dddda6229bfa9057b103371'; \
            ;; \
        *) ;; \
        esac \
    && if [ ! -e "/downloads/node-v$NODE_VERSION-linux-$ARCH-musl.tar.xz" ]; then \
          echo "Downloading node $NODE_VERSION $ARCH" ; \
          curl --fail --silent --show-error --location --output "/downloads/node-v$NODE_VERSION-linux-$ARCH-musl.tar.xz" --compressed "https://unofficial-builds.nodejs.org/download/release/v$NODE_VERSION/node-v$NODE_VERSION-linux-$ARCH-musl.tar.xz" ; \
       fi \
    && echo "Checking Node download hash" \
    && echo "$NODE_DOWNLOAD_SHA256 /downloads/node-v$NODE_VERSION-linux-$ARCH-musl.tar.xz" | sha256sum -c - \
    \
    && echo "Installing Node" \
    && tar -xJf "/downloads/node-v$NODE_VERSION-linux-$ARCH-musl.tar.xz" -C /usr/local --strip-components=1 --no-same-owner \
    && ln -s /usr/local/bin/node /usr/local/bin/nodejs \
    # Remove unused OpenSSL headers to save ~34MB. See this NodeJS issue: https://github.com/nodejs/node/issues/46451
    && find /usr/local/include/node/openssl/archs -mindepth 1 -maxdepth 1 ! -name "$OPENSSL_ARCH" -exec rm -rf {} \; \
    && node --version \
    && npm --version \
    && rm -rf /tmp/*

CMD ["gradle"]

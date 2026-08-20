# Homelab Foundation
#
# This image includes the JDK and dependencies that Homelab builds upon.
#
# This file includes code from Postgresql's Dockerfile
# Copyright (c) 2014, Docker PostgreSQL Authors (See AUTHORS)
# https://github.com/docker-library/postgres/blob/master/LICENSE
# See https://github.com/docker-library/postgres/tree/master/18/alpine3.23


FROM eclipse-temurin:25-alpine-3.23

ARG PG_MAJOR=18
RUN \
    echo "installing dependencies" \
    && apk add \
    argon2-libs \
    gosu \
    postgresql${PG_MAJOR} \
    postgresql${PG_MAJOR}-contrib \
    \
    && echo "creating /run/postgresql directory (for lockfiles)" \
    && mkdir /run/postgresql \
    && chown postgres /run/postgresql

COPY ./homelab-foundation /homelab-foundation

STOPSIGNAL SIGINT

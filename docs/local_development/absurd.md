Local Development Absurd
========================

Absurd powers some workflow code in Wasmo OS.

ℹ️ Prerequisite: [PostgreSQL](postgresql.md).

It's automatically installed in the Wasmo DB as part of the DB schema migrations (`ensureSchemaVersion()`).

You only need to install it manually if you’d like to upgrade the Wasmo DB to a later version
of `absurd`, or want to generate schema migration SQL that does so.

Complete instructions are in the [Absurd Installation] doc.

```bash
curl -fsSL \
  https://github.com/earendil-works/absurd/releases/latest/download/absurdctl \
  -o absurdctl
chmod +x absurdctl
```

[Absurd Installation]: https://earendil-works.github.io/absurd/tools/absurdctl/

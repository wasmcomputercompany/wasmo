Local Development Absurd
========================

Absurd powers some workflow code in Wasmo OS.

ℹ️ Prerequisite: [PostgreSQL](postgresql.md).

Only install it if you’d like to use their tooling to manage workflows.

Complete instructions are in the [Absurd Installation] doc.

```bash
curl -fsSL \
  https://github.com/earendil-works/absurd/releases/latest/download/absurdctl \
  -o absurdctl
chmod +x absurdctl
```


```bash
export PGDATABASE="postgresql://postgres:password@localhost:5432/wasmo_development"
absurdctl init
absurdctl schema-version
absurdctl create-queue default
```

[Absurd Installation]: https://earendil-works.github.io/absurd/tools/absurdctl/

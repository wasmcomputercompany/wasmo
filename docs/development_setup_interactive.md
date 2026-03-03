Development Setup: Interactive
==============================

Run a local server:

```bash
$ ./gradlew host:server:server-development:run
```

Navigate to [wasmo.localhost:8080](http://wasmo.localhost:8080) to view this.

Note that we prefix `localhost` with `wasmo.` because we use the domain attribute on our HTTP
cookies to authenticate users across hostnames (`wasmo.localhost`, `jesse99.wasmo.localhost`, etc.),
and browsers forbid the domain attribute on single-label hostnames like `localhost`.


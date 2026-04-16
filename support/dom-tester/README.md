# DOM Tester

Snapshot testing for HTML DOMs.

This is derived from [Redwood's dom-testing module][redwood].

### Files in Kotlin/JS

Kotlin/JS browser tests run via [Karma]. It runs a NodeJS service, which serves the HTML page,
and that hosts the in-browser tests.

We have a custom Karma middleware that implements these endpoints:

  * `GET /dom-tester-snapshots/{path}`: Reads a snapshot file from the local file system. Returns
    200 if the file is found, and 404 if it does not.

  * `POST /dom-tester-snapshots/{path}`: Writes a snapshot file to the local file system.

Prefixing either path with `/build` writes to the project's `build` directory, where files won't
be in Git.

### DOM Snapshots

We snapshot HTML elements using [html-to-image].

To debug, HTML is written to `build/dom-tester-snapshots`. Because these files don't include CSS
or other resources, run the service at `http://localhost:8080` while browsing these snapshots.


### Delayed Failures

When a call to [snapshot] detects a mismatch, that failure is queued until the test completes. This
is intended to avoid a usability problem for tests that take multiple snapshots. In such tests all
snapshots are emitted before any failures are reported.


### Snapshots Deleted?

It's a known bug that `./gradlew clean` deletes the generated snapshot files. It is safe to revert
these changes, or to regenerate with the instructions below.


### Generating New Golden Snapshots

To force new golden snapshots to be generated, run this:

```bash
$ ../../gradlew cleanDomTester jsBrowserTest --continue || ../../gradlew jsBrowserTest
```

Notice that we run `jsBrowserTest` twice. That's because we deliberately fail the task whenever new
snapshots are written. (The second test run should succeed.)


[Karma]: https://karma-runner.github.io/0.13/config/configuration-file.html
[html-to-image]: https://github.com/bubkoo/html-to-image
[redwood]: https://github.com/cashapp/redwood/

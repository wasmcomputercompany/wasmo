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


### DOM Snapshots

We snapshot HTML elements using [html-to-image].

[Karma]: https://karma-runner.github.io/0.13/config/configuration-file.html
[html-to-image]: https://github.com/bubkoo/html-to-image
[redwood]: https://github.com/cashapp/redwood/

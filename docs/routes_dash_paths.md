Dash Paths
==========

Wasmo owns the `/-/` prefix of each app domain for its own use.

HTTP requests to these paths are processed by the Wasmo host and are not routed to the app's
incoming HTTP handler.


Files in the Public Store
-------------------------

Apps have a public object store, and objects placed there are world-readable under `/-/public/`. It
is not necessary to be authenticated to the computer to view these files.

```kotlin
publicStore.put(
  PutObjectRequest(
    key = "hello.txt",
    value = "this is published to the entire Internet".encodeUtf8(),
  ),
)
```

These show up on the Internet for everyone here.

```
https://music-jesse99.wasmo.com/-/public/hello.txt
```

Files in the User Store
-----------------------

Apps have a user object store, and objects placed there are readable to that user when they are
signed in under `/-/user/`. Each authenticated user gets their own independent data for these files.

```kotlin
userStore.put(
  PutObjectRequest(
    key = "secret.txt",
    value = "this is only accessible to the user who stored it".encodeUtf8(),
  ),
)
```

```
https://music-jesse99.wasmo.com/-/user/secret.txt
```

Bundled files
-------------

Apps are distributed with resource files, which are world-readable under `/-/resources`.

```
https://music-jesse99.wasmo.com/-/resources/bundled.txt
```


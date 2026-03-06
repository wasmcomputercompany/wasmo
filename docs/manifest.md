Manifest
========

Wasmo apps are distributed using a manifest file plus a set of attached resources.

The manifest is a [TOML] file. It is typically named with the `.wasmo.toml` suffix, like
`recipes.wasmo.toml`.

This is a sample:

```toml
version = 35
slug = "recipes"
base_url = "https://example.com/recipes/v35/"

[[resource]]
url = "recipes.zip"
unzip = true

[[route]]
path = '/static/**'
resource_path = '/static/**'

[launcher]
label = 'Recipes'
maskable_icon_path = '/static/launcher-icon.svg'
```

We'll tour manifest's data, starting with the smallest possible manifest:

```toml
version = 35
slug = "recipes"
```

This is the application's version. It's an int64. When apps are upgraded, a lifecycle function is
called with the previous and new version, which may be useful to trigger migration code.

The slug is between 1 and 15 ASCII lowercase letters or digits. The first character is not a digit.
(If you're a regex enjoyer, the regex is `[a-z][a-z0-9]{0,14}`.)

This app doesn't do anything and returns HTTP 404 on all calls. Useful apps include executable code
(`app.wasm`), static resources, or both.

```toml
base_url = "https://example.com/recipes/v35/"
```

The base URL is used to resolve resource URLs in the manifest. It defaults to the URL that the
manifest was fetched from.


Resources
---------

```toml
[[resource]]
url = "app.wasm"
```

The manifest contains an array of resources. Note the double square braces on `[[resource]]`:
that's TOML’s Array of Tables syntax!

Resources are files that are transferred from the Internet to the Wasmo computer when the app is
installed.

```toml
[[resource]]
url = "https://example.com/static/icon.svg"
content_type = "image/svg+xml"
```

Resources may have an optional content-type; otherwise [common media types] will be guessed based on
their file extension.

```toml
[[resource]]
url = "recipes.zip"
unzip = true
```

Multiple resources may be aggregated into a `.zip` file for easier distribution. You must use
`unzip = true` to access items independently.

```toml
[[resource]]
url = "app.wasm"
sha256 = "21c14de150041aa8d5cf1c10bab6147b9fa9c60c324ea3e7fff22b293519fe81"
```

Resources can authenticated with a SHA-256 hash. If the hash doesn't match, installing the app will
fail.


WebAssembly
-----------

If the application is executable it must include an `app.wasm` resource.


Routes
------

By default, all HTTP calls to the application invoke the application's code.

```toml
[[route]]
path = '/favicon.ico'
resource_path = '/static/favicon.ico'
```

Use routes to directly serve individual files from the application's resources.

```toml
[[route]]
path = '/static/**'
resource_path = '/static/**'
```

If the route path ends with `/**`, and the resource path ends with `/**`, this maps all paths with
the same prefix.

```toml
[[route]]
path = '/recipe-list.json'
objects_key = '/my-recipes/recipe-list.json'
```

Routes can also serve objects written to the object store. The `objects_key` in the route should
have a leading `/` followed by the `key` written to the object store.

```toml
[[route]]
path = '/my-recipes/**'
objects_key = '/my-recipes/**'
```

Prefix mapping can also be used with object store routes.


Routes and Access
-----------------

Above we explained how routes are used to figure out how to fulfill a given network request. Routes
are also used to configure who has access.

```toml
[[route]]
path = '/my-recipes/**'
objects_key = '/my-recipes/**'
access = 'public'
```

By default, all routes are only available to the computer’s owner. Make a route available to the
entire Internet by specifying `access` is `public`.

```toml
[[route]]
path = '/blog/**'
access = 'public'
```

You can make a route publicly accessible while retaining its default data source. This configuration
executes the application to serve code prefixed with `/blog/`.

```toml
[[route]]
path = '/blog/**'
access = 'public'

[[route]]
path = '/blog/admin/**'
access = 'private'
```

You may make a path prefix `public`, and then narrow the access with a longer prefix. The precedence
rules of access rules is ‘longest wins’, so in this case a request for `/blog/admin/posts` will be
decided by the second route because `/blog/admin/**` is a longer prefix than `/blog/**`.


Launcher Appearance
-------------------

Applications control how they look on the Wasmo computer page.

Note that unlike `[[resource]]` and `[[route]]`, this item uses single-square braces.

```toml
[launcher]
label = 'Recipes'
maskable_icon_path = '/static/launcher-icon.svg'
```

The `maskable_icon_path` is a path that will be served by the application. It should be mapped by
a route, or generated on-demand by the application. This icon does not need to be public.

See [the icons guide](./launcher_icons.md) for guidance on styling the launcher icons.


[TOML]: https://toml.io/en/
[common media types]: https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/MIME_types/Common_types

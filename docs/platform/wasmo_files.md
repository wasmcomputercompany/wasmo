.wasmo files
============

Wasmo apps are distributed as `.wasmo` archives (ZIP files). When you install an app, Wasmo OS
copies the contents of the archive to the target computer.

Here's a sample layout of a `.wasmo` archive:

```
recipes.wasmo/
 |- app.wasm
 |- wasmo-manifest.toml
 '- www/
     '- static/
         |- launcher-icon.svg
         '- recipes.css
```

In [dev mode](./dev_mode.md), Wasmo OS can run applications directly from a directory on the local
file system. This way when you change the manifest, the `.wasm` file, or a resource, that change is
reflected immediately in the running application.

In either mode, the archive or directory’s contents are available as _resources_ that can be used
by the application.


`app.wasm`
----------

If the application is executable it must include an `app.wasm` resource. This special path is loaded
by Wasmo OS and executed.

(Applications that don't include an `app.wasm` file may serve static resources only.)


`wasmo-manifest.toml`
---------------------

At the root of the archive/directory there must be a [TOML] file with this name.

This is a sample:

```toml
target = 'https://wasmo.com/sdk/1'
version = 35
base_url = 'https://example.com/recipes/v35/'

[[external_resource]]
from = '../build/dist/js/developmentExecutable'
to = '/www/static'
include = ['**/*.js', '**/*.js.map']

[launcher]
label = 'Recipes'
maskable_icon_path = '/static/launcher-icon.svg'
```

We'll tour manifest's data, starting with the smallest possible manifest:

```toml
target = 'https://wasmo.com/sdk/1'
version = 35
```

#### `target` (Required)

The `target` must be `https://wasmo.com/sdk/1`. (This is our mechanism to evolve our spec.)

#### `version` (Required)

The `version` is the application's version. It's an int64. When apps are upgraded, a lifecycle
function is called with the previous and new version, which may be useful to trigger migration code.

This app doesn't do anything and returns HTTP 404 on all calls. Useful apps include executable code
(`app.wasm`), static resources, or both.


### `external_resource` (Array)

```toml
[[external_resource]]
from = '../build/dist/js/developmentExecutable'
to = '/www/static'
include = ['**/*.js', '**/*.js.map']
```

This is only used in dev mode.

The manifest contains an array of external resources. Note the double square braces on
`[[external_resource]]`: that's TOML’s Array of Tables syntax!

This item maps resources on the local file system so they can be used by the application.

When the Wasmo CLI packages a directory into a `.wasmo` archive, it will copy external resources
into the archive (and omit the `[[external_resource]]` item).


#### `from`

The local file or directory to copy resources from. This may be an absolute or relative path. If it
is a relative path, it is relative to the directory that contains the `wasmo-manifest.toml` file.

#### `to`

The path to copy resources to. This must start with `/`.

#### `include` (optional, default is `['**/*']`)

A pattern to select which paths to copy. Use `*` to match any sequence of characters in a path
segment (no slashes), and `**` to match any number of path segments (including none).


### `launcher` (Optional)

Applications control how they look on the Wasmo computer page.

Note that unlike `[[external_resource]]`, this item uses single-square braces.

```toml
[launcher]
label = 'Recipes'
maskable_icon_path = '/static/launcher-icon.svg'
home_path = '/home'
```

#### `label` (Optional)

This is a short string labeling the icon in the launcher.

#### `maskable_icon_path` (Optional)

The `maskable_icon_path` is a path that will be served by the application. It should be mapped by
a route, or generated on-demand by the application. This icon does not need to be public.

See [the icons guide](./launcher_icons.md) for guidance on styling the launcher icons.

#### `home_path` (Optional, default value is `/`)

This is the path that the launcher will navigate to when launching the application.


### `dev_mode` (Optional)

This configuration is ignored unless Wasmo OS is running in dev mode.

```toml
[dev_mode]
hot_reload = true
```

#### `hot_reload` (Optional, default is false)

True to automatically reload the app when its code or resources change. This is implemented by
dangerously rewriting the application's HTML to add a reload trigger.

See [the dev mode guide](./dev_mode.md) for details.


`www-public/` files
-------------------

All files in the `www-public/` directory are served on the application's website to everyone on the
Internet.


`www/` files
-------------------

Files in `www/` are only served to the computer's owner. If the owner is not signed in, or if any
other user requests such files, the request will be routed to the application's `httpService`
instead.

See the [Inbound HTTP Calls](inbound_http_calls.md) for details and precedence rules.


[TOML]: https://toml.io/en/
[common media types]: https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/MIME_types/Common_types

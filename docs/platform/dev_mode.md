Dev Mode
========

Wasmo OS can run in dev mode. It adds features to help develop new apps.


Install from the File System
----------------------------

In addition to installing apps from a URL, like `https://example.com/recipes/v1/recipes.wasmo`,
apps can also be installed from the file system, like `/Users/jesse/Development/recipes/build/recipes.wasmo`.


Updates without version changes
-------------------------------

The OS will update apps even if the manifest’s version field is unchanged.


Polling for Updates
-------------------

The OS will poll for app updates. If the app is installed from the file system, it'll watch the file
system and install updates automatically.


Hot Reloading
-------------

This requires an opt-in in the [manifest](wasmo_files.md).

Served HTML will be rewritten to add a hot reloading hook. This will trigger automatic page
refreshes when the app is updated.

Wasmo OS will not inject code unless this setting is enabled and the OS is executing in dev mode.

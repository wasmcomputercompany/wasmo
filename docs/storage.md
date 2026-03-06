Storage
=======

Wasmo doesn't have a hierarchical file system.

There's an S3-like object store, and a SQLite-compatible database.

Goals
-----

When moving between Wasmo hosts, everything the user cares about should be available in S3.

 - All the users' data, both objects and databases
 - Which apps are installed, at which versions, even if the original artifacts are no longer online

But we don't need the S3 directory to be the always-on source of truth for everything. Instead,
users can push an ‘export’ button to write wasmo-metadata.json to this S3 store. Symmetrically,
pushing an ‘import’ button should read this file.

There’s some races and complexity here that needs to be more thoroughly designed. For example, we
should decide whether a host locks an S3 bucket so it isn’t accidentally used by two hosts
concurrently.


S3
--

The S3 bucket has a `/user` directory where apps can read and write objects directly.

```
/bucket
    /photos
        /user
            /library
                /DCIM0001.heif
                /DCIM0002.heif
            /thumbnails-512x512
                /DCIM0001.jpg
                /DCIM0002.jpg
```

There's a `/sqlite` directory where apps' SQLite databases are stored. Apps aren't given direct
access to this directory because database access is managed.

```
/bucket
    /photos
        /sqlite
            /photos.db
    /snake
        /sqlite
            /default.db
```

There's an `/app` directory where resources from the installed apps go. We copy everything at
app install time, so app resources don't need to be online when the apps are used.

```
/bucket
    /photos
        /app
            /v2
                /app.wasm
                /resources
                    /photos.css
                    /photos.html
                    /photos.js
    /snake
        /app
            /v30
                /app.wasm
                /resources
                    /snake.css
                    /snake.html
                    /snake.js
```

Backblaze B2
------------

We've implemented connectivity to Backblaze.

We're pinned to the CA East region.

We've done our own S3 client, including [AWS Signature Version 4].

```
https://s3.<region>.backblazeb2.com/<your-bucket-name>
```


App Updates
-----------

To update apps:

1. Garbage collect all old obsolete versions.
2. Copy in the new version.
3. Set the version in the admin database.
4. Garbage collect all old obsolete versions.

The double GC means we’ll avoid ever having more than two versions in the store at the same time,
even if step 4 crashes at some point.


[AWS Signature Version 4]: https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_sigv.html

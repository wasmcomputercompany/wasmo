Journal
=======

This is a sample app that attempts to lightly exercise some platform APIs.


Entries
-------

An entry has a token, a title, a date, a markdown-formatted body, and a slug.

An entry can be public or private.


Attachment
----------

Images etc. are stored in the object store as a child of an entry with a sequence number.


Object Store
------------

### `/site`

The published site.


Dynamic Pages
-------------

### `/admin`

The admin home page shows a list of entries.


### `/admin/entries/<token>`

Edit a particular entry.


APIs
----

Where request or response bodies exist, the content type must be `application/json`.


### `/api/entries`

List entries in reverse chronological order.


### `/api/entries/<token>`

Edit an entry identified by a token.

`POST` to this endpoint to save.

`GET` to this endpoint to fetch an entry JSON.


### `/api/entries/<token>/attachments/<token>`

`GET` this endpoint to read an attachment.

`PUT` to this endpoint to add an attachment.


### `/api/publish-state`

Returns the journal's publish state.


### `/api/request-publish`

Enqueue a job to immediately publish the site.


Publishing
----------

1. Write the entire site to the object store.
2. Prune unreachable content from the object store.

Use the database to guide this process. In particular, we walk the file system and remove entries that
don’t match anything currently published.



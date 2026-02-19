App Platform
============

Inbound HTTP Calls
------------------

This is how the application presents an HTML UI to browsers, and data API to mobile applications.
It may also be used for webhooks.

Inbound requests will include additional context about the caller:

 - Original IP address
 - Single-sign on identity


Static Assets Serving
---------------------

Applications can embed static assets, and they'll be automatically served with appropriate caching
headers.


Outbound HTTP Calls
-------------------

Applications can call other servers over HTTP.


Web Sockets
-----------

We have web sockets, but the process serving a web socket call isn’t necessarily long-lived and
memory resident. Instead, we map web sockets into resources that can post messages and receive them.


Secrets Vault
-------------

A key-value store of secrets.

It has a `getOrCreate()` API if you need to provision a new secret.

It can do secure-enclave style `Ed25519` signing, where the application can create a key pair,
validate a signature, but never exfiltrate the private key.


Object Store
------------

An S3-compatible service.

We don’t expose the full capabilities of Amazon S3 because we want to support multiple independent
implementations. For example, you cannot set arbitrary permissions on files; everything is
private to the application.


SQLite DB
---------

Applications can create and list their SQLite databases.


Job Scheduling
--------------

Applications can ask to be called back in the future. This may be implemented itself in the
guest layer, using the SQLite DB to store job metadata.

We should have a maximum parallelism permitted by any application, to avoid fork bombs.


Version Upgrade
---------------

When attempting an app update, we call the new application, `/after-update` with information about
the previous and current installation version. If this command fails, we fail the update.


Resources
---------

Applications can observe their own use of these resources:

 * CPU
 * RAM
 * Storage
 * Network

We’ll also include APIs to convert resource use into money, so applications can document what their
resources cost the user.

We'll use [Tigerbeetle] to track for resource use.


In-App Purchases
----------------

Installed applications can request in-app purchases that must be approved by the user interactively
through a OAuth-like flow.

This may be used to unlock features in the application, such as a 'Supporter' badge in Immich.

When payment is received we will optionally notify an external endpoint owned by the application's
vendor. That endpoint should acknowledge receipt and return a signed token that can be validated by
the installed application. This entire scheme prevents self-hosted applications from spoofing
in-app purchases.



[Tigerbeetle]: https://tigerbeetle.com/

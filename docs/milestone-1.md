Milestone 1
===========

Hello World Guest App
---------------------

It's a simple program that can receive an inbound HTTP request and respond with a 'Hello, Jesse'
HTML response.


Server
------

The server has the following capabilities:

1. Create new computers
2. Install apps into computers
3. Run the Hello World endpoint


Implementation
--------------

I'm comfortable with Kotlin, so I intend to start there for both client and server.

 * [PostgreSQL] for the server's own bookkeeping
 * [Kobweb] for the server's own web framework
 * [Ktor] for the server's own web serving
 * [SQLDelight] for SQL


Challenges
----------

[WIT] doesn’t have a Kotlin implementation, though one is [coming soon][KT-64569].

[Chasm] is new.


Out of Scope
------------

Doing reasonable things with DNS names. Ultimately we want DNS to direct everything on
`*.example.com` to our server, and let HTTP sort it out.

Accepting payments to create computers. When this is fully implemented it should be optional.

Authentication.



[Chasm]: https://github.com/CharlieTap/chasm
[Kobweb]: https://kobweb.varabyte.com/
[Ktor]: https://ktor.io/
[PostgreSQL]: https://www.postgresql.org/
[SQLDelight]: https://sqldelight.github.io/sqldelight/latest/
[WIT]: https://component-model.bytecodealliance.org/design/wit.html
[KT-64569]: https://youtrack.jetbrains.com/projects/KT/issues/KT-64569/Kotlin-Wasm-Support-Component-Model

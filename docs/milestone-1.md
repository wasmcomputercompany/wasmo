Milestone 1
===========

Create an account and a computer through the web user interface. We will initially skip payment so
the 'Checkout' button will skip immediately to creating a computer.

Install a Hello World guest app into the computer.

Execute the Hello World guest app.


Hello World Guest App
---------------------

It's a simple program that can receive an inbound HTTP request and respond with a 'Hello, Jesse'
HTML response.


Server
------

The server has the following capabilities:

1. Create accounts and computers.
2. Install apps into computers
3. Run the Hello World endpoint


Implementation
--------------

I'm comfortable with Kotlin, so I intend to start there for both client and server.

 * [PostgreSQL] for the server's own bookkeeping
 * [Compose HTML] for the server's own web framework
 * [Ktor] for the server's own web serving
 * [SQLDelight] for SQL


Challenges
----------

[WIT] doesn’t have a Kotlin implementation, though one is [coming soon][KT-64569].


Out of Scope
------------

Doing reasonable things with DNS names. Ultimately we want DNS to direct everything on
`*.example.com` to our server, and let HTTP sort it out.



[Chasm]: https://github.com/CharlieTap/chasm
[Compose HTML]: https://github.com/JetBrains/compose-multiplatform/tree/master/html
[Ktor]: https://ktor.io/
[PostgreSQL]: https://www.postgresql.org/
[SQLDelight]: https://sqldelight.github.io/sqldelight/latest/
[WIT]: https://component-model.bytecodealliance.org/design/wit.html
[KT-64569]: https://youtrack.jetbrains.com/projects/KT/issues/KT-64569/Kotlin-Wasm-Support-Component-Model

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


Host Server
-----------

The host server has the following capabilities:

1. Create accounts and computers.
2. Install apps into computers
3. Run the Hello World endpoint, implemented in Kotlin (and not WebAssembly)


Implementation
--------------

I'm comfortable with Kotlin, so I intend to start there for both client and server.

 * [PostgreSQL] for the server's own bookkeeping
 * [Compose HTML] for the server's own web framework
 * [Ktor] for the server's own web serving
 * [SQLDelight] for SQL

We're going to start by faking the WebAssembly integration and embed Kotlin instead. This way we
can focus on bringing integrations online, and save WebAssembly to a later milestone.


Challenges
----------

[WIT] doesn’t have a Kotlin implementation, though one is [coming soon][KT-64569].


[Chasm]: https://github.com/CharlieTap/chasm
[Compose HTML]: https://github.com/JetBrains/compose-multiplatform/tree/master/html
[Ktor]: https://ktor.io/
[PostgreSQL]: https://www.postgresql.org/
[SQLDelight]: https://sqldelight.github.io/sqldelight/latest/
[WIT]: https://component-model.bytecodealliance.org/design/wit.html
[KT-64569]: https://youtrack.jetbrains.com/projects/KT/issues/KT-64569/Kotlin-Wasm-Support-Component-Model

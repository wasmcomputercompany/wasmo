Milestone 3
===========

Proposed on 2026-04-28 by Jesse
🪏 Status: in development


Wasmo Developer Experience
--------------------------

Let’s set up continuous integration. We could use GitHub actions, or one of the nicer alternatives
like BuildKite.

We’re attempting to [structure dependencies] between our Gradle modules. Let’s formalize this and
enforce it with build tooling.


Wasmo Kotlin SDK
----------------

Let’s publish a platform `.jar` for app development outside the OS repo. App developers will use it
as a container to run and test their applications.

This initial SDK will not use WebAssembly.


Isolated App Databases
----------------------

Complete Mike’s work on provisioning per-app databases.


WebAssembly
-----------

Let’s compile Journal to WebAssembly.

We’ll need to create [WIT] interfaces for the entire platform:

* Create `.wit` files for the entire platform.
* Generate `.kt` bindings for those `.wit` interfaces.
* Bridge the host OS and the generated bindings.
* Bridge the guest application and the generated bindings.


[structure dependencies]: https://ralf-wondratschek.com/presentation/android-at-scale-at-square.html
[WIT]: https://component-model.bytecodealliance.org/design/wit.html
[BuildKite]: https://buildkite.com/home/

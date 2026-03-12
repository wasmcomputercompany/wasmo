Code Structure
==============

We write compact Kotlin.


Dependencies
------------

In the platform itself we're avoiding application frameworks like React and Spring Boot. If the
platform has a deprecation cycle that we need to track, it's a bad fit for this project.

We sparingly use small purpose-built open source libraries to integrate specific technologies:

 * passkeys
 * TOML
 * Stripe

One possible exception to this rule is that we use [Metro](./dependency_injection.md) for dependency
injection.


Exceptions
----------

We've got some custom exception classes, extending from `UserException`.

Throw `UserException` when there's a mistake attributable to an app developer or interactive user.
For example:

 * Malformed TOML in a developer-supplied manifest
 * A slug that's longer than permitted
 * A resource referenced by a manifest cannot be downloaded

These errors are not problems with host service and shouldn't page the Wasmo operator. But they
should be surfaced to the end user or developer.

All other exceptions are routed to the Wasmo operator should page that person.




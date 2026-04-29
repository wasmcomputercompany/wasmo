WasmoX
======

Like any platform, Wasmo OS and Wasmo applications are developed and deployed independently.

The platform APIs are the boundary between the OS and applications. This boundary is where APIs are
most ridged. Changes to these APIs impact service operators (including self-hosted end users),
application developers, and even end-users who suffer from OS/application compatibility problems.

We mitigate this somewhat by shipping some behavior in Wasmo applications as libraries, rather than
in the platform. These are called the 'WasmoX' libraries.

This pattern is inspired by [AndroidX], which is how the Android ecosystem accelerates application
development.

[AndroidX]: https://developer.android.com/jetpack/androidx

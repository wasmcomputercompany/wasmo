Wasmo, Your Cloud Computer
==========================

We're building a cloud computer for regular people.

It's a secure place to store all your stuff:

 - A lifetime of photos and videos.
 - Your collection of music, books, and movies.

Wasmo can do all kinds of computery things, like running apps:

 - to manage your smart devices
 - to securely store your passwords
 - to track what you're reading, watching, and listening to

You'll pay a low monthly price for your Wasmo. And you'll be able to cancel all those enshittified
services that charge you every month to hold a narrow slice of your digital life.


Yours
-----

It's gross that Google and Apple close people's accounts without reason or accountability. It's
gross that governments in foreign countries have jurisdiction over our data.

We'd like to fix that.

Your Wasmo is yours. And although it's in our datacenter, its fast & easy to move it to somebody
else's. You can even run it Wasmo at home if you have a spare house computer.

Wasmo is open source.


Cloud
-----

Your Wasmo runs in a datacenter in Toronto, Canada. (We intend to expand into multiple countries so
you can put your Wasmo in a place that fits your geography and your politics.)

Running in a datacenter is rad:

 - access your stuff everywhere you have Internet
 - pay-as-you-go for storage, scaling up as much as you need
 - always on and backed-up


Computer
--------

Wasmo is a computer. It has a UI, runs apps, and stores your stuff.

But Wasmo isn’t a _conventional_ computer; it’s a _WebAssembly_ computer. It saves power (and
money!) by only running when you're using it. Each app runs in a secure sandbox and can't access
each other's data.


Status
======

Wasmo is under active development. We hope to have a public preview in July 2026.

Work-in-progress:

 * [wasmo.dev (staging)](https://wasmo.dev/): our unstable experimentation environment.
 * [wasmo.com (production)](https://wasmo.com/): an incomplete preview of the production service.

### ⚠️ Warning! ⚠️

Don't put any data you care about on either `wasmo.dev` or `wasmo.com`! Until we launch, either
environment may be wiped without notice.


Developers
==========

Are you interested in building a Wasmo app? We hope to have a developer SDK in July 2026. Get in
touch now if you'd like.


Contact
=======

 * [jesse@wasmo.com](mailto:jesse@wasmo.com), project founder


Contributors
============

Wasmo docs are in [docs](docs/).


Project layout
--------------

| Directory   | What it's for                                                                            |
|:------------|:-----------------------------------------------------------------------------------------|
| apps        | Wasm-packaged applications that use the framework.                                       |
| `'--` admin | The admin app. It's pre-installed on each computer and privileged to install other apps. |
| client      | The user interface to create new computers.                                              |
| platform    | APIs exposed by the server and consumed by apps.                                         |
| server      | A deployable artifact that hosts apps and satisfies their requirements.                  |
| common      | Shared utilities.                                                                        |


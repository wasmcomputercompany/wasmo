Inbound HTTP Calls
==================

This is how the application presents an HTML UI to browsers, and data API to mobile applications.
It may also be used for webhooks.


Application URLs
----------------

Each application has its own hostname reachable to anyone on the Internet!

For example, if the application named `recipes` is installed on the computer named `jesse99`, the
installed application's website will have a URL like `https://recipes-jesse99.wasmo.com/`.


Inbound Headers
---------------

Inbound requests include additional context about the caller:

- Original IP address
- Single-sign on identity

Applications do not see cookies issued by Wasmo OS:

* `__Http-wasmo_session`
* `wasmo_session`

Routing Rules
-------------

Every inbound HTTP call has this routing process:

1. Static objects in the `www-public/` directory of the object store.

2. Static resources in the `www-public/` directory of the app's `.wasmo` file.

3. Static objects in the `www/` directory of the object store.
   This route is only used if the caller is authenticated as the computer owner.

4. Static objects in the `www/` directory of the app's `.wasmo` file.
   This route is only used if the caller is authenticated as the computer owner.

5. Dynamically calling the app's `httpService`.
   This route is only used if the caller is authenticated as the computer owner.

If the request path ends with `/` the file name `index.html` is appended before looking up static
objects and static resources.

Serving static objects is potentially more efficient because they are handled by the OS.

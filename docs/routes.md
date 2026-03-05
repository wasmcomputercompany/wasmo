Routes
======

Computer Launcher
-----------------

```
https://jesse99.wasmo.com/
```

This is the home page for the computer `jesse99`.

On the home screen we'll show a list of `UserTasks`:

 * Welcome
 * ConfirmEmailAddress
 * PaymentOverdue


Apps
----

```
https://music-jesse99.wasmo.com/
```

This is the home page for the app `music` on the computer `jesse99`.

Paths prefixed with `/-/` are [dash paths](routes_dash_paths.md) and are processed by Wasmo. The
application will never receive these requests.

Paths prefixed with `/.well-known/` are [well-known](https://en.wikipedia.org/wiki/Well-known_URI)
and may be processed by the Wasmo host or routed to the application for processing. Initially there
is an allowlist of well-known paths that are routed to the application.

All other paths are routed to the application for processing.


Invite
------

```
https://wasmo.com/invite/abcdabcdabcdabcdabcdabcd
```

It's a simple form with some text and a button.

    Wasmo

    Your Cloud Computer

    You've been invited to look around our wildly incomplete website. Please send feedback to
    jessewilson.99 on Signal.

    [Accept Invite]

If the ticket has a passkey saved, it requests it.

If the ticket does not have a passkey saved, it saves a new one.

Once you have a passkey, you redirect to `/build-yours`.


Admin
-----

If the current session has an `admin` role grant there's a screen to create new invite tickets.

If the current session does not have `admin`, this redirects to `/`.

```
https://wasmo.com/admin
```

Invite tickets have a name and email address attached.

Invite tickets track whether they're claimed.


Teaser
------

```
https://wasmo.com/teaser
```

This shows a Wasmo logo and a link to GitHub.


Build Yours
-----------

```
https://wasmo.com/build-yours
```

This is the form to build a new Wasmo. It links to the Stripe checkout site.


Computers
---------

```
https://wasmo.com/computers
```

This is a list of the current user's Wasmos.


Home
----

```
https://wasmo.com/
```

If there's no invite_ticket in the page data, this shows `/teaser`.

If there is a computer_list in the page data, this shows `/computers`.

Otherwise, this shows `/build-yours`.


After Checkout
--------------

```
https://wasmo.com/after-checkout/abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd`
```

When this loads, we need to make an API call to Stripe.

If the session status is 'complete', redirect to the computer launcher.

If the session status is 'open', redirect to `/build-yours` with a notification that payment is
incomplete.



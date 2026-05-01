In-App Purchases
================

We’ll offer an API for installed apps to solicit payment to unlock features or other things.
End-users will approve one-time purchases and subscriptions interactively through an OAuth-like
flow.

This may be used to unlock features in the application, such as a 'Supporter' badge in Immich,
or a Santa Hat in Snake.

A purchase involves several coordinating programs. For example, buying a hat in Snake involves:

* *The Snake Wasmo app*, running on either wasmo.com or Homelab Wasmo. It is bundled with a public
  key to check signatures made by the app’s publisher.

* *The User’s Browser*, interacting with the Snake Wasmo app.

* *The Wasmo.com Shop*, our in-app purchase service that coordinates all payments, including
  payments that occur on Homelab Wasmo.

* *The Snake App Publisher Site*, who will accept money to unlock cool hats. They keep a private key
  to sign In-App Purchase Receipts (IAPRs).

The happy path hat purchase goes like this:

```
        Snake                User's             Wasmo.com            Snake App
       Wasmo App            Browser               Shop              Publisher Site
          |                    |                    |                    |
          |               “I would like             |                    |
          |                 to buy my               |                    |
          |                snake a hat”             |                    |
          |                    |                    |                    |
          |            buy hat |                    |                    |
          |<-------------------|                    |                    |
          |                    |                    |                    |
          | Redirect To        |                    |                    |
          | Purchase URL       |                    |                    |
          |------------------> | GET /buy?...       |                    |
          |                    |------------------->|                    |
          |                    |                    |                    |
          |                    |       Confirm Page |                    |
          |                    |<-------------------|                    |
          |                    |                    |                    |
          |                 “that is a              |                    |
          |                 fair price”             |                    |
          |                    |                    |                    |
          |                    | POST /approve      |                    |
          |                    |------------------->| In-App Purchase    |
          |                    |                    | Receipt (IAPR)     |
          |                    |                    |------------------->|
          |                    |                    |                    |
          |                    |                    |                  sign
          |                    |                    |                  IAPR
          |                    |                    |                    |
          |                    |                    | Signed IAPR        |
          |                    |        Signed IAPR |<-------------------|
          |                    |         + Redirect |                    |
          |        Signed IAPR |<-------------------|                    |
          |<-------------------|                    |                    |
          |                    |                    |                    |
        check                  |                    |                    |
      signature                |                    |                    |
          |                    |                    |                    |
          | snake              |                    |                    |
          | wearing hat        |                    |                    |
          |------------------->|                    |                    |
          |                    |                    |                    |
          V                    V                    V                    V
```

This is a complicated scheme! But it ensures the app publisher retains full control of their
intellectual property. In particular, they control that the number of dollars received is consistent
with the number of purchases made.


Wasmo.com Shop Signing
----------------------

One of the main value propositions of Wasmo is that app publishers don’t need to operate web
services indefinitely. But in the process above we have required the app publisher operate a
highly-available webservice to sign IAPRs.

We can have the Wasmo.com Shop do that instead. Publishers that trust our bookkeeping don’t need to
run a server.


Motivation
----------

We're attempting to build a thriving 2-sided market for Wasmo users and application developers.
We need a mechanism for users to buy software to ensure a supply of commercial software.

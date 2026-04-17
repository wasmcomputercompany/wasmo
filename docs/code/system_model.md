System Model
============

Accounts & Authentication
-------------------------

An `Account` is a unit of access.

Each account has zero or more linked email addresses that can be used to sign in. An email address
may be linked to at most one account at a time.

Each account has zero or more `Cookies` that grant the holder full access to the account's data.

Each account has zero or more `Passkeys` that also grant the holder full access to the account's
data. Use passkeys to extend trust from one cookie-holding client to another.

We create accounts eagerly and without a sign-up process! You just need to have a browser that can
save a cookie, and take any action that our service wants to persist.


Invites
-------

An account can create invitations, that can be claimed by another account.

At the moment these aren't tagged or limited in any way. We should use this primitive to create a
proper invite incentive with privileges for both sender and receiver.


Computers
---------

A computer is a unit of billing.

Create a spec for a computer `ComputerSpec`, then pay for it to materialize it into a real
`Computer` record.

Computers are linked to accounts with `ComputerAccess`. An access record is initially granted to the
spec’s creator.

We may later add mechanisms to share a computer with authenticated friends and family.

A computer is paid for if there's an active `ComputerAllocation`. This links a paid time period
to a computer.

A computer has a URL like `jesse12.wasmo.com` or `mikepaw3.wasmo.dev`. Accounts with access to this
computer may successfully load this URL; others will be redirected to a sign-in screen.


Published Apps
--------------

A _published_ app is an inert artifact described by an `AppManifest`. It is data.


Installed Apps
--------------

Install a specific published app on a specific computer to create an _installed_ app.

An installed app has a URL like `snake-jesse12.wasmo.com` or `recipes-mikepaw3.wasmo.dev`. Accounts
with access to the host computer may successfully load this URL. Others will either be redirected
or not, depending on the route configuration in the app’s manifest.

When an app needs operational work, its `maintenance_scheduled_at` timestamp is not null and a job
is enqueued. Calls to the app must wait until maintenance is complete. We'll use a combination of
server-side delays and HTTP 503 responses.

Maintenance will not start until all in-flight calls complete.

Here's some kinds of app maintenance:

 * `InstallAppJob`
 * `UpdateAppJob` (not yet implemented)
 * `ClearDataJob` (not yet implemented)










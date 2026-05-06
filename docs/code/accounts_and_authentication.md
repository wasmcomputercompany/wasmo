Accounts and Authentication
===========================

An `Account` is a unit of access.

We persist accounts eagerly, possibly before the sign-up flow. Use a browser that can save a cookie
to take any action that causes our service to store some data.

This lets us link all of a client’s stuff together. For example, suppose you picked French as your
language on Wasmo.com’s main landing page. If you later sign up with an email address, we know to
use French when emailing you.

The number of records in the `Accounts` table is not useful for tracking how many customers we have.


Sign Up and Sign In
-------------------

Most of the useful actions in Wasmo require a Signed In account. An account is Signed In once they
provide the requirements of the distribution.

| Distribution | Required                                | Optional                                    |
|--------------|-----------------------------------------|---------------------------------------------|
| Homelab      | `Username` (default is `admin`)         | `Password`, `InstanceAdminGrant`            |
| SDK          | `Username` (default is `admin`)         |                                             |
| Hosted       | `VerifiedEmailAddress`, `AcceptedTerms` | `Passkey`, `Password`, `InstanceAdminGrant` |
| Sandbox      | `VerifiedEmailAddress`, `AcceptedTerms` | `Passkey`, `Password`, `InstanceAdminGrant` |

We use cookies to link a browser session with an account. Each account has zero or more `Cookies`
that grant the holder access to the account.


Account Assets
--------------

### `Username`

Support for usernames requires a new DB schema revision. The initial (`admin`) account is set up as
part of that schema migration. Initially, it has no password.

### `Password`

Passwords are optional.

Once supported, passwords can be added to accounts that do not yet have one.

Passwords will be stored following [OWASP guidelines].

### `InstanceAdminGrant`

If an account has this asset, it has admin privileges on the instance.

Admins may do the following:

- Create invite links
- Create ‘reset password’ links for any user
- Grant or revoke admin for any user

This is a normal privilege for Homelab, and an extremely dangerous privilege for Hosted.

All privileged actions are written to an audit log.

### `Passkey`

Passkeys are partially implemented in the [invites flow](../../code/invites.md). We should probably
redo that flow (but keep the passkeys).

### `VerifiedEmailAddress`

We [verify email addresses](../../code/email_address_linking.md).

### `AcceptedTerms`

We will add a terms of use document and require users to accept these terms before they can engage
with the services we host.


Implementation
--------------

Homelab admins can choose to require Passwords for their instance. They are optional by default.

Jesse's working on making it possible to express what's required via the DI graph.

Account migration between instance types is not supported.

Exposing Homelab to the Internet is not Wasmo’s responsibility. Can be done via home VPN.

[OWASP guidelines]:https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html

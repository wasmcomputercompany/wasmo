Accounts Assets
===============

Different distributions require different assets before they are permitted to create or operate
computers.

| Distribution | Required                                | Optional                                    |
|--------------|-----------------------------------------|---------------------------------------------|
| Homelab      | `Username` (default is `admin`)         | `Password`, `InstanceAdminGrant`            |
| SDK          | `Username` (default is `admin`)         |                                             |
| Hosted       | `VerifiedEmailAddress`, `AcceptedTerms` | `Passkey`, `Password`, `InstanceAdminGrant` |
| Sandbox      | `VerifiedEmailAddress`, `AcceptedTerms` | `Passkey`, `Password`, `InstanceAdminGrant` |

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

Required assets are fixed at build time via the distribution's `WasmoService.Config`.

Jesse's working on making it possible to do this via the DI graph, but that isn't ready yet.

Account migration between instance types is not supported.

Exposing homelab to the Internet is not Wasmo’s responsibility. Can be done via home VPN.

[OWASP guidelines]:https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html

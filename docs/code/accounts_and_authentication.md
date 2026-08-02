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

Passwords will be stored following [OWASP guidelines]. Specifically we store a hash + salt for each password in the DB, see [Password Hashing](#password-hashing) below.

It's the user's choice whether they want to enable password authentication for their account.

- If present, the password is associated with the account (not a username), to allow flexibility to introduce passwords for accounts identified via email address. However, initially our UI might only allow setting one on Homelab (implies username).
- It's the user's choice whether they want to choose password authentication or a more secure method.
- At most one password per account. We don't make any efforts to discourage password sharing (e.g. hand out access keys). If you want to share your account with your spouse, you have to share the password.

| account identifier | has password? | how a user signs in                                         |
|--------------------|---------------|-------------------------------------------------------------|
| username           | password      | click on username and type password                         |
| username           | no password   | click on username                                           |
| email address      | password      | enter email address and password (optionally passkey)       |
| email address      | no password   | enter email address and challenge code (optionally passkey) |

 - If an account is identified by email address-identified, the client doesn't know whether the thing it's prompting the user for is a password or a challenge code.
   - This prevents rogue clients probing whether a particular email address has an account.
 - If an account is identifed by username, the client is told whether there's a password on the account, so it knows whether to prompt for one.

#### Password Hashing
- We'll store an *Argon2* hash ([Wikipedia](https://en.wikipedia.org/wiki/Argon2)) for each password.
- Inputs to the hash are:
  - the plaintext password as UTF-8 bytes
  - a random per-password salt (16 bytes)
  - a random per-Wasmo instance **pepper** (TBD bytes - maybe >= 16 bytes to match salt?)
  - the account ID
  - these hard coded values (from OWASP guidelines); the hard-coded values at the time a password is created are stored with the hashes, in case we want to change these values later.
    - memory size m=12288 (12 MiB)
    - iterations t=3
    - parallelism p=1
- The pepper and accountId together are passed as **associatedData** in the `Argon2` function. While we could just concatenate them, we will instead serialize a protobuf containing those two values in order to remain more flexible about future additions.
- Hashing happens on the server only; during login, the client sends the plaintext password over https (else the hash would become the password); note that the cookie is transmitted over https as well.
- Protection against brute force cracking is provided through
   - slowness of Argon2 + difficulty of obtaining DB + pepper
   - rate limiting via com.wasmo.permits.PermitService (this measure doesn't help if/when a dump of the DB has been stolen)
- We'll use the [reference implementation of Argon2](https://github.com/P-H-C/phc-winner-argon2) (in C) with these [JVM bindings](https://github.com/phxql/argon2-jvm) - that project is much more active than [the alternative](https://github.com/kosprov/jargon2-api) also linked from the reference implementation GitHub page.
- The per-Wasmo instance *pepper* value means that password hashes can only be used on that instance.
  - This is consistent with our plan to support moving _computers_ between Wasmo instances, but not _accounts_.
- The pepper value is secret configuration of the Wasmo instance, like the postgresql password. It's stored in the same place: For now in an environment variable, later in a dedicated secrets vault.
- Out of the three variants (Argon2i, Argon2d, and Argon2id), we'll ues Argon2id for hybrid protection against both classes of attack noted in the [README](https://github.com/P-H-C/phc-winner-argon2/blob/master/README.md).

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

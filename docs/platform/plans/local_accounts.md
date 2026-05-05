Local Accounts
==============

Per distribution, there is one type of account: local (homelab), or standard (sandbox, hosted):

| distribution | host URL                                              | account type |
|--------------|-------------------------------------------------------|--------------|
| homelab      | http://jesse123.wasmo.local or http://wasmo.localhost | local        |
| sandbox      | https://jesse123.wasmo.dev                            | standard     |
| hosted       | https://jesse123.wasmo.com                            | standard     |

Credential type depends on the account type:

| account type | credential type      | credentials : account |
|--------------|----------------------|-----------------------|
| local        | username, [password] | 1:1 (?)               |
| standard     | email, [passkey]     | many:1                |

- homelab uses local accounts with this kind of credentials:
  - Initially only username. We'll add password later, but it'll probably remain optional.
  - Once supported, passwords can be added to accounts that do not yet have one.
  - When added, passwords will be stored following [OWASP guidelines](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).
  - Identifier (for use in DB table, Kotlin class etc.): `LocalCredentials`.
- Local accounts are entirely managed within wasmo. In contrast, standard accounts have an external
  component: email address (email account is hosted elsewhere), passkey (public hostname with SSL
  Certificate for https://).
- Supported account type (standard vs. local) is fixed at build time via the distribution's
  `WasmoService.Config`; needed implementations are provided via DI bindings (separate
  `WasmoServiceGraph` per distribution; Jesse has an idea for how to structure this, TBC).
- Account migration between instance types is not supported.
- Exposing homelab to the Internet is not Wasmo's responsibility. Can be done via home VPN.
- Support for local accounts involves a new DB schema revision. The initial (admin) account is set
  up as part of that schema migration. Initially, it has no password.

Ideas (not final)

- Local account recovery: Each account has a parent account that can reset its password.
  - When an account's password is reset, its children's passwords remain valid.
  - Question: How many levels of hierarchy?
    - (a) `admin` creates all other accounts
    - (b) `admin` creates parent accounts, parent accounts create child accounts.
    - (c) arbitrary nesting (any account can create sub-accounts)
- Open question: 1:1 vs. many:1 mapping for LocalCredentials : local account?


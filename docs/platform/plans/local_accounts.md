Local Accounts
==============

Per instance type, there is one type of account: local (homelab) or external (sandbox, hosted):

| instance type | host URL                                              | account type |
|---------------|-------------------------------------------------------|--------------|
| homelab       | http://jesse123.wasmo.local or http://wasmo.localhost | local        |
| sandbox       | https://jesse123.wasmo.dev                            | external     |
| hosted        | https://jesse123.wasmo.com                            | external     |

Authn depends on the account type:

| account type | authn                | authn : account |
|--------------|----------------------|-----------------|
| local        | username, [password] | 1:1 (?)         |
| external     | email, [passkey]     | many:1          |

- homelab uses local accounts (password authn)
  - username is required, password is optional! In the first step, we won't even have UI to set one.
  - optional passwords will be stored following [OWASP guidelines](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).
  - name for this kind of authn in our code: "password", despite the password part being optional.
    "localauthn" would sound clunky, "username" would suggest that there's never a password.
- Local accounts are entirely managed within wasmo. External accounts aren't necessarily entirely
  external, but they have a public / third party component: Externally hosted email account, or SSL
  Certificate for public hostname for passkeys (instance types with external accounts use https://).
- Account type (external vs. local) is fixed at build time via a hard coded `WasmoService.Config`.
- Account migration between instance types is not supported.
- Exposing homelab to the Internet is not Wasmo's responsibility. Can be done via home VPN.

Ideas (not final)

- Local account recovery: Each account has a parent account that can reset its password.
  - When an account's password is reset, its children's passwords remain valid.
  - Question: How many levels of hierarchy?
    - (a) `admin` creates all other accounts
    - (b) `admin` creates parent accounts, parent accounts create child accounts.
    - (c) more levels?
- `admin` account is the root. Initially it has no password.
- Open question: 1:1 vs. many:1 username/password combo : local account?


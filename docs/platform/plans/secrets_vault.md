Secrets Vault
=============

A key-value store of secrets.

It has a `getOrCreate()` API if you need to provision a new secret.

It can do secure-enclave style `Ed25519` signing, where the application can create a key pair,
validate a signature, but never exfiltrate the private key.

Our secrets should use authenticated encryption.

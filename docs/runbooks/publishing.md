GPG Publishing
==============

We followed the [Maven Central GPG guide].

This is the signing key we use to sign artifacts published to Maven Central:

```
-----BEGIN PGP PUBLIC KEY BLOCK-----

mDMEafNczhYJKwYBBAHaRw8BAQdADGCP6FhAtTvgVb0zd6M/xnfkFo36pDdj7SCO
cZpXxb+0KVdhc21vIFB1Ymxpc2ggSm9iIDxwdWJsaXNoLWpvYkB3YXNtby5jb20+
iLUEExYKAF0bFIAAAAAABAAObWFudTIsMi41KzEuMTIsMCwzAhsDBQsJCAcCAiIC
BhUKCQgLAgQWAgMBAh4HAheAFiEE9PpHMBTeNxViazHw+VPVyv4Lj3AFAmnzXScF
CRLMA1kACgkQ+VPVyv4Lj3DlOwD+LgwS4Y0BmThona8ytWIH2wc7luqkr7XkFw2D
mxEvpw0A+gMBSuc6JqfW9+ttmn70pYKkqbN3BVt0vrOPesEituwFuDgEafNczhIK
KwYBBAGXVQEFAQEHQDC1JpIMSCtHRCXJhjwAjLiQqfGXxKHPxlCUhDmqLAUOAwEI
B4iaBBgWCgBCFiEE9PpHMBTeNxViazHw+VPVyv4Lj3AFAmnzXM4bFIAAAAAABAAO
bWFudTIsMi41KzEuMTIsMCwzAhsMBQkFo5qAAAoJEPlT1cr+C49wJ34BANdYFDeA
rLttqmjZxHCn+pgVLRvao7GHPR2i6G9uLTrKAQDWtEKqVyXrHEythuJp2x5YTbN8
oHAM5JKPUgU05IQGBA==
=flAe
-----END PGP PUBLIC KEY BLOCK-----
```

It expires on `2029-04-29`.

If you’d like to check a dependency, import it like this:

```
gpg --keyserver keys.openpgp.org --recv-keys F4FA473014DE3715626B31F0F953D5CAFE0B8F70
```


[Maven Central GPG guide]: https://central.sonatype.org/publish/requirements/gpg

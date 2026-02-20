Development Setup: Services
===========================

For best connectivity, configure all the different services Wasmo uses.

I put secrets in `~/.bash_profile`. It's necessary to restart the shell or IntelliJ to cause new
environment variables to be picked up.


Fly.io
------

```bash
brew install flyctl
```


Backblaze
---------

```
export B2_REGION_ID=xx-xxxx-000
export B2_APPLICATION_KEY_ID=0000000000000000000000000
export B2_APPLICATION_KEY=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
export B2_BUCKET=wasmo-development
```

Postmark
--------

```
export POSTMARK_SERVER_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

Stripe
------

```
export STRIPE_PUBLISHABLE_KEY=pk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
export STRIPE_SECRET_KEY=sk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

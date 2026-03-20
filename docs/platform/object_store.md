Object Store
============

Wasmo has an S3-compatible service.

We don’t expose the full capabilities of Amazon S3 because we want to support multiple independent
implementations. For example, you cannot set arbitrary permissions on files; everything is
private to the application.

Job Service
===========

Applications can ask to be called back in the future. This may be implemented itself in the
guest layer, using the SQLite DB to store job metadata.

We should have a maximum parallelism permitted by any application, to avoid fork bombs.



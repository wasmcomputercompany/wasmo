App Updates
===========

After an app update, we call `WasmoApp.afterInstall()` with information about the previous and
current installation version. If this command fails, we fail the update.

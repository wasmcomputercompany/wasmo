Files
=====

The `/tmp` directory is exposed as a preopen via the WASI filesystem API.


Use Cases
---------

Data written to the filesystem is temporary. Use the object store and SQL services for persistent
storage. Wasmo will periodically clear temporary directories to limit resource usage. (Temporary
directories are not cleared while application is executing.)

These temporary directories are intended to support a few use cases.

### As a Temporary Workspace

For example, an application may write an uploaded file to a temporary directory, manipulate it on
disk, and then transfer it back to the network or to the object store.

### As a Disk Cache

An application may fetch assets from the network and store them to the temporary directory.

Applications should prefer the filesystem over the object store for this use case. The filesystem
is not redundantly persisted, which makes it faster and cheaper.


Directory Structure
-------------------

Immediate children of `/tmp` must be directories. Those directories' names must be between 1 and 15
lowercase ASCII characters (a-z) or digits (0-9). The directory name's first character must not be a
digit.


Eviction
--------

Each immediate child of `/tmp` is managed and evicted independently. Wasmo uses the directory’s size
and access history to inform what to evict. Applications should use different temporary directories
for different data: that way the OS can evict what isn't used without evicting what is used. When
a temporary directory is evicted, it is deleted completely and atomically.


Sandboxed By Construction
-------------------------

Applications cannot see each others' files or use the file system to exchange data.


Developer Access
----------------

Each application's files are mapped to the host computer's file system, such as in
`~/.wasmo/jesse99/journal/tmp`. The path contains the computer slug and app slug.


WASI FileSystem API
-------------------

Wamso implements WASI filesystem APIs.

The only permitted children of this path are directories that follow the naming convention above.
Attempts to create regular files will fail.

Applications cannot access files or directories outside of `/tmp`.

Only files and directories are permitted - no symlinks, sockets, or other file types.

Limitations of the host computer's filesystem impact Wasmo applications:

> The directory separator is always the forward slash.
> Paths may be case-folded or not.
> Deleting (unlinking) a file may fail if there are other file descriptors open.


Transfer Service
----------------

Wasmo’s `TransferService` API can move files between the network, object store, and file system.

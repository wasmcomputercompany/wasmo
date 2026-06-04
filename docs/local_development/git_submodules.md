Git Submodules
==============

We vendor in [Pico] and [WASI] with git submodules.

Set it up like so:

```bash
$ git submodule init
$ git submodule update
```

You can make it automatic:

```bash
$ git config --global submodule.recurse true
```

[Pico]: https://picocss.com/
[WASI]: https://github.com/WebAssembly/WASI

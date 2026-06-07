WIT
===

A Kotlin implementation of a WIT parser.

See the [Explainer], [Overview] and [Spec].

This module contains documentation and specifications copyrighted by the
[W3C WebAssembly Community Group], licensed under the Apache license.

* **wit-core**: Parses and models `.wit` files.
* **wit-kotlin**: A runtime library for running components in Kotlin. It declares built-in types
  for `Tuple` and `Result` types.
* **wit-kotlin-generator**: uses parsed `.wit` files to generate `.kt` files.
* **wit-testing**: test facets for our own internal testing.

[Explainer]: https://github.com/WebAssembly/component-model/blob/main/design/mvp/Explainer.md
[Overview]: https://component-model.bytecodealliance.org/design/wit.html
[Spec]: https://github.com/WebAssembly/component-model/blob/main/design/mvp/WIT.md
[W3C WebAssembly Community Group]: https://www.w3.org/community/webassembly/

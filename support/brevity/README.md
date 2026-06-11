Brevity
=======

> _‘brevity is the soul of wit’_
>  –– Polonius in Shakespeare’s Hamlet

This is a Kotlin implementation of a WIT processor.

See the [Explainer], [Overview] and [Spec].

This project contains documentation and specifications copyrighted by the
[W3C WebAssembly Community Group], licensed under the Apache license.

* **wit-core**: Parses and models `.wit` files.
* **wit-kotlin**: A runtime library for running components in Kotlin. It declares built-in types
  for `Tuple` and `Result` types.
* **wit-kotlin-generator**: uses parsed `.wit` files to generate `.kt` files.
* **wit-testing**: test facets for our own internal testing.

Models
------

We have several different representations of the `.wit` code, that fit together in a pipeline.

* **io**: a direct representation of the `.wit` source code. This isn’t linked and so type
  references are just strings and not resolved. Use `IoWitPackageReader` to load this model.
* **ir**: a linked representation of an entire project. Type references are resolved to their
  fully-qualified values. Includes are applied, so worlds contain their full transitive
  dependencies. This representation doesn’t model syntactic sugar like `Use` and `Include`. Use
  `IrMapper` to transform `io` into this model.
* **kt**: an general-purpose Kotlin model of the project, with Kotlin names for all symbols. Use
  `KtMapper` to transform `ir` into this model.
* **api**: a user-facing Kotlin API for a project, represented as `KotlinPoet` files.


[Explainer]: https://github.com/WebAssembly/component-model/blob/main/design/mvp/Explainer.md
[Overview]: https://component-model.bytecodealliance.org/design/wit.html
[Spec]: https://github.com/WebAssembly/component-model/blob/main/design/mvp/WIT.md
[W3C WebAssembly Community Group]: https://www.w3.org/community/webassembly/

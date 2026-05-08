Design and Style
================

We use a custom build of [Pico], compiled with [Sass].

Breakpoints
-----------

We use CSS breakpoints for a responsive-width content container. Apply the `.ContentWidth` class to
a `<div>` to apply this. Our width breakpoints are almost the same as Pico’s defaults, but:

* Our pixel sizes are always a multiple of 4. This is so the 4-column launcher grid packs nicely.
* We cap out at 'large', so our viewport never exceeds 948px and our font size never exceeds 19px.

| Screen Width | Content Width |
 |:-------------|:--------------|
| < 576px      | 344px         |
| >= 576px     | 508px         |
| >= 768px     | 700px         |
| >= 1024px    | 948px         |

Custom Pico
-----------

Our Pico settings are in our `pico-for-wasmo.scss` file. The inventory of available options is in
Pico’s `scss/_settings.scss`.

We use the [Freefair Gradle Plugin] to compile the Pico `.scss` into `.css`.




[Freefair Gradle Plugin]: https://docs.freefair.io/gradle-plugins/current/reference/#_sass_tasks
[Pico]: https://picocss.com/
[Sass]: https://sass-lang.com/install/

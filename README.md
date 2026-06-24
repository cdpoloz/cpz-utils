# cpz-utils

`cpz-utils` is a small Java utility library for reusable application logic.

It contains pure Java utilities for time, packed colors, and coherent noise
generation and progression. The project is intentionally framework-agnostic:
it does not depend on Processing, rendering APIs, UI toolkits, or external
libraries.

---

## Why cpz-utils?

`cpz-utils` exists to keep common low-level behavior reusable across projects
without coupling that behavior to a specific application framework.

The library is useful when a project needs shared utilities for timing, color
packing, or noise-state progression, but the integration layer should remain
outside the utility code. Projects that use Processing, a game loop, a UI
framework, or a custom runtime can adapt these utilities from the outside.

---

## What It Provides

- `com.cpz.utils.time`: stateful time utilities backed by an injectable
  monotonic `TimeSource`.
- `com.cpz.utils.color`: static helpers for packed ARGB `int` colors.
- `com.cpz.utils.noise`: seeded multidimensional noise generation and stateful
  one-dimensional noise progression.

The current codebase does not provide rendering, Processing adapters, color
objects, dependency injection frameworks, or application architecture
components.

---

## Design Principles

- Pure Java utilities.
- No framework lock-in.
- No dependency on Processing.
- Small, focused abstractions.
- Explicit APIs over hidden behavior.
- Reusable across different projects.
- Integration-specific adapters live outside the library core.
- No unnecessary external dependencies.
- Clear separation between core logic and integration layers.

---

## Modules Overview

### time

The `time` package provides small stateful utilities for monotonic time-based
logic:

- `TimeSource`: functional interface returning monotonic nanoseconds.
- `SystemTimeSource`: `TimeSource` backed by `System.nanoTime()`.
- `Timer`: period pulses, elapsed checks, on/off phases, and duty-cycle phases.
- `Stopwatch`: elapsed-time measurement with stop, resume, reset, and restart.
- `Countdown`: remaining-time measurement toward expiration.
- `FixedStepTimer`: fixed-step accumulation for simulation-style loops.

All time classes expose public durations in milliseconds where applicable, while
using nanoseconds internally.

### color

The `color` package currently contains `Colors`, a static utility class for
packed ARGB integers in `0xAARRGGBB` format.

It supports:

- RGB, ARGB, grayscale, and percentage-based construction.
- Alpha replacement.
- Alpha, red, green, and blue channel reading.
- Direct interpolation between two packed ARGB colors.

It does not provide CSS parsing, named palettes, gradients, HSL/HSV conversion,
or rendering integration.

### noise

The `noise` package separates noise sampling, generation, and stateful
progression. It provides seeded Improved Perlin Noise and normalized fractal
Brownian motion (fBm), while remaining independent from Processing and external
libraries.

Current public types:

- `NoiseSource`: functional interface for one-dimensional noise lookup.
- `NoiseField`: interface for one-dimensional, two-dimensional, and
  three-dimensional noise lookup.
- `PerlinNoise`: deterministic Improved Perlin Noise normalized to `[0, 1]`.
- `FractalNoise`: normalized fBm over another `NoiseField`.
- `NoiseValue`: stateful holder for `noiseSource`, `position`, and `speed`.
- `NoiseVector3`: composition of three independent `NoiseValue` instances.
- `Vector3f`: immutable record for three float components.

`NoiseValue` advances its position with `update()`, reads with `get()`, and can
be repositioned with `reset(float)`. `NoiseVector3` applies the same idea to
three independent one-dimensional values; it is not spatial 3D noise.

---

## Project Structure

```text
cpz-utils/
├── LICENSE
├── README.md
├── docs/
│   ├── colors.md
│   ├── countdown.md
│   ├── fixed-step-timer.md
│   ├── noise.md
│   ├── stopwatch.md
│   └── timer.md
└── src/
    └── com/
        └── cpz/
            └── utils/
                ├── color/
                │   └── Colors.java
                ├── noise/
                │   ├── FractalNoise.java
                │   ├── NoiseField.java
                │   ├── NoiseSource.java
                │   ├── NoiseValue.java
                │   ├── NoiseVector3.java
                │   ├── PerlinNoise.java
                │   └── Vector3f.java
                └── time/
                    ├── Countdown.java
                    ├── FixedStepTimer.java
                    ├── Stopwatch.java
                    ├── SystemTimeSource.java
                    ├── TimeSource.java
                    └── Timer.java
```

IDE metadata may be present in local checkouts, but it is not part of the
runtime API.

---

## Maven

After release `0.2.3` is published, the artifact will be available from Maven
Central with this dependency:

```xml
<dependency>
    <groupId>io.github.cdpoloz</groupId>
    <artifactId>cpz-utils</artifactId>
    <version>0.2.3</version>
</dependency>
```

---

## Minimal Examples

### Time

```java
import com.cpz.utils.time.FixedStepTimer;

FixedStepTimer timer = new FixedStepTimer(20);
timer.start();

while (timer.pollStep()) {
    updateSimulation(); // fixed-step update
}
```

```java
import com.cpz.utils.time.Stopwatch;

Stopwatch stopwatch = new Stopwatch();
stopwatch.start();

// work to measure

stopwatch.stop();
long elapsedMillis = stopwatch.getElapsedMillis();
```

### Noise

```java
import com.cpz.utils.noise.PerlinNoise;
import com.cpz.utils.noise.NoiseValue;

PerlinNoise source = new PerlinNoise(1234L);

NoiseValue value = new NoiseValue(source, 0.0f, 0.01f);
value.update();

float current = value.get();
```

```java
import com.cpz.utils.noise.NoiseSource;
import com.cpz.utils.noise.NoiseVector3;
import com.cpz.utils.noise.Vector3f;

NoiseSource source = x -> (float) Math.sin(x);

NoiseVector3 vector = new NoiseVector3(source, 0.0f, 0.01f);
vector.reset(0.0f, 10.0f, 20.0f);
vector.setSpeed(0.01f, 0.02f, 0.03f);
vector.update();

Vector3f current = vector.get();
```

### Color

```java
import com.cpz.utils.color.Colors;

int base = Colors.rgb(40, 120, 220);
int overlay = Colors.withAlpha(96, base);

int red = Colors.red(overlay);
int alpha = Colors.alpha(overlay);
```

---

## Module Notes

### time

Use `Timer` when the required behavior is period-oriented: pulses, finished
checks, on/off phases, or duty-cycle phases.

Use `Stopwatch` for elapsed time measurement, `Countdown` for remaining time
toward zero, and `FixedStepTimer` when elapsed time should be consumed as fixed
simulation steps.

All time utilities can use a custom `TimeSource`, which makes them easier to
test or drive from a deterministic runtime.

### color

`Colors` treats colors as packed `int` values in `0xAARRGGBB` format. This keeps
the API compact and easy to pass between rendering layers, adapters, or data
objects.

Construction inputs are clamped to their expected ranges. Channel readers simply
extract bytes from the packed integer.

### noise

`NoiseSource` is the integration point for an actual noise function. It can be a
lambda, test stub, deterministic function, or adapter to an external algorithm.

`NoiseField` extends that contract to two-dimensional and three-dimensional
sampling. `PerlinNoise` provides deterministic Improved Perlin Noise in
`[0, 1]`, while `FractalNoise` combines octaves from any `NoiseField` as a
normalized weighted sum.

`NoiseValue` stores one position and one speed. Calling `update()` advances the
position by the speed. Calling `get()` reads the noise value at the current
position without mutating state.

`NoiseVector3` is not a 3D noise algorithm. It is a small composition wrapper
around three independent `NoiseValue` instances and returns immutable `Vector3f`
snapshots. See [Noise](docs/noise.md) for the complete range, dimensional, and
validation contracts.

---

## Relationship With Other Projects

`cpz-utils` is the foundational layer for the CPZ ecosystem, providing shared logic
across UI, simulation, and tooling projects.

Other projects, such as `cpz-mvvm-processing-template`, integrate these utilities
through adapters. Those adapters belong to the consuming project, not
to the core of `cpz-utils`.

---

## Documentation

Module-level documentation lives in `docs/`:

- [Colors](docs/colors.md)
- [Timer](docs/timer.md)
- [Stopwatch](docs/stopwatch.md)
- [Countdown](docs/countdown.md)
- [FixedStepTimer](docs/fixed-step-timer.md)
- [Noise](docs/noise.md)

The README is the project entry point. The files in `docs/` provide more detail
about class behavior, state semantics, and non-goals.

---

## Status / Scope

The current scope is intentionally small:

- time utilities
- packed ARGB color helpers
- seeded multidimensional noise fields, fractal noise, and one-dimensional
  noise-state helpers

Out of scope for the current project:

- Processing dependencies or built-in Processing adapters
- rendering utilities
- UI components
- additional noise algorithms beyond the current Perlin and fBm implementations
- color-space conversion systems
The API is stable for current modules, but the library is still evolving.

---

## License

`cpz-utils` is released under the Apache License, Version 2.0. See [LICENSE](LICENSE).

---

## Author

**Carlos Polo Zamora**  
GitHub: https://github.com/cdpoloz  
Alias: CPZ / cepezeta / cdpoloz

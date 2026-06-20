# Noise

## Purpose

`com.cpz.utils.noise` is a framework-agnostic package for generating,
progressing, and adapting coherent noise values in pure Java.

The package separates three responsibilities:

- `NoiseSource` and `NoiseField` define sampling contracts.
- `PerlinNoise` and `FractalNoise` generate noise values.
- `NoiseValue` and `NoiseVector3` store and advance one-dimensional sampling
  positions.

Adapters for external frameworks remain in consuming projects. The package does
not depend on Processing, AWT, rendering APIs, or external noise libraries.

## NoiseSource

`NoiseSource` is a functional interface for one-dimensional noise lookup:

```java
float noise(float x);
```

It remains the integration point for a lambda, test stub, deterministic
function, or external adapter:

```java
NoiseSource source = x -> (float) Math.sin(x);
```

`NoiseSource` does not impose a general output range. Each implementation
defines its own range and sampling behavior. Its single abstract method keeps it
compatible with existing lambdas and adapters.

## NoiseField

`NoiseField` extends `NoiseSource` with two-dimensional and three-dimensional
sampling:

```java
float noise(float x);
float noise(float x, float y);
float noise(float x, float y, float z);
```

Because it has three sampling methods, `NoiseField` is not a functional
interface. It can still be passed to any API that accepts `NoiseSource`, such as
`NoiseValue` or `NoiseVector3`.

The interface does not define an output range, periodicity, or relationship
between dimensional overloads. Those properties belong to each implementation.

## PerlinNoise

`PerlinNoise` is an immutable, deterministic implementation of Improved Perlin
Noise. It implements `NoiseField` and requires an explicit `long` seed:

```java
import com.cpz.utils.noise.PerlinNoise;

PerlinNoise noise = new PerlinNoise(1234L);
float value = noise.noise(0.25f);
```

The same seed and coordinates produce the same result. Internally, the seed is
used to generate a 256-entry permutation with Fisher-Yates and SplitMix64. The
permutation is duplicated for indexed lookup, and the resulting field is
periodic every 256 cells along each axis.

One-dimensional and two-dimensional samples are slices of the same
three-dimensional field:

- `noise(x)` samples `(x, 0, 0)`.
- `noise(x, y)` samples `(x, y, 0)`.
- `noise(x, y, z)` samples the full three-dimensional field.

For example:

```java
import com.cpz.utils.noise.PerlinNoise;

PerlinNoise noise = new PerlinNoise(1234L);

float value2D = noise.noise(12.5f, -3.25f);
float value3D = noise.noise(12.5f, -3.25f, 8.75f);
```

All coordinates must be finite. Passing `NaN` or positive or negative infinity
throws `IllegalArgumentException`.

`PerlinNoise` is final and immutable. Its permutation is created during
construction and is not exposed or modified, so instances are safe to share
between threads.

## Perlin Output Range

`PerlinNoise` converts the underlying signed field to a normalized value and
clamps the result to `[0, 1]`. A normalized value of `0.5` corresponds to zero in
the underlying signed field.

Code that needs a signed value can convert it explicitly:

```java
float normalizedValue = noise.noise(0.25f);
float signedValue = normalizedValue * 2.0f - 1.0f;
```

This range is specific to `PerlinNoise`; it is not a general guarantee of
`NoiseSource` or `NoiseField`.

## FractalNoise

`FractalNoise` implements normalized fractal Brownian motion (fBm). It wraps any
`NoiseField` and does not depend directly on `PerlinNoise`.

```java
import com.cpz.utils.noise.FractalNoise;
import com.cpz.utils.noise.PerlinNoise;

PerlinNoise base = new PerlinNoise(1234L);
FractalNoise fractal = new FractalNoise(
        base,
        5,
        1.0f,
        2.0f,
        0.5f
);

float value = fractal.noise(0.25f, 0.5f, 0.75f);
```

The constructor parameters are:

- `source`: non-null noise field sampled by every octave.
- `octaves`: number of octaves, at least `1`.
- `frequency`: finite initial frequency greater than `0`.
- `lacunarity`: finite frequency multiplier greater than `1`.
- `persistence`: finite weight multiplier in `[0, 1]`.

The first octave uses the configured frequency and a weight of `1`. After each
octave, frequency is multiplied by `lacunarity` and weight is multiplied by
`persistence`. The accumulated values are divided by the accumulated weights,
forming a normalized weighted sum.

With one octave, the result is the wrapped source sampled at the initial
frequency. A persistence of `0` uses only the first octave. A persistence of `1`
assigns equal weight to every octave.

## Fractal Output Range

`FractalNoise` does not impose a fixed range. With finite non-negative weights,
its normalized weighted sum preserves the bounds of a finite, bounded source.
For example:

- a source bounded to `[0, 1]` remains in `[0, 1]`;
- a source bounded to `[-1, 1]` remains in `[-1, 1]`;
- `FractalNoise` wrapping `PerlinNoise` remains in `[0, 1]`.

Input coordinates must be finite. `FractalNoise` also throws
`IllegalArgumentException` if an octave produces a scaled coordinate that
cannot be represented as a finite `float`. If the wrapped source returns `NaN`
or infinity, it throws `IllegalStateException`.

`FractalNoise` is final and immutable. It is safe to share between threads when
its wrapped `NoiseField` is also safe to use concurrently.

## NoiseValue

`NoiseValue` is a stateful holder for a position on a one-dimensional noise
axis. It stores:

- `NoiseSource noiseSource`
- `float position`
- `float speed`

The source must not be `null`. The position is advanced by the current speed
when `update()` is called:

```java
position += speed;
```

`get()` calls `noiseSource.noise(position)` without changing state. It does not
normalize, clamp, wrap, or otherwise reinterpret the returned value.

Because `NoiseField` extends `NoiseSource`, `PerlinNoise` can be used directly:

```java
import com.cpz.utils.noise.NoiseValue;
import com.cpz.utils.noise.PerlinNoise;

PerlinNoise noise = new PerlinNoise(1234L);
NoiseValue value = new NoiseValue(noise, 0.0f, 0.01f);

value.update();
float current = value.get();
```

`reset(float newPosition)` changes the current position without changing the
speed or replacing the source. Speed may be positive, negative, or zero.

## NoiseVector3

`NoiseVector3` composes three independent `NoiseValue` instances. Each component
uses the same `NoiseSource`, but owns a separate one-dimensional position and
speed.

```java
NoiseSource source = x -> (float) Math.sin(x);
NoiseVector3 vector = new NoiseVector3(source, 0.0f, 0.01f);

vector.reset(0.0f, 10.0f, 20.0f);
vector.setSpeed(0.01f, 0.02f, 0.03f);
vector.update();

Vector3f current = vector.get();
float x = current.x();
float y = current.y();
float z = current.z();
```

`NoiseVector3` is not spatial three-dimensional noise. It performs three
independent one-dimensional lookups and returns their values as an immutable
`Vector3f` snapshot. Use `NoiseField.noise(x, y, z)` when the required operation
is a single sample from a spatial three-dimensional field.

`Vector3f` is a minimal immutable record. It contains three `float` components
and does not provide vector arithmetic.

## Processing Adapters

Processing integration remains outside `cpz-utils`. A consuming project may
implement `NoiseSource` by delegating to `PApplet.noise(float)`, as existing
`ProcessingNoiseSource` adapters do.

Those adapters continue to work with `NoiseValue` and `NoiseVector3`. Their
`[0, 1]` output is a property of Processing, not a general `NoiseSource`
contract. No Processing type or dependency is required by this package.

## Main Types

- `NoiseSource`: functional one-dimensional noise source.
- `NoiseField`: one-dimensional, two-dimensional, and three-dimensional field.
- `PerlinNoise`: seeded Improved Perlin Noise in `[0, 1]`.
- `FractalNoise`: normalized fBm over another `NoiseField`.
- `NoiseValue`: mutable one-dimensional position and speed.
- `NoiseVector3`: three independent `NoiseValue` components.
- `Vector3f`: immutable three-component snapshot.

## Non-goals

The package does not provide:

- Processing adapters inside the library;
- rendering or visual utilities;
- vector arithmetic;
- configurable tiling periods;
- simplex, ridged, or domain-warped noise;
- random position generation inside `NoiseValue` or `NoiseVector3`.

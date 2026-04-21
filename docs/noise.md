# Noise

## Purpose

`com.cpz.utils.noise` is a small framework-agnostic package for working with
noise values through an injected one-dimensional source.

This package does not implement a noise algorithm. It stores and advances
one-dimensional positions, then asks a user-provided `NoiseSource` for the value
at each position.

Typical uses include:

- wrapping an existing one-dimensional noise function
- advancing a noise position by a fixed speed
- reading the current noise value without mutating state
- resetting the position explicitly

The package does not depend on Processing, AWT, rendering APIs, or external
noise libraries.

## Noise Source

`NoiseSource` is a functional interface for one-dimensional noise lookup:

```java
float noise(float x);
```

It abstracts the source of noise values. The implementation can be a lambda, a
test stub, a deterministic function, or an adapter owned by the consuming
project. `cpz-utils` does not provide Perlin, simplex, random, fractal, or
Processing-backed noise in this package.

```java
NoiseSource source = x -> (float) Math.sin(x);
```

## Noise Value

`NoiseValue` is a small stateful holder for a position on a one-dimensional
noise axis. It stores:

- `NoiseSource noiseSource`
- `float position`
- `float speed`

The injected `NoiseSource` must not be `null`. Passing `null` to any constructor
throws `IllegalArgumentException` with the message
`noiseSource must not be null`.

Available constructors include:

- `NoiseValue(NoiseSource noiseSource)`
- `NoiseValue(NoiseSource noiseSource, float initialPosition)`
- `NoiseValue(NoiseSource noiseSource, float initialPosition, float speed)`

The one-argument constructor starts at position `0.0f` with speed `0.0f`. The
two-argument constructor uses the provided initial position with speed `0.0f`.

## Position And Speed

`position` is the current coordinate passed to `NoiseSource.noise(float x)`.
`NoiseValue` does not clamp, wrap, randomize, or otherwise reinterpret it.

`speed` is the amount added to `position` when `update()` is called. Speed may
be positive, negative, or zero.

```java
NoiseValue value = new NoiseValue(source, 10.0f, -0.25f);

value.update(); // position becomes 9.75f
```

## Update And Read

`update()` advances the internal position by the current speed:

```java
position += speed;
```

`get()` returns `noiseSource.noise(position)` for the current position. It does
not mutate the position, speed, or source.

```java
NoiseSource source = x -> x * 2.0f;
NoiseValue value = new NoiseValue(source, 1.0f, 0.5f);

float before = value.get(); // 2.0f

value.update();

float after = value.get(); // 3.0f
```

## Reset

`reset(float newPosition)` explicitly repositions the value on the
one-dimensional axis. It does not change the speed or replace the noise source.

```java
NoiseValue value = new NoiseValue(source, 0.0f, 0.01f);

value.update();
value.reset(25.0f);

float current = value.get();
```

## Basic Usage

```java
NoiseSource source = x -> (float) Math.sin(x);

NoiseValue value = new NoiseValue(source, 0.0f, 0.01f);

value.update();
float current = value.get();
```

## NoiseVector3

`NoiseVector3` is a small three-dimensional container composed of three
independent `NoiseValue` instances:

- one `NoiseValue` for x
- one `NoiseValue` for y
- one `NoiseValue` for z

Each component uses the same injected `NoiseSource`, but each axis owns its own
`NoiseValue` state. Calling `update()` updates x, y, and z. Calling `get()`
returns a new immutable `Vector3f` with the current component values.

`NoiseVector3` does not use Processing, `PVector`, arrays, collections, or an
external vector dependency. It is only a minimal composition wrapper around
three one-dimensional `NoiseValue` objects.

```java
NoiseSource source = x -> (float) Math.sin(x);

NoiseVector3 vector = new NoiseVector3(source, 0.0f, 0.01f);

vector.reset(0.0f, 10.0f, 20.0f);
vector.setSpeed(0.01f, 0.02f, 0.03f);

vector.update();

Float3 current = vector.get();
float x = current.x();
float y = current.y();
float z = current.z();
```

## Main Methods

Main public operations include:

- `NoiseSource.noise(float x)`
- `NoiseValue(NoiseSource noiseSource)`
- `NoiseValue(NoiseSource noiseSource, float initialPosition)`
- `NoiseValue(NoiseSource noiseSource, float initialPosition, float speed)`
- `update()`
- `get()`
- `reset(float newPosition)`
- `getPosition()`
- `getSpeed()`
- `setSpeed(float speed)`
- `NoiseVector3(NoiseSource noiseSource)`
- `NoiseVector3(NoiseSource noiseSource, float initialPosition)`
- `NoiseVector3(NoiseSource noiseSource, float initialPosition, float speed)`
- `NoiseVector3.update()`
- `NoiseVector3.get()`
- `NoiseVector3.reset(float px, float py, float pz)`
- `NoiseVector3.setSpeed(float speed)`
- `NoiseVector3.setSpeed(float sx, float sy, float sz)`
- `NoiseVector3.getSpeed()`
- `Float3(float x, float y, float z)`

## Notes / Non-goals

`com.cpz.utils.noise` does not provide:

- 2D or N-dimensional vector containers
- vector math
- octaves
- seed management
- fractal noise
- Processing adapters
- visual utilities
- random value generation inside `NoiseValue`

Those features are outside the scope of this version. The package stays focused
on minimal, explicit state holders backed by an injected one-dimensional noise
source.

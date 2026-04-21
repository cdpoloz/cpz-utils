# Stopwatch

## Purpose

`Stopwatch` is a small stateful utility for measuring elapsed time in Java
applications. It reads time through a `TimeSource`, which makes it usable
with the system clock, tests, simulations, or custom time flows.

Internally, `Stopwatch` uses monotonic nanosecond time based on
`System.nanoTime()` through `SystemTimeSource`. Its public API exposes elapsed
time in milliseconds for simplicity.

Use `Stopwatch` when the required behavior is a simple chronometer:

- start measuring elapsed time
- stop measuring while preserving the measured value
- resume measuring from the preserved value
- reset or restart the measurement
- query elapsed time

`Stopwatch` does not model periods, pulses, on/off phases, or duty cycles. Use
`Timer` for those behaviors. Use `Countdown` when the required behavior is a
configured duration that counts down toward expiration. Use `FixedStepTimer`
when elapsed time must be accumulated and consumed as fixed simulation steps.

## Time Source

By default, `Stopwatch` uses `SystemTimeSource`, which delegates to
`System.nanoTime()`.

A custom `TimeSource` can be injected when deterministic time is needed:

```java
Stopwatch stopwatch = new Stopwatch(() -> simulatedNanos);
```

## Stateful Behavior

`Stopwatch` stores a small explicit state: start time, preserved elapsed time,
and running state.

The stopwatch has two states: running and stopped.

When running, it measures elapsed time from the current start plus any preserved
elapsed time from previous runs. When stopped, it does not read the time source
and `getElapsedMillis()` returns the preserved elapsed time.

Stopping a stopwatch preserves the measured value. It is not the same as
resetting it.

- `start()` starts measuring if the stopwatch is stopped. If elapsed time was
  previously preserved by `stop()`, measuring resumes from that value. Calling
  `start()` while already running has no effect.
- `stop()` stops measuring and preserves the elapsed time measured so far.
  Calling `stop()` while already stopped has no effect.
- `reset()` clears elapsed time and leaves the stopwatch stopped.
- `restart()` clears elapsed time and starts measuring from the current time.
- `toggle()` stops the stopwatch when running, or starts it when stopped.
- `getElapsedMillis()` returns elapsed time as a `long` without changing state.
  While running, it reads the current time. While stopped, it returns the
  preserved elapsed time without reading the time source.
- `isRunning()` returns whether the stopwatch is currently measuring time.

## Basic Usage

```java
Stopwatch stopwatch = new Stopwatch();

stopwatch.start();

// work to measure

stopwatch.stop();
long elapsedMillis = stopwatch.getElapsedMillis();
```

## Resume After Stop

```java
Stopwatch stopwatch = new Stopwatch();

stopwatch.start();
stopwatch.stop();

long firstRunMillis = stopwatch.getElapsedMillis();

stopwatch.start();
stopwatch.stop();

long accumulatedMillis = stopwatch.getElapsedMillis();
```

## Restart From Zero

```java
Stopwatch stopwatch = new Stopwatch();

stopwatch.start();

// discard the previous measurement and start again
stopwatch.restart();
```

## Main Methods

Main public operations include:

- `start()`
- `stop()`
- `reset()`
- `restart()`
- `toggle()`
- `getElapsedMillis()`
- `isRunning()`

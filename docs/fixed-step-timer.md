# FixedStepTimer

## Purpose

`FixedStepTimer` is a small stateful utility for converting monotonic elapsed
time into discrete simulation steps of a fixed duration. It reads time through a
`TimeSource`, making it suitable for system time, tests, simulations, or
externally controlled time flows.

Internally, `FixedStepTimer` uses monotonic nanosecond time based on
`System.nanoTime()` through `SystemTimeSource`. Its public API exposes the step
duration in milliseconds, while step counts are exposed as whole counts.

Use `FixedStepTimer` when the required behavior is fixed-step accumulation:

- configure a fixed step duration
- start accumulating elapsed time
- query whether complete steps are available
- consume one or more complete steps explicitly
- preserve sub-step remainder between frames or loop iterations
- reset or restart accumulation

`FixedStepTimer` is not a stopwatch, countdown, or cyclic period timer. Use
`Stopwatch` to measure elapsed time upward, `Countdown` to measure remaining
time toward zero, and `Timer` for period pulses or phase-style behavior.

## Time Source

By default, `FixedStepTimer` uses `SystemTimeSource`, which delegates to
`System.nanoTime()`.

A custom `TimeSource` can be injected when deterministic time is needed:

```java
FixedStepTimer timer = new FixedStepTimer(() -> simulatedNanos, 20);
```

## Stateful Behavior

`FixedStepTimer` stores a small explicit state: last update time, step duration,
accumulated unconsumed time, and running state.

The timer has two states: running and stopped.

When running, it accumulates elapsed monotonic time. When stopped, it does not
read the time source and keeps the accumulated unconsumed time preserved.

A step is available when accumulated unconsumed time is greater than or equal to
the configured step duration. Consuming a step subtracts exactly one step
duration from the accumulated time. Any sub-step remainder is preserved.

- `start()` starts accumulation if the timer is stopped. Existing accumulated
  time, including complete unconsumed steps and sub-step remainder, is
  preserved. Calling `start()` while already running has no effect.
- `stop()` accumulates time up to the current moment, preserves all unconsumed
  accumulated time, and leaves the timer stopped. Calling `stop()` while already
  stopped has no effect.
- `reset()` clears accumulated time and leaves the timer stopped.
- `restart()` clears accumulated time and starts accumulating from the current
  time.
- `toggle()` stops the timer when running, or starts it when stopped.
- `pollStep()` consumes one complete step if one is available. It mutates state
  by updating accumulated time while running and subtracting one step duration.
  It preserves any sub-step remainder.
- `hasStep()` returns whether at least one complete step is available without
  consuming it.
- `getAvailableSteps()` returns the number of complete steps available without
  consuming them.
- `consumeAvailableSteps()` consumes and returns all complete steps currently
  available. It preserves any sub-step remainder.
- `isRunning()` returns whether the timer is currently accumulating elapsed
  time.
- `setStepMillis(int stepMillis)` sets a new fixed step duration, clears
  accumulation, and leaves the timer stopped.

## Query And Consumption

`hasStep()` and `getAvailableSteps()` are query methods. They do not consume
steps and do not change the accumulated remainder. While running, they read the
current time to include elapsed time since the last update. While stopped, they
use only preserved accumulated time and do not read the time source.

`pollStep()` and `consumeAvailableSteps()` are consumption methods. Their names
make the mutation explicit. Both update accumulated time while running before
consuming steps.

## Long Delays

If the host loop stalls or a frame takes a long time, the timer may report many
available steps. This first version does not impose a maximum number of steps
per query or per consumption call. That keeps the class simple and explicit.

Applications that need to avoid a spiral of death should cap the number of
simulation updates they process per frame and decide what to do with any
remaining backlog.

## Preconditions

`stepMillis` must be greater than `0`. Constructors and
`setStepMillis(int stepMillis)` throw `IllegalArgumentException` with the
message `stepMillis must be > 0` when the value is invalid.

The injected `TimeSource` must not be `null`. Passing `null` throws
`IllegalArgumentException` with the message `timeSource must not be null`.

## Basic Usage

```java
FixedStepTimer timer = new FixedStepTimer(20);

timer.start();

while (timer.pollStep()) {
    updateSimulation();
}
```

## Consume All Available Steps

```java
FixedStepTimer timer = new FixedStepTimer(20);

timer.start();

long steps = timer.consumeAvailableSteps();
for (long i = 0; i < steps; i++) {
    updateSimulation();
}
```

## Main Methods

Main public operations include:

- `FixedStepTimer(int stepMillis)`
- `FixedStepTimer(TimeSource timeSource, int stepMillis)`
- `setStepMillis(int stepMillis)`
- `getStepMillis()`
- `start()`
- `stop()`
- `reset()`
- `restart()`
- `toggle()`
- `pollStep()`
- `hasStep()`
- `getAvailableSteps()`
- `consumeAvailableSteps()`
- `isRunning()`

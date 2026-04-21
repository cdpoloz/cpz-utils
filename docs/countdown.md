# Countdown

## Purpose

`Countdown` is a small stateful utility for measuring remaining time until a
configured duration reaches zero. It reads time through a `TimeSource`, making
it suitable for system time, tests, simulations, or externally controlled time
flows.

Internally, `Countdown` uses monotonic nanosecond time based on
`System.nanoTime()` through `SystemTimeSource`. Its public API exposes duration
and remaining time in milliseconds for simplicity.

Use `Countdown` when the required behavior is a simple count toward expiration:

- configure a duration
- start counting down
- query remaining time
- check whether the countdown has expired
- stop while preserving the remaining time
- reset or restart from the full duration

`Countdown` does not model periods, pulses, on/off phases, or duty cycles. Use
`Timer` for those behaviors. Use `Stopwatch` when the required behavior is
measuring elapsed time upward instead of remaining time downward. Use
`FixedStepTimer` when elapsed time must be accumulated and consumed as fixed
simulation steps.

## Time Source

By default, `Countdown` uses `SystemTimeSource`, which delegates to
`System.nanoTime()`.

A custom `TimeSource` can be injected when deterministic time is needed:

```java
Countdown countdown = new Countdown(() -> simulatedNanos, 5000);
```

## Stateful Behavior

`Countdown` stores a small explicit state: start time, full duration, preserved
remaining time, and running state.

The countdown has two observable states: actively running and stopped. An
expired countdown is treated as no longer actively running by `isRunning()`,
even though expiration is derived during queries without mutating internal
state.

When running, remaining time is the preserved remaining time minus the time
since the current start. When stopped, `getRemainingMillis()` returns the
preserved remaining time without reading the time source.

Stopping a countdown preserves the remaining time. It is not the same as
resetting it.

- `start()` starts counting down if the countdown is stopped and has remaining
  time. If remaining time was previously preserved by `stop()`, counting
  resumes from that value. Calling `start()` while already actively running has
  no effect. Calling `start()` after expiration has no effect; use `restart()`
  to begin a new full countdown.
- `stop()` stops counting down and preserves the remaining time. Calling
  `stop()` while already stopped has no effect. If the countdown has already
  expired, the preserved remaining time becomes `0`.
- `reset()` restores the full configured duration as the remaining time and
  leaves the countdown stopped.
- `restart()` restores the full configured duration and starts counting down
  from the current time.
- `toggle()` stops the countdown when it is actively running, or starts it when
  it is stopped and has remaining time. Toggling an expired countdown does not
  restart it.
- `getRemainingMillis()` returns remaining time as a `long` without changing
  state. While running, it reads the current time. While stopped, it returns the
  preserved remaining time without reading the time source. The returned value
  is never negative.
- `isExpired()` returns `true` when remaining time is `0`. It does not mutate
  state.
- `isRunning()` returns `true` only while the countdown is actively counting
  down and remaining time is greater than `0`.
- `setDurationMillis(int durationMillis)` sets a new full duration, restores
  that duration as the remaining time, and leaves the countdown stopped.

## Preconditions

`durationMillis` must be greater than `0`. Constructors and
`setDurationMillis(int durationMillis)` throw `IllegalArgumentException` with
the message `durationMillis must be > 0` when the value is invalid.

The injected `TimeSource` must not be `null`. Passing `null` throws
`IllegalArgumentException` with the message `timeSource must not be null`.

## Basic Usage

```java
Countdown countdown = new Countdown(5000);

countdown.start();

if (countdown.isExpired()) {
    System.out.println("Expired");
}

long remainingMillis = countdown.getRemainingMillis();
```

## Pause And Resume

```java
Countdown countdown = new Countdown(5000);

countdown.start();
countdown.stop();

long preservedRemainingMillis = countdown.getRemainingMillis();

countdown.start();
```

## Restart From Full Duration

```java
Countdown countdown = new Countdown(5000);

countdown.start();

// discard the current remaining time and start from the full duration
countdown.restart();
```

## Main Methods

Main public operations include:

- `Countdown(int durationMillis)`
- `Countdown(TimeSource timeSource, int durationMillis)`
- `setDurationMillis(int durationMillis)`
- `getDurationMillis()`
- `start()`
- `stop()`
- `reset()`
- `restart()`
- `toggle()`
- `getRemainingMillis()`
- `isExpired()`
- `isRunning()`

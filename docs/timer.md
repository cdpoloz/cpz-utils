# Timer

## Purpose

`Timer` is a small stateful utility for time-based logic. It reads time through
a `TimeSource`, making it suitable for system time, tests, simulations, or externally
controlled time flows. Internally, it uses monotonic nanosecond time based on
`System.nanoTime()`, while its public API remains in milliseconds for simplicity.

Typical uses include:

- periodic one-shot pulses
- elapsed time checks
- repeating on/off phases
- duty-cycle phases

For simple chronometer-style elapsed-time measurement with start, stop, reset,
and resume semantics, use `Stopwatch` instead.

For count-toward-zero behavior with remaining time and expiration semantics,
use `Countdown` instead.

For fixed-step simulation updates where elapsed time is accumulated and
consumed as discrete steps, use `FixedStepTimer` instead.

## Time Source

By default, `Timer` uses `SystemTimeSource`, which delegates to
`System.nanoTime()`.

A custom `TimeSource` can be injected when deterministic time is needed:

```java
Timer timer = new Timer(() -> simulatedNanos);
```

## Stateful Behavior

`Timer` stores a small explicit state: start time, period, duty cycle, and
running state.
Several methods read the current time on each call, and some methods may also
reset or advance the internal cycle state.

The timer has two states: running and stopped. When stopped, it does not measure
time, generate pulses, or evaluate phases. `getElapsedMillis()` returns `0` when
the timer is stopped.

- `getElapsedMillis()` returns the elapsed time since the current start as a
  `long`.
- `getPeriodMillis()` returns the configured period as a `long`.
- `isPeriodFinished()` checks whether the configured period has elapsed without
  resetting the period start.
- `pollPeriodPulse()` returns `true` only once per elapsed period and resets the
  internal start time when it returns `true`.
- `isOnOffPhaseActive()` evaluates a repeating on/off phase and may reset the
  phase start after a full on/off cycle.
- `isDutyCyclePhaseActive()` evaluates a duty-cycle phase and may reset the
  phase start after each period.

## Preconditions

`periodMillis` must be greater than `0`. `dutyCycle` is expected in the `0.0` to
`1.0` range; values outside that range are clamped by the implementation.
The timer must be configured with a valid period before being started; `start()`
fails if no period has been configured.

## Basic Usage

```java
Timer timer = new Timer();
timer.start(1000);

if (timer.pollPeriodPulse()) {
    System.out.println("Pulse");
}
```

## Main Methods

Main public operations include:

- `start()`
- `start(int periodMillis)`
- `start(int periodMillis, float dutyCycle)`
- `pollPeriodPulse()`
- `isPeriodFinished()`
- `isOnOffPhaseActive()`
- `isDutyCyclePhaseActive()`
- `getElapsedMillis()`
- `getPeriodMillis()`
- `stop()`
- `toggle()`

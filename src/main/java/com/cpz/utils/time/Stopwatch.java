package com.cpz.utils.time;

/**
 * Stateful stopwatch utility backed by an injectable {@link TimeSource}.
 * <p>
 * A stopwatch measures elapsed time without periods, pulses, phases, or duty
 * cycles. It uses monotonic nanosecond time internally and exposes elapsed time
 * in milliseconds.
 * <p>
 * Stopping a stopwatch preserves the measured elapsed time. Starting it again
 * resumes from that preserved value. Use {@link #reset()} to clear the elapsed
 * time and leave the stopwatch stopped, or {@link #restart()} to clear it and
 * immediately start measuring from the current time.
 *
 * @author CPZ
 */
public final class Stopwatch {

    private static final long NANOS_PER_MILLI = 1_000_000L;

    private final TimeSource timeSource;

    private long startTimeNanos;
    private long elapsedNanos;

    private boolean running;

    /**
     * Creates a stopwatch backed by the system clock.
     */
    public Stopwatch() {
        this(new SystemTimeSource());
    }

    /**
     * Creates a stopwatch backed by the provided time source.
     *
     * @param timeSource source used for all time reads
     * @throws IllegalArgumentException if {@code timeSource} is {@code null}
     */
    public Stopwatch(TimeSource timeSource) {
        if (timeSource == null) throw new IllegalArgumentException("timeSource must not be null");
        this.timeSource = timeSource;
    }

    /**
     * Starts this stopwatch if it is stopped.
     * <p>
     * If the stopwatch already has preserved elapsed time from a previous
     * {@link #stop()}, measuring resumes from that value. Calling this method
     * while the stopwatch is already running has no effect.
     */
    public void start() {
        if (running) return;
        startTimeNanos = timeSource.nowNanos();
        running = true;
    }

    /**
     * Stops this stopwatch and preserves the elapsed time measured so far.
     * <p>
     * Calling this method while the stopwatch is already stopped has no effect.
     */
    public void stop() {
        if (!running) return;
        long nowNanos = timeSource.nowNanos();
        elapsedNanos += nowNanos - startTimeNanos;
        startTimeNanos = 0L;
        running = false;
    }

    /**
     * Clears elapsed time and leaves this stopwatch stopped.
     */
    public void reset() {
        startTimeNanos = 0L;
        elapsedNanos = 0L;
        running = false;
    }

    /**
     * Clears elapsed time and starts measuring from the current time.
     */
    public void restart() {
        elapsedNanos = 0L;
        startTimeNanos = timeSource.nowNanos();
        running = true;
    }

    /**
     * Stops this stopwatch if it is running, or starts it if it is stopped.
     * <p>
     * When toggling from stopped to running, any preserved elapsed time is kept
     * and measuring resumes from that value.
     */
    public void toggle() {
        if (running) stop();
        else start();
    }

    /**
     * Returns elapsed milliseconds without changing this stopwatch's state.
     * <p>
     * While running, the returned value is the preserved elapsed time plus the
     * time since the current start. While stopped, the returned value is the
     * preserved elapsed time from the last stop or reset.
     *
     * @return elapsed time in milliseconds
     */
    public long getElapsedMillis() {
        if (!running) return elapsedNanos / NANOS_PER_MILLI;
        long nowNanos = timeSource.nowNanos();
        long totalElapsedNanos = elapsedNanos + nowNanos - startTimeNanos;
        return totalElapsedNanos / NANOS_PER_MILLI;
    }

    /**
     * Returns whether this stopwatch is currently measuring time.
     *
     * @return {@code true} if the stopwatch is running; {@code false} otherwise
     */
    public boolean isRunning() {
        return running;
    }
}

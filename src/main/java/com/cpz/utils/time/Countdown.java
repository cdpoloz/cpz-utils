package com.cpz.utils.time;

/**
 * Stateful countdown utility backed by an injectable {@link TimeSource}.
 * <p>
 * A countdown measures remaining time until a configured duration reaches zero.
 * It uses monotonic nanosecond time internally and exposes duration and
 * remaining time in milliseconds.
 * <p>
 * Stopping a countdown preserves the remaining time. Starting it again resumes
 * from that preserved value. Use {@link #reset()} to restore the full duration
 * and leave the countdown stopped, or {@link #restart()} to restore the full
 * duration and immediately start counting down.
 *
 * @author CPZ
 */
public final class Countdown {

    private static final long NANOS_PER_MILLI = 1_000_000L;

    private final TimeSource timeSource;

    private long startTimeNanos;
    private long durationNanos;
    private long remainingNanos;

    private boolean running;

    /**
     * Creates a countdown backed by the system clock.
     *
     * @param durationMillis countdown duration in milliseconds; must be greater
     *                       than zero
     * @throws IllegalArgumentException if {@code durationMillis} is not greater
     *                                  than zero
     */
    public Countdown(int durationMillis) {
        this(new SystemTimeSource(), durationMillis);
    }

    /**
     * Creates a countdown backed by the provided time source.
     *
     * @param timeSource source used for all time reads
     * @param durationMillis countdown duration in milliseconds; must be greater
     *                       than zero
     * @throws IllegalArgumentException if {@code timeSource} is {@code null} or
     *                                  {@code durationMillis} is not greater
     *                                  than zero
     */
    public Countdown(TimeSource timeSource, int durationMillis) {
        if (timeSource == null) throw new IllegalArgumentException("timeSource must not be null");
        this.timeSource = timeSource;
        this.durationNanos = toNanos(requirePositiveDuration(durationMillis));
        this.remainingNanos = durationNanos;
    }

    /**
     * Starts this countdown if it is stopped and has remaining time.
     * <p>
     * If the countdown was previously stopped, measuring resumes from the
     * preserved remaining time. Calling this method while the countdown is
     * already actively running has no effect. Calling it after expiration has no
     * effect; use {@link #restart()} to start a full duration again.
     */
    public void start() {
        if (running) return;
        if (remainingNanos == 0L) return;
        startTimeNanos = timeSource.nowNanos();
        running = true;
    }

    /**
     * Stops this countdown and preserves the remaining time.
     * <p>
     * Calling this method while the countdown is already stopped has no effect.
     * If the countdown has already expired, the preserved remaining time becomes
     * zero.
     */
    public void stop() {
        if (!running) return;
        remainingNanos = getRemainingNanos();
        startTimeNanos = 0L;
        running = false;
    }

    /**
     * Restores the full duration and leaves this countdown stopped.
     */
    public void reset() {
        startTimeNanos = 0L;
        remainingNanos = durationNanos;
        running = false;
    }

    /**
     * Restores the full duration and starts counting down from the current time.
     */
    public void restart() {
        remainingNanos = durationNanos;
        startTimeNanos = timeSource.nowNanos();
        running = true;
    }

    /**
     * Stops this countdown if it is actively running, or starts it if it is
     * stopped and has remaining time.
     * <p>
     * Toggling an expired countdown does not restart it. Use {@link #restart()}
     * when a new full countdown should begin after expiration.
     */
    public void toggle() {
        if (isRunning()) stop();
        else start();
    }

    /**
     * Returns the remaining milliseconds without changing this countdown's
     * state.
     * <p>
     * While running, the returned value is the preserved remaining time minus
     * the time since the current start. While stopped, the returned value is the
     * preserved remaining time. The returned value is never negative.
     *
     * @return remaining time in milliseconds
     */
    public long getRemainingMillis() {
        return getRemainingNanos() / NANOS_PER_MILLI;
    }

    /**
     * Returns whether this countdown has reached zero remaining time.
     * <p>
     * Expiration is derived from the current remaining time and does not mutate
     * this countdown's state.
     *
     * @return {@code true} if no time remains; {@code false} otherwise
     */
    public boolean isExpired() {
        return getRemainingNanos() == 0L;
    }

    /**
     * Returns whether this countdown is actively counting down.
     * <p>
     * Once the remaining time reaches zero, this method returns {@code false}
     * without mutating state.
     *
     * @return {@code true} if the countdown is running and has remaining time;
     *         {@code false} otherwise
     */
    public boolean isRunning() {
        return running && getRemainingNanos() > 0L;
    }

    /**
     * Sets a new full duration, restores that duration as the remaining time,
     * and leaves this countdown stopped.
     *
     * @param durationMillis countdown duration in milliseconds; must be greater
     *                       than zero
     * @throws IllegalArgumentException if {@code durationMillis} is not greater
     *                                  than zero
     */
    public void setDurationMillis(int durationMillis) {
        this.durationNanos = toNanos(requirePositiveDuration(durationMillis));
        reset();
    }

    /**
     * Returns the configured full duration in milliseconds.
     *
     * @return configured duration in milliseconds
     */
    public long getDurationMillis() {
        return durationNanos / NANOS_PER_MILLI;
    }

    private long getRemainingNanos() {
        if (!running) return remainingNanos;
        long nowNanos = timeSource.nowNanos();
        long elapsedNanos = nowNanos - startTimeNanos;
        long currentRemainingNanos = remainingNanos - elapsedNanos;
        return Math.max(currentRemainingNanos, 0L);
    }

    private static int requirePositiveDuration(int durationMillis) {
        if (durationMillis <= 0) throw new IllegalArgumentException("durationMillis must be > 0");
        return durationMillis;
    }

    private static long toNanos(int durationMillis) {
        return durationMillis * NANOS_PER_MILLI;
    }
}

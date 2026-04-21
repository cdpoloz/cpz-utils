package com.cpz.utils.time;

/**
 * Stateful fixed-step timer backed by an injectable {@link TimeSource}.
 * <p>
 * A fixed-step timer converts monotonic elapsed time into discrete simulation
 * steps of a configured duration. It accumulates elapsed time while running,
 * reports how many complete steps are available, and consumes steps explicitly
 * without discarding the sub-step remainder.
 * <p>
 * Stopping a fixed-step timer preserves accumulated time that has not yet been
 * consumed. Starting it again resumes accumulation from that preserved value.
 * Use {@link #reset()} to clear accumulation and leave the timer stopped, or
 * {@link #restart()} to clear accumulation and immediately start accumulating
 * from the current time.
 *
 * @author CPZ
 */
public final class FixedStepTimer {

    private static final long NANOS_PER_MILLI = 1_000_000L;

    private final TimeSource timeSource;

    private long lastUpdateNanos;
    private long stepNanos;
    private long accumulatedNanos;

    private boolean running;

    /**
     * Creates a fixed-step timer backed by the system clock.
     *
     * @param stepMillis fixed step duration in milliseconds; must be greater
     *                   than zero
     */
    public FixedStepTimer(int stepMillis) {
        this(new SystemTimeSource(), stepMillis);
    }

    /**
     * Creates a fixed-step timer backed by the provided time source.
     *
     * @param timeSource source used for all time reads
     * @param stepMillis fixed step duration in milliseconds; must be greater
     *                   than zero
     */
    public FixedStepTimer(TimeSource timeSource, int stepMillis) {
        if (timeSource == null) throw new IllegalArgumentException("timeSource must not be null");
        this.timeSource = timeSource;
        this.stepNanos = toNanos(requirePositiveStep(stepMillis));
    }

    /**
     * Starts this timer if it is stopped.
     * <p>
     * Any accumulated sub-step time or complete unconsumed steps are preserved.
     * Calling this method while the timer is already running has no effect.
     */
    public void start() {
        if (running) return;
        lastUpdateNanos = timeSource.nowNanos();
        running = true;
    }

    /**
     * Stops this timer and preserves all accumulated unconsumed time.
     * <p>
     * Calling this method while the timer is already stopped has no effect.
     */
    public void stop() {
        if (!running) return;
        updateAccumulatedNanos();
        lastUpdateNanos = 0L;
        running = false;
    }

    /**
     * Clears accumulated time and leaves this timer stopped.
     */
    public void reset() {
        lastUpdateNanos = 0L;
        accumulatedNanos = 0L;
        running = false;
    }

    /**
     * Clears accumulated time and starts accumulating from the current time.
     */
    public void restart() {
        accumulatedNanos = 0L;
        lastUpdateNanos = timeSource.nowNanos();
        running = true;
    }

    /**
     * Stops this timer if it is running, or starts it if it is stopped.
     */
    public void toggle() {
        if (running) stop();
        else start();
    }

    /**
     * Consumes one complete fixed step if one is available.
     * <p>
     * This method mutates the timer by first accumulating elapsed time while
     * running, then subtracting one configured step when enough accumulated time
     * is available. The sub-step remainder is preserved.
     *
     * @return {@code true} when one step was consumed; {@code false} otherwise
     */
    public boolean pollStep() {
        if (running) updateAccumulatedNanos();
        if (accumulatedNanos < stepNanos) return false;
        accumulatedNanos -= stepNanos;
        return true;
    }

    /**
     * Returns whether at least one complete fixed step is available without
     * consuming it.
     */
    public boolean hasStep() {
        return getCurrentAccumulatedNanos() >= stepNanos;
    }

    /**
     * Returns the number of complete fixed steps currently available without
     * consuming them.
     * <p>
     * While running, this method reads the current time to include elapsed time
     * since the last update. While stopped, it uses only preserved accumulated
     * time and does not read the time source.
     */
    public long getAvailableSteps() {
        return getCurrentAccumulatedNanos() / stepNanos;
    }

    /**
     * Consumes and returns all complete fixed steps currently available.
     * <p>
     * This method mutates the timer by first accumulating elapsed time while
     * running, then subtracting all complete steps from the accumulated time.
     * The sub-step remainder is preserved.
     */
    public long consumeAvailableSteps() {
        if (running) updateAccumulatedNanos();
        long steps = accumulatedNanos / stepNanos;
        accumulatedNanos -= steps * stepNanos;
        return steps;
    }

    /**
     * Returns whether this timer is currently accumulating elapsed time.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Sets a new fixed step duration, clears accumulation, and leaves this
     * timer stopped.
     *
     * @param stepMillis fixed step duration in milliseconds; must be greater
     *                   than zero
     */
    public void setStepMillis(int stepMillis) {
        this.stepNanos = toNanos(requirePositiveStep(stepMillis));
        reset();
    }

    /**
     * Returns the configured fixed step duration in milliseconds.
     */
    public long getStepMillis() {
        return stepNanos / NANOS_PER_MILLI;
    }

    private void updateAccumulatedNanos() {
        long nowNanos = timeSource.nowNanos();
        accumulatedNanos += nowNanos - lastUpdateNanos;
        lastUpdateNanos = nowNanos;
    }

    private long getCurrentAccumulatedNanos() {
        if (!running) return accumulatedNanos;
        long nowNanos = timeSource.nowNanos();
        return accumulatedNanos + nowNanos - lastUpdateNanos;
    }

    private static int requirePositiveStep(int stepMillis) {
        if (stepMillis <= 0) throw new IllegalArgumentException("stepMillis must be > 0");
        return stepMillis;
    }

    private static long toNanos(int millis) {
        return millis * NANOS_PER_MILLI;
    }
}

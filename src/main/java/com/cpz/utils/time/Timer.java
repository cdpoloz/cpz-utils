package com.cpz.utils.time;

/**
 * Stateful timer utility backed by an injectable {@link TimeSource}.
 *
 * @author CPZ
 */
public final class Timer {

    private static final long NANOS_PER_MILLI = 1_000_000L;

    private final TimeSource timeSource;

    private long startTimeNanos;
    private long periodNanos;
    private float dutyCycle;

    private boolean running;

    /**
     * Creates a timer backed by the system clock.
     */
    public Timer() {
        this(new SystemTimeSource());
    }

    /**
     * Creates a timer backed by the provided time source.
     *
     * @param timeSource source used for all time reads
     * @throws IllegalArgumentException if {@code timeSource} is {@code null}
     */
    public Timer(TimeSource timeSource) {
        if (timeSource == null) {
            throw new IllegalArgumentException("timeSource must not be null");
        }
        this.timeSource = timeSource;
    }

    /**
     * Starts or restarts this timer from the current time.
     * Requires a valid period to be configured first.
     *
     * @throws IllegalStateException if no valid period has been configured
     */
    public void start() {
        if (periodNanos == 0) {
            throw new IllegalStateException("Timer must be configured with a valid period before calling start()");
        }
        startTimeNanos = timeSource.nowNanos();
        running = true;
    }

    /**
     * Configures the period and starts or restarts this timer.
     *
     * @param periodMillis period duration in milliseconds; must be greater than
     *                     zero
     * @throws IllegalArgumentException if {@code periodMillis} is not greater
     *                                  than zero
     */
    public void start(int periodMillis) {
        this.periodNanos = toNanos(requirePositivePeriod(periodMillis));
        start();
    }

    /**
     * Configures the period and duty cycle, then starts or restarts this timer.
     *
     * @param periodMillis period duration in milliseconds; must be greater than
     *                     zero
     * @param dutyCycle active fraction of each period; values are clamped to
     *                  the range {@code [0, 1]}
     * @throws IllegalArgumentException if {@code periodMillis} is not greater
     *                                  than zero
     */
    public void start(int periodMillis, float dutyCycle) {
        this.periodNanos = toNanos(requirePositivePeriod(periodMillis));
        this.dutyCycle = clamp(dutyCycle);
        start();
    }

    private static int requirePositivePeriod(int periodMillis) {
        if (periodMillis <= 0) throw new IllegalArgumentException("periodMillis must be > 0");
        return periodMillis;
    }

    private static long toNanos(int periodMillis) {
        return periodMillis * NANOS_PER_MILLI;
    }

    /**
     * Returns true once when the period has elapsed, then resets the period start.
     *
     * @return {@code true} when the period elapses; {@code false} otherwise
     */
    public boolean pollPeriodPulse() {
        if (!running) return false;

        long nowNanos = timeSource.nowNanos();
        long elapsedNanos = nowNanos - startTimeNanos;
        if (elapsedNanos >= periodNanos) {
            startTimeNanos = nowNanos;
            return true;
        }
        return false;
    }

    /**
     * Checks whether the configured period has elapsed without changing the period start.
     *
     * @return {@code true} if the period has elapsed; {@code false} otherwise
     */
    public boolean isPeriodFinished() {
        if (!running) return false;

        long nowNanos = timeSource.nowNanos();
        long elapsedNanos = nowNanos - startTimeNanos;
        return elapsedNanos >= periodNanos;
    }

    /**
     * Evaluates a repeating on/off phase and may reset the phase start after a full cycle.
     *
     * @return {@code true} during the active half of the cycle; {@code false}
     *         otherwise
     */
    public boolean isOnOffPhaseActive() {
        if (!running) return false;

        long nowNanos = timeSource.nowNanos();
        long elapsedNanos = nowNanos - startTimeNanos;
        if (elapsedNanos >= 2L * periodNanos) {
            startTimeNanos = nowNanos;
            elapsedNanos = 0L;
        }
        return elapsedNanos < periodNanos;
    }

    /**
     * Evaluates a duty-cycle phase and may reset the phase start after each period.
     *
     * @return {@code true} during the active duty-cycle interval; {@code false}
     *         otherwise
     */
    public boolean isDutyCyclePhaseActive() {
        if (!running) return false;

        long nowNanos = timeSource.nowNanos();
        long elapsedNanos = nowNanos - startTimeNanos;
        long onTimeNanos = (long) (periodNanos * dutyCycle);
        if (elapsedNanos >= periodNanos) {
            startTimeNanos = nowNanos;
            elapsedNanos = 0L;
        }
        return elapsedNanos < onTimeNanos;
    }

    /**
     * Sets the duty cycle and evaluates the current duty-cycle phase.
     *
     * @param dutyCycle active fraction of the period; values are clamped to the
     *                  range {@code [0, 1]}
     * @return {@code true} during the active duty-cycle interval; {@code false}
     *         otherwise
     */
    public boolean isDutyCyclePhaseActive(float dutyCycle) {
        if (!running) return false;

        this.dutyCycle = clamp(dutyCycle);
        return isDutyCyclePhaseActive();
    }

    /**
     * Returns elapsed milliseconds since the current start without changing the period start.
     *
     * @return elapsed time in milliseconds, or zero if the timer is stopped
     */
    public long getElapsedMillis() {
        if (!running) return 0L;

        long nowNanos = timeSource.nowNanos();
        long elapsedNanos = nowNanos - startTimeNanos;
        return elapsedNanos / NANOS_PER_MILLI;
    }

    /**
     * Stops this timer and clears its current start time and duty cycle.
     */
    public void stop() {
        running = false;
        startTimeNanos = 0L;
        dutyCycle = 0f;
    }

    /**
     * Stops this timer if it is running, or starts it if it is stopped.
     *
     * @throws IllegalStateException if the timer is stopped and no valid period
     *                               has been configured
     */
    public void toggle() {
        if (running) stop();
        else start();
    }

    private static float clamp(float value) {
        if (value < 0f) return 0f;
        return Math.min(value, 1f);
    }

    /**
     * Returns whether this timer is currently running.
     *
     * @return {@code true} if the timer is running; {@code false} otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Sets the duty cycle used by {@link #isDutyCyclePhaseActive()}.
     *
     * @param dutyCycle active fraction of the period; values are clamped to the
     *                  range {@code [0, 1]}
     */
    public void setDutyCycle(float dutyCycle) {
        this.dutyCycle = clamp(dutyCycle);
    }

    /**
     * Returns the configured duty cycle.
     *
     * @return configured duty cycle in the range {@code [0, 1]}
     */
    public float getDutyCycle() {
        return dutyCycle;
    }

    /**
     * Sets the period duration without starting the timer.
     *
     * @param periodMillis period duration in milliseconds; must be greater than
     *                     zero
     * @throws IllegalArgumentException if {@code periodMillis} is not greater
     *                                  than zero
     */
    public void setPeriodMillis(int periodMillis) {
        this.periodNanos = toNanos(requirePositivePeriod(periodMillis));
    }

    /**
     * Returns the configured period duration.
     *
     * @return configured period duration in milliseconds, or zero if no period
     *         has been configured
     */
    public long getPeriodMillis() {
        return periodNanos / NANOS_PER_MILLI;
    }
}

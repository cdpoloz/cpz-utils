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
     */
    public void start() {
        if (periodNanos == 0) {
            throw new IllegalStateException("Timer must be configured with a valid period before calling start()");
        }
        startTimeNanos = timeSource.nowNanos();
        running = true;
    }

    public void start(int periodMillis) {
        this.periodNanos = toNanos(requirePositivePeriod(periodMillis));
        start();
    }

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
     */
    public boolean isPeriodFinished() {
        if (!running) return false;

        long nowNanos = timeSource.nowNanos();
        long elapsedNanos = nowNanos - startTimeNanos;
        return elapsedNanos >= periodNanos;
    }

    /**
     * Evaluates a repeating on/off phase and may reset the phase start after a full cycle.
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

    public boolean isDutyCyclePhaseActive(float dutyCycle) {
        if (!running) return false;

        this.dutyCycle = clamp(dutyCycle);
        return isDutyCyclePhaseActive();
    }

    /**
     * Returns elapsed milliseconds since the current start without changing the period start.
     */
    public long getElapsedMillis() {
        if (!running) return 0L;

        long nowNanos = timeSource.nowNanos();
        long elapsedNanos = nowNanos - startTimeNanos;
        return elapsedNanos / NANOS_PER_MILLI;
    }

    public void stop() {
        running = false;
        startTimeNanos = 0L;
        dutyCycle = 0f;
    }

    public void toggle() {
        if (running) stop();
        else start();
    }

    private static float clamp(float value) {
        if (value < 0f) return 0f;
        return Math.min(value, 1f);
    }

    public boolean isRunning() {
        return running;
    }

    public void setDutyCycle(float dutyCycle) {
        this.dutyCycle = clamp(dutyCycle);
    }

    public float getDutyCycle() {
        return dutyCycle;
    }

    public void setPeriodMillis(int periodMillis) {
        this.periodNanos = toNanos(requirePositivePeriod(periodMillis));
    }

    public long getPeriodMillis() {
        return periodNanos / NANOS_PER_MILLI;
    }
}

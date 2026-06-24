package com.cpz.utils.time;

/**
 * Source of monotonic time in nanoseconds used by {@link Timer},
 * {@link Stopwatch}, {@link Countdown}, and {@link FixedStepTimer}.
 *
 * @author CPZ
 */
@FunctionalInterface
public interface TimeSource {

    /**
     * Returns the current time in nanoseconds.
     *
     * @return current monotonic time in nanoseconds
     */
    long nowNanos();
}

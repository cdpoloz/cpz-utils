package com.cpz.utils.time;

/**
 * Source of monotonic time in nanoseconds used by {@link Timer}.
 *
 * @author CPZ
 */
@FunctionalInterface
public interface TimeSource {

    /**
     * Returns the current time in nanoseconds.
     */
    long nowNanos();
}

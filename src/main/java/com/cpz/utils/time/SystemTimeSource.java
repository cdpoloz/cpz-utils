package com.cpz.utils.time;

/**
 * {@link TimeSource} implementation backed by {@link System#nanoTime()}.
 *
 * @author CPZ
 */
public final class SystemTimeSource implements TimeSource {

    @Override
    public long nowNanos() {
        return System.nanoTime();
    }
}

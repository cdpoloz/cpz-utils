package com.cpz.utils.noise;

/**
 * Source of one-dimensional noise values used by {@link NoiseValue}.
 * <p>
 * This interface abstracts the noise algorithm. Implementations may delegate to
 * any deterministic or external source, but this package does not provide or
 * require a specific noise implementation.
 *
 * @author CPZ
 */
@FunctionalInterface
public interface NoiseSource {

    /**
     * Returns the noise value at the given one-dimensional position.
     *
     * @param x position on the one-dimensional noise axis
     * @return noise value for {@code x}
     */
    float noise(float x);
}

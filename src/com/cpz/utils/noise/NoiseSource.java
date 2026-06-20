package com.cpz.utils.noise;

/**
 * Functional source of one-dimensional noise values used by {@link NoiseValue}
 * and {@link NoiseVector3}.
 * <p>
 * This interface abstracts the noise algorithm. Implementations may delegate to
 * a deterministic algorithm, a lambda, or an external adapter. The interface
 * does not define an output range; each implementation documents its own range
 * and sampling behavior.
 * <p>
 * {@link NoiseField} extends this interface with two-dimensional and
 * three-dimensional sampling while remaining compatible with APIs that consume
 * a one-dimensional source.
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

package com.cpz.utils.noise;

/**
 * Multidimensional noise field that can be sampled in one, two, or three
 * dimensions.
 *
 * <p>This interface extends {@link NoiseSource} to preserve compatibility
 * with APIs that consume one-dimensional noise sources.</p>
 *
 * <p>The output range, continuity, periodicity and relationship between
 * dimensional overloads are defined by each implementation.</p>
 *
 * <p>Unlike {@link NoiseSource}, this interface is not a functional interface
 * because it declares sampling operations for multiple dimensions.</p>
 *
 * @see PerlinNoise
 * @see FractalNoise
 * @author CPZ
 */
public interface NoiseField extends NoiseSource {

    /**
     * Samples the noise field at a two-dimensional coordinate.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the sampled noise value
     */
    float noise(float x, float y);

    /**
     * Samples the noise field at a three-dimensional coordinate.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     * @return the sampled noise value
     */
    float noise(float x, float y, float z);
}

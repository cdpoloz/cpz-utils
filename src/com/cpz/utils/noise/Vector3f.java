package com.cpz.utils.noise;

/**
 * Immutable record containing three {@code float} components.
 * <p>
 * {@link NoiseVector3} uses this type for value and speed snapshots. It does not
 * provide vector arithmetic.
 *
 * @param x x component
 * @param y y component
 * @param z z component
 *
 * @author CPZ
 */
public record Vector3f(float x, float y, float z) {
}

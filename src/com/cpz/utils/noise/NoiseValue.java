package com.cpz.utils.noise;

/**
 * Stateful one-dimensional noise value backed by an injectable
 * {@link NoiseSource}.
 * <p>
 * The value stores a position on a one-dimensional axis and advances that
 * position by its current speed when {@link #update()} is called. Reading the
 * current noise value with {@link #get()} does not change the internal state.
 *
 * @author CPZ
 */
public final class NoiseValue {

    private final NoiseSource noiseSource;

    private float position;
    private float speed;

    /**
     * Creates a noise value at position {@code 0.0f} with speed {@code 0.0f}.
     *
     * @param noiseSource source used for all noise reads
     */
    public NoiseValue(NoiseSource noiseSource) {
        this(noiseSource, 0.0f);
    }

    /**
     * Creates a noise value at the given initial position with speed
     * {@code 0.0f}.
     *
     * @param noiseSource     source used for all noise reads
     * @param initialPosition initial position on the one-dimensional noise axis
     */
    public NoiseValue(NoiseSource noiseSource, float initialPosition) {
        this(noiseSource, initialPosition, 0.0f);
    }

    /**
     * Creates a noise value at the given initial position and speed.
     *
     * @param noiseSource     source used for all noise reads
     * @param initialPosition initial position on the one-dimensional noise axis
     * @param speed           amount added to the position on each update
     */
    public NoiseValue(NoiseSource noiseSource, float initialPosition, float speed) {
        if (noiseSource == null) throw new IllegalArgumentException("noiseSource must not be null");
        this.noiseSource = noiseSource;
        this.position = initialPosition;
        this.speed = speed;
    }

    /**
     * Advances the current position by the current speed.
     */
    public void update() {
        position += speed;
    }

    /**
     * Returns the noise value at the current position without changing state.
     */
    public float get() {
        return noiseSource.noise(position);
    }

    /**
     * Repositions this value on the one-dimensional noise axis.
     *
     * @param newPosition new current position
     */
    public void reset(float newPosition) {
        position = newPosition;
    }

    /**
     * Returns the current position on the one-dimensional noise axis.
     */
    public float getPosition() {
        return position;
    }

    /**
     * Returns the amount added to the position on each update.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Sets the amount added to the position on each update.
     *
     * @param speed new update speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }
}

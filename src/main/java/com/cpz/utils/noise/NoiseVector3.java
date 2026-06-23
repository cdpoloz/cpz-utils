package com.cpz.utils.noise;

/**
 * Three-component noise vector composed of three independent {@link NoiseValue}
 * instances.
 * <p>
 * Each component follows its own one-dimensional position and speed while all
 * components use the same {@link NoiseSource}. This class does not sample a
 * spatial three-dimensional {@link NoiseField}; use
 * {@link NoiseField#noise(float, float, float)} for that purpose.
 *
 * @author CPZ
 */
public final class NoiseVector3 {

    private final NoiseValue x;
    private final NoiseValue y;
    private final NoiseValue z;

    /**
     * Creates a noise vector with all axes at position {@code 0.0f} and speed
     * {@code 0.0f}.
     *
     * @param noiseSource source used for all noise reads
     * @throws IllegalArgumentException if {@code noiseSource} is {@code null}
     */
    public NoiseVector3(NoiseSource noiseSource) {
        this(noiseSource, 0.0f);
    }

    /**
     * Creates a noise vector with all axes at the given initial position and
     * speed {@code 0.0f}.
     *
     * @param noiseSource     source used for all noise reads
     * @param initialPosition initial position for each axis
     * @throws IllegalArgumentException if {@code noiseSource} is {@code null}
     */
    public NoiseVector3(NoiseSource noiseSource, float initialPosition) {
        this(noiseSource, initialPosition, 0.0f);
    }

    /**
     * Creates a noise vector with all axes at the given initial position and
     * speed.
     *
     * @param noiseSource     source used for all noise reads
     * @param initialPosition initial position for each axis
     * @param speed           amount added to each axis position on each update
     * @throws IllegalArgumentException if {@code noiseSource} is {@code null}
     */
    public NoiseVector3(NoiseSource noiseSource, float initialPosition, float speed) {
        x = new NoiseValue(noiseSource, initialPosition, speed);
        y = new NoiseValue(noiseSource, initialPosition, speed);
        z = new NoiseValue(noiseSource, initialPosition, speed);
    }

    /**
     * Creates a noise vector with independent component positions and zero
     * speed.
     *
     * @param noiseSource source used for all noise reads
     * @param px          initial x component position
     * @param py          initial y component position
     * @param pz          initial z component position
     * @throws IllegalArgumentException if {@code noiseSource} is {@code null}
     */
    public NoiseVector3(NoiseSource noiseSource, float px, float py, float pz) {
        this.x = new NoiseValue(noiseSource, px);
        this.y = new NoiseValue(noiseSource, py);
        this.z = new NoiseValue(noiseSource, pz);
    }

    /**
     * Creates a noise vector with independent positions and speeds for each
     * component.
     *
     * @param noiseSource source used for all noise reads
     * @param px          initial x component position
     * @param py          initial y component position
     * @param pz          initial z component position
     * @param sx          initial x component speed
     * @param sy          initial y component speed
     * @param sz          initial z component speed
     * @throws IllegalArgumentException if {@code noiseSource} is {@code null}
     */
    public NoiseVector3(NoiseSource noiseSource, float px, float py, float pz, float sx, float sy, float sz) {
        this.x = new NoiseValue(noiseSource, px, sx);
        this.y = new NoiseValue(noiseSource, py, sy);
        this.z = new NoiseValue(noiseSource, pz, sz);
    }

    /**
     * Advances all component positions by their current speeds.
     */
    public void update() {
        x.update();
        y.update();
        z.update();
    }

    /**
     * Returns the current noise vector without changing state.
     *
     * @return immutable snapshot of the three source values
     */
    public Vector3f get() {
        return new Vector3f(x.get(), y.get(), z.get());
    }

    /**
     * Repositions each component on its one-dimensional noise axis.
     *
     * @param px new x component position
     * @param py new y component position
     * @param pz new z component position
     */
    public void reset(float px, float py, float pz) {
        x.reset(px);
        y.reset(py);
        z.reset(pz);
    }

    /**
     * Sets the same speed for all components.
     *
     * @param speed new update speed for all components
     */
    public void setSpeed(float speed) {
        x.setSpeed(speed);
        y.setSpeed(speed);
        z.setSpeed(speed);
    }

    /**
     * Sets the speed for each component.
     *
     * @param sx new x component speed
     * @param sy new y component speed
     * @param sz new z component speed
     */
    public void setSpeed(float sx, float sy, float sz) {
        x.setSpeed(sx);
        y.setSpeed(sy);
        z.setSpeed(sz);
    }

    /**
     * Returns the current component speeds.
     *
     * @return immutable snapshot of the component speeds
     */
    public Vector3f getSpeed() {
        return new Vector3f(x.getSpeed(), y.getSpeed(), z.getSpeed());
    }
}

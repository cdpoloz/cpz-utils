package com.cpz.utils.noise;

/**
 * Generates normalized fractal Brownian motion noise by combining multiple
 * octaves from another {@link NoiseField}.
 *
 * <p>Each octave increases the sampling frequency by {@code lacunarity} and
 * reduces its contribution by {@code persistence}. The accumulated result is
 * divided by the total weight. For a finite source bounded to a fixed interval,
 * this normalized weighted sum preserves that interval.</p>
 *
 * <p>This class does not impose a range on the wrapped field. When the source is
 * {@link PerlinNoise}, results remain in {@code [0, 1]}. A non-finite value from
 * the source is rejected.</p>
 *
 * <p>Instances are immutable and safe to use concurrently when the wrapped
 * noise field is also safe to use concurrently.</p>
 *
 * @author CPZ
 */
public final class FractalNoise implements NoiseField {

    private final NoiseField source;
    private final int octaves;
    private final float frequency;
    private final float lacunarity;
    private final float persistence;

    /**
     * Creates a normalized fractal noise field.
     *
     * @param source      the noise field sampled by every octave
     * @param octaves     the number of octaves; must be at least one
     * @param frequency   the initial sampling frequency; must be finite and
     *                    greater than zero
     * @param lacunarity  the frequency multiplier between octaves; must be
     *                    finite and greater than one
     * @param persistence the weight multiplier between octaves; must be
     *                    finite and between zero and one, inclusive
     * @throws IllegalArgumentException if {@code source} is {@code null} or a
     *                                  numeric parameter is outside its required
     *                                  range
     */
    public FractalNoise(NoiseField source, int octaves, float frequency, float lacunarity, float persistence) {
        if (source == null) throw new IllegalArgumentException("source must not be null");
        if (octaves < 1) throw new IllegalArgumentException("octaves must be at least 1");
        validatePositive(frequency, "frequency");
        validateGreaterThanOne(lacunarity, "lacunarity");
        if (!Float.isFinite(persistence) || persistence < 0.0f || persistence > 1.0f) {
            throw new IllegalArgumentException("persistence must be finite and between 0 and 1");
        }
        this.source = source;
        this.octaves = octaves;
        this.frequency = frequency;
        this.lacunarity = lacunarity;
        this.persistence = persistence;
    }

    private static float scaleCoordinate(float coordinate, double frequency, String name) {
        double scaledCoordinate = coordinate * frequency;
        if (!Double.isFinite(scaledCoordinate) || scaledCoordinate > Float.MAX_VALUE || scaledCoordinate < -Float.MAX_VALUE) {
            throw new IllegalArgumentException("scaled " + name + " coordinate must be finite");
        }
        return (float) scaledCoordinate;
    }

    private static void validateCoordinate(float coordinate, String name) {
        if (!Float.isFinite(coordinate)) throw new IllegalArgumentException(name + " must be finite");
    }

    private static void validatePositive(float value, String name) {
        if (!Float.isFinite(value) || value <= 0.0f)
            throw new IllegalArgumentException(name + " must be finite and greater than 0");
    }

    private static void validateGreaterThanOne(float value, String name) {
        if (!Float.isFinite(value) || value <= 1.0f)
            throw new IllegalArgumentException(name + " must be finite and greater than 1");
    }

    private static void validateSourceValue(float value) {
        if (!Float.isFinite(value)) throw new IllegalStateException("source returned a non-finite noise value");
    }

    /**
     * Returns the noise field sampled by every octave.
     *
     * @return wrapped noise field
     */
    public NoiseField getSource() {
        return source;
    }

    /**
     * Returns the configured number of octaves.
     *
     * @return number of octaves
     */
    public int getOctaves() {
        return octaves;
    }

    /**
     * Returns the initial sampling frequency.
     *
     * @return initial frequency
     */
    public float getFrequency() {
        return frequency;
    }

    /**
     * Returns the frequency multiplier applied between octaves.
     *
     * @return lacunarity
     */
    public float getLacunarity() {
        return lacunarity;
    }

    /**
     * Returns the weight multiplier applied between octaves.
     *
     * @return persistence
     */
    public float getPersistence() {
        return persistence;
    }

    /**
     * Samples normalized fractal Brownian motion along one dimension.
     *
     * @param x finite x-coordinate
     * @return normalized weighted sum of the sampled octaves
     * @throws IllegalArgumentException if {@code x} is not finite or a scaled
     *                                  coordinate is not representable as a
     *                                  finite {@code float}
     * @throws IllegalStateException    if the wrapped field returns a
     *                                  non-finite value
     */
    @Override
    public float noise(float x) {
        validateCoordinate(x, "x");
        double sum = 0.0;
        double totalWeight = 0.0;
        double currentFrequency = frequency;
        double currentWeight = 1.0;
        for (int octave = 0; octave < octaves; octave++) {
            float sampleX = scaleCoordinate(x, currentFrequency, "x");
            float sourceValue = source.noise(sampleX);
            validateSourceValue(sourceValue);
            sum += sourceValue * currentWeight;
            totalWeight += currentWeight;
            currentWeight *= persistence;
            if (currentWeight == 0.0) break;
            currentFrequency *= lacunarity;
        }
        return (float) (sum / totalWeight);
    }

    /**
     * Samples normalized fractal Brownian motion in two dimensions.
     *
     * @param x finite x-coordinate
     * @param y finite y-coordinate
     * @return normalized weighted sum of the sampled octaves
     * @throws IllegalArgumentException if a coordinate is not finite or a
     *                                  scaled coordinate is not representable
     *                                  as a finite {@code float}
     * @throws IllegalStateException    if the wrapped field returns a
     *                                  non-finite value
     */
    @Override
    public float noise(float x, float y) {
        validateCoordinate(x, "x");
        validateCoordinate(y, "y");
        double sum = 0.0;
        double totalWeight = 0.0;
        double currentFrequency = frequency;
        double currentWeight = 1.0;
        for (int octave = 0; octave < octaves; octave++) {
            float sampleX = scaleCoordinate(x, currentFrequency, "x");
            float sampleY = scaleCoordinate(y, currentFrequency, "y");
            float sourceValue = source.noise(sampleX, sampleY);
            validateSourceValue(sourceValue);
            sum += sourceValue * currentWeight;
            totalWeight += currentWeight;
            currentWeight *= persistence;
            if (currentWeight == 0.0) break;
            currentFrequency *= lacunarity;
        }
        return (float) (sum / totalWeight);
    }

    /**
     * Samples normalized fractal Brownian motion in three dimensions.
     *
     * @param x finite x-coordinate
     * @param y finite y-coordinate
     * @param z finite z-coordinate
     * @return normalized weighted sum of the sampled octaves
     * @throws IllegalArgumentException if a coordinate is not finite or a
     *                                  scaled coordinate is not representable
     *                                  as a finite {@code float}
     * @throws IllegalStateException    if the wrapped field returns a
     *                                  non-finite value
     */
    @Override
    public float noise(float x, float y, float z) {
        validateCoordinate(x, "x");
        validateCoordinate(y, "y");
        validateCoordinate(z, "z");
        double sum = 0.0;
        double totalWeight = 0.0;
        double currentFrequency = frequency;
        double currentWeight = 1.0;
        for (int octave = 0; octave < octaves; octave++) {
            float sampleX = scaleCoordinate(x, currentFrequency, "x");
            float sampleY = scaleCoordinate(y, currentFrequency, "y");
            float sampleZ = scaleCoordinate(z, currentFrequency, "z");
            float sourceValue = source.noise(sampleX, sampleY, sampleZ);
            validateSourceValue(sourceValue);
            sum += sourceValue * currentWeight;
            totalWeight += currentWeight;
            currentWeight *= persistence;
            if (currentWeight == 0.0) break;
            currentFrequency *= lacunarity;
        }
        return (float) (sum / totalWeight);
    }
}

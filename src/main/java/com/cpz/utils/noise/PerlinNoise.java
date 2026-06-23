package com.cpz.utils.noise;

/**
 * Deterministic implementation of Improved Perlin Noise.
 *
 * <p>The generated values are normalized and clamped to the range
 * {@code [0, 1]}. A value of {@code 0.5} corresponds to zero in the
 * underlying signed noise field.</p>
 *
 * <p>One-dimensional and two-dimensional samples are slices of the same
 * three-dimensional noise field: they sample the unused coordinates at zero.
 * The field is periodic every 256 cells along each axis.</p>
 *
 * <p>The permutation table contains 256 seeded entries duplicated for indexed
 * lookup. It is generated with Fisher-Yates using SplitMix64 output. Equal seeds
 * and coordinates produce equal results.</p>
 *
 * <p>Instances are immutable and safe to use concurrently.</p>
 *
 * @author CPZ
 */
public final class PerlinNoise implements NoiseField {

    private static final int PERMUTATION_SIZE = 256;
    private static final int PERMUTATION_MASK = PERMUTATION_SIZE - 1;
    private static final long SPLIT_MIX_GAMMA = 0x9E3779B97F4A7C15L;
    private final long seed;
    private final int[] permutation;

    /**
     * Creates a deterministic Perlin noise field with a 256-cell period on each
     * axis.
     *
     * @param seed the seed used to generate the permutation table
     */
    public PerlinNoise(long seed) {
        this.seed = seed;
        this.permutation = createPermutation(seed);
    }

    private static int[] createPermutation(long seed) {
        int[] values = new int[PERMUTATION_SIZE];
        for (int i = 0; i < PERMUTATION_SIZE; i++) values[i] = i;
        long state = seed;
        for (int i = PERMUTATION_SIZE - 1; i > 0; i--) {
            state += SPLIT_MIX_GAMMA;
            long randomValue = mixSplitMix64(state);
            int selectedIndex = (int) Long.remainderUnsigned(randomValue, i + 1L);
            int temporary = values[i];
            values[i] = values[selectedIndex];
            values[selectedIndex] = temporary;
        }
        int[] result = new int[PERMUTATION_SIZE * 2];
        for (int i = 0; i < result.length; i++) result[i] = values[i & PERMUTATION_MASK];
        return result;
    }

    private static long mixSplitMix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static int latticeIndex(double coordinate) {
        double remainder = coordinate % PERMUTATION_SIZE;
        if (remainder < 0.0) remainder += PERMUTATION_SIZE;
        return (int) remainder;
    }

    private static double fade(double value) {
        return value * value * value * (value * (value * 6.0 - 15.0) + 10.0);
    }

    private static double lerp(double start, double end, double amount) {
        return start + amount * (end - start);
    }

    private static double gradient(int hash, double x, double y, double z) {
        int gradientHash = hash & 15;
        double first = gradientHash < 8 ? x : y;
        double second = gradientHash < 4 ? y : gradientHash == 12 || gradientHash == 14 ? x : z;
        double firstContribution = (gradientHash & 1) == 0 ? first : -first;
        double secondContribution = (gradientHash & 2) == 0 ? second : -second;
        return firstContribution + secondContribution;
    }

    private static void validateCoordinate(float coordinate, String name) {
        if (!Float.isFinite(coordinate)) throw new IllegalArgumentException(name + " must be finite");
    }

    /**
     * Returns the seed used by this noise field.
     *
     * @return the seed
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Samples the one-dimensional slice of this field at {@code (x, 0, 0)}.
     *
     * @param x finite x-coordinate
     * @return normalized and clamped noise value in {@code [0, 1]}
     * @throws IllegalArgumentException if {@code x} is not finite
     */
    @Override
    public float noise(float x) {
        validateCoordinate(x, "x");
        return normalize(sample(x, 0.0, 0.0));
    }

    /**
     * Samples the two-dimensional slice of this field at {@code (x, y, 0)}.
     *
     * @param x finite x-coordinate
     * @param y finite y-coordinate
     * @return normalized and clamped noise value in {@code [0, 1]}
     * @throws IllegalArgumentException if either coordinate is not finite
     */
    @Override
    public float noise(float x, float y) {
        validateCoordinate(x, "x");
        validateCoordinate(y, "y");
        return normalize(sample(x, y, 0.0));
    }

    /**
     * Samples this three-dimensional noise field.
     *
     * @param x finite x-coordinate
     * @param y finite y-coordinate
     * @param z finite z-coordinate
     * @return normalized and clamped noise value in {@code [0, 1]}
     * @throws IllegalArgumentException if any coordinate is not finite
     */
    @Override
    public float noise(float x, float y, float z) {
        validateCoordinate(x, "x");
        validateCoordinate(y, "y");
        validateCoordinate(z, "z");
        return normalize(sample(x, y, z));
    }

    private static float normalize(double value) {
        double normalizedValue = (value + 1.0) * 0.5;
        normalizedValue = Math.max(0.0, Math.min(1.0, normalizedValue));
        return (float) normalizedValue;
    }

    private double sample(double x, double y, double z) {
        double floorX = Math.floor(x);
        double floorY = Math.floor(y);
        double floorZ = Math.floor(z);

        int xi = latticeIndex(floorX);
        int yi = latticeIndex(floorY);
        int zi = latticeIndex(floorZ);

        double localX = x - floorX;
        double localY = y - floorY;
        double localZ = z - floorZ;

        double u = fade(localX);
        double v = fade(localY);
        double w = fade(localZ);

        int a = permutation[xi] + yi;
        int aa = permutation[a] + zi;
        int ab = permutation[a + 1] + zi;

        int b = permutation[xi + 1] + yi;
        int ba = permutation[b] + zi;
        int bb = permutation[b + 1] + zi;

        double x1 = lerp(
                gradient(permutation[aa], localX, localY, localZ),
                gradient(permutation[ba], localX - 1.0, localY, localZ),
                u
        );
        double x2 = lerp(
                gradient(permutation[ab], localX, localY - 1.0, localZ),
                gradient(permutation[bb], localX - 1.0, localY - 1.0, localZ),
                u
        );
        double y1 = lerp(x1, x2, v);
        x1 = lerp(
                gradient(permutation[aa + 1], localX, localY, localZ - 1.0),
                gradient(permutation[ba + 1], localX - 1.0, localY, localZ - 1.0),
                u
        );
        x2 = lerp(
                gradient(permutation[ab + 1], localX, localY - 1.0, localZ - 1.0),
                gradient(permutation[bb + 1], localX - 1.0, localY - 1.0, localZ - 1.0),
                u
        );
        double y2 = lerp(x1, x2, v);
        return lerp(y1, y2, w);
    }
}

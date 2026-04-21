package com.cpz.utils.color;

/**
 * Utility methods for packed ARGB colors.
 * <p>
 * Colors are represented as {@code int} values in {@code 0xAARRGGBB} format:
 * alpha in the most significant byte, followed by red, green, and blue.
 * <p>
 * This class builds, adjusts, and reads packed color values. It has no
 * dependency on Processing, AWT, CSS parsing, color spaces, gradients, or
 * rendering frameworks.
 * <p>
 * Channel values are explicit byte channels. Values below {@code 0} are
 * clamped to {@code 0}; values above {@code 255} are clamped to {@code 255}.
 * Percentage methods accept values in the {@code 0..100} range and clamp
 * values outside that range before converting them to byte channels.
 *
 * @author CPZ
 */
public final class Colors {
   private Colors() {
   }

   /**
    * Creates a grayscale color with an explicit alpha channel.
    * <p>
    * The same clamped value is used for red, green, and blue.
    *
    * @param alpha alpha channel, clamped to {@code 0..255}
    * @param value grayscale channel value, clamped to {@code 0..255}
    * @return packed {@code 0xAARRGGBB} color
    */
   public static int agray(int alpha, int value) {
      return argb(alpha, value, value, value);
   }

   /**
    * Creates an opaque grayscale color.
    * <p>
    * The same clamped value is used for red, green, and blue. The alpha channel
    * is set to {@code 255}.
    *
    * @param value grayscale channel value, clamped to {@code 0..255}
    * @return packed opaque {@code 0xAARRGGBB} color
    */
   public static int gray(int value) {
      return rgb(value, value, value);
   }

   /**
    * Creates an opaque RGB color.
    * <p>
    * The alpha channel is set to {@code 255}.
    *
    * @param red red channel, clamped to {@code 0..255}
    * @param green green channel, clamped to {@code 0..255}
    * @param blue blue channel, clamped to {@code 0..255}
    * @return packed opaque {@code 0xAARRGGBB} color
    */
   public static int rgb(int red, int green, int blue) {
      return argb(255, red, green, blue);
   }

   /**
    * Creates a color from explicit alpha, red, green, and blue byte channels.
    *
    * @param alpha alpha channel, clamped to {@code 0..255}
    * @param red red channel, clamped to {@code 0..255}
    * @param green green channel, clamped to {@code 0..255}
    * @param blue blue channel, clamped to {@code 0..255}
    * @return packed {@code 0xAARRGGBB} color
    */
   public static int argb(int alpha, int red, int green, int blue) {
      alpha = clamp(alpha);
      red = clamp(red);
      green = clamp(green);
      blue = clamp(blue);
      return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
   }

   /**
    * Creates an opaque RGB color from percentage channels.
    * <p>
    * Each percentage is clamped to {@code 0..100} and converted to a byte
    * channel using integer arithmetic.
    *
    * @param redPercent red percentage, clamped to {@code 0..100}
    * @param greenPercent green percentage, clamped to {@code 0..100}
    * @param bluePercent blue percentage, clamped to {@code 0..100}
    * @return packed opaque {@code 0xAARRGGBB} color
    */
   public static int rgbPct(int redPercent, int greenPercent, int bluePercent) {
      return rgb(pctToByte(redPercent), pctToByte(greenPercent), pctToByte(bluePercent));
   }

   /**
    * Creates a color from alpha, red, green, and blue percentage channels.
    * <p>
    * Each percentage is clamped to {@code 0..100} and converted to a byte
    * channel using integer arithmetic.
    *
    * @param alphaPercent alpha percentage, clamped to {@code 0..100}
    * @param redPercent red percentage, clamped to {@code 0..100}
    * @param greenPercent green percentage, clamped to {@code 0..100}
    * @param bluePercent blue percentage, clamped to {@code 0..100}
    * @return packed {@code 0xAARRGGBB} color
    */
   public static int argbPct(int alphaPercent, int redPercent, int greenPercent, int bluePercent) {
      return argb(pctToByte(alphaPercent), pctToByte(redPercent), pctToByte(greenPercent), pctToByte(bluePercent));
   }

   /**
    * Creates an opaque grayscale color from a percentage channel.
    * <p>
    * The percentage is clamped to {@code 0..100}, converted to a byte channel,
    * and reused for red, green, and blue.
    *
    * @param valuePercent grayscale percentage, clamped to {@code 0..100}
    * @return packed opaque {@code 0xAARRGGBB} color
    */
   public static int grayPct(int valuePercent) {
      return gray(pctToByte(valuePercent));
   }

   /**
    * Creates a grayscale color from alpha and grayscale percentage channels.
    * <p>
    * Percentages are clamped to {@code 0..100}. The grayscale percentage is
    * converted to a byte channel and reused for red, green, and blue.
    *
    * @param alphaPercent alpha percentage, clamped to {@code 0..100}
    * @param valuePercent grayscale percentage, clamped to {@code 0..100}
    * @return packed {@code 0xAARRGGBB} color
    */
   public static int agrayPct(int alphaPercent, int valuePercent) {
      return agray(pctToByte(alphaPercent), pctToByte(valuePercent));
   }

   private static int pctToByte(int value) {
      return Math.max(0, Math.min(100, value)) * 255 / 100;
   }

   /**
    * Returns the given color with its alpha channel replaced.
    * <p>
    * Red, green, and blue are preserved from {@code color}. The provided alpha
    * value is clamped to {@code 0..255}.
    *
    * @param alpha replacement alpha channel, clamped to {@code 0..255}
    * @param color packed {@code 0xAARRGGBB} color whose RGB channels are kept
    * @return packed {@code 0xAARRGGBB} color with the replaced alpha channel
    */
   public static int withAlpha(int alpha, int color) {
      return color & 16777215 | (clamp(alpha) & 255) << 24;
   }

   /**
    * Returns the red channel from a packed ARGB color.
    * <p>
    * The input color is interpreted as {@code 0xAARRGGBB}. The returned value
    * is in the {@code 0..255} range.
    *
    * @param color packed {@code 0xAARRGGBB} color
    * @return red channel in {@code 0..255}
    */
   public static int red(int color) {
      return color >> 16 & 255;
   }

   /**
    * Returns the green channel from a packed ARGB color.
    * <p>
    * The input color is interpreted as {@code 0xAARRGGBB}. The returned value
    * is in the {@code 0..255} range.
    *
    * @param color packed {@code 0xAARRGGBB} color
    * @return green channel in {@code 0..255}
    */
   public static int green(int color) {
      return color >> 8 & 255;
   }

   /**
    * Returns the blue channel from a packed ARGB color.
    * <p>
    * The input color is interpreted as {@code 0xAARRGGBB}. The returned value
    * is in the {@code 0..255} range.
    *
    * @param color packed {@code 0xAARRGGBB} color
    * @return blue channel in {@code 0..255}
    */
   public static int blue(int color) {
      return color & 255;
   }

   /**
    * Returns the alpha channel from a packed ARGB color.
    * <p>
    * The input color is interpreted as {@code 0xAARRGGBB}. The returned value
    * is in the {@code 0..255} range. This method reads the existing alpha
    * channel; use {@link #withAlpha(int, int)} to replace it.
    *
    * @param color packed {@code 0xAARRGGBB} color
    * @return alpha channel in {@code 0..255}
    */
   public static int alpha(int color) {
      return color >>> 24 & 255;
   }

   /**
    * Returns the given color with its alpha channel replaced.
    * <p>
    * This two-argument overload is kept for compatibility with older code.
    * Prefer {@link #withAlpha(int, int)} for replacing alpha, and
    * {@link #alpha(int)} for reading the existing alpha channel.
    *
    * @param alpha replacement alpha channel, clamped to {@code 0..255}
    * @param color packed {@code 0xAARRGGBB} color whose RGB channels are kept
    * @return packed {@code 0xAARRGGBB} color with the replaced alpha channel
    */
   @Deprecated
   public static int alpha(int alpha, int color) {
      return withAlpha(alpha, color);
   }

   private static int clamp(int value) {
      return Math.max(0, Math.min(255, value));
   }
}

# Colors (cpz-utils)

## Purpose

`Colors` is a small static utility for packed ARGB colors. It builds, adjusts,
interpolates, and reads color values represented as Java `int`s.

The class is framework-agnostic. It does not depend on Processing, AWT, CSS
parsing, color-space libraries, rendering APIs, or UI frameworks.

Typical uses include:

- creating packed RGB and ARGB colors
- creating grayscale colors
- creating colors from percentage channels
- replacing the alpha channel of an existing color
- interpolating between two packed ARGB colors
- reading alpha, red, green, and blue channels from a packed color

## Color Representation

`Colors` uses packed ARGB integers in `0xAARRGGBB` format.

The byte order is:

- `AA`: alpha
- `RR`: red
- `GG`: green
- `BB`: blue

All methods in this class use that representation. Construction methods return
packed `0xAARRGGBB` values, alpha replacement keeps the RGB bytes from the input
color, and channel readers extract bytes from the same layout.

## Channel Semantics

Explicit channel methods use byte channel values in the `0..255` range.

Values outside that range are clamped:

- values below `0` become `0`
- values above `255` become `255`

Percentage methods use integer percentages in the `0..100` range. Percentages
outside that range are clamped before conversion, then converted to byte
channels with integer arithmetic:

```java
byteValue = percent * 255 / 100;
```

This keeps behavior simple and deterministic.

## Construction Methods

`rgb(int red, int green, int blue)` creates an opaque color. The alpha channel
is set to `255`.

```java
int color = Colors.rgb(32, 96, 160);
```

`argb(int alpha, int red, int green, int blue)` creates a color with explicit
alpha, red, green, and blue channels.

```java
int translucentBlue = Colors.argb(128, 32, 96, 160);
```

`gray(int value)` creates an opaque grayscale color. The same clamped value is
used for red, green, and blue.

```java
int gray = Colors.gray(180);
```

`agray(int alpha, int value)` creates a grayscale color with explicit alpha.
The same clamped value is used for red, green, and blue.

```java
int translucentGray = Colors.agray(96, 180);
```

Percentage variants convert `0..100` percentages to byte channels:

```java
int halfRed = Colors.rgbPct(50, 0, 0);
int translucentWhite = Colors.argbPct(50, 100, 100, 100);
int darkGray = Colors.grayPct(25);
int fadedGray = Colors.agrayPct(40, 25);
```

Available construction methods include:

- `rgb(int red, int green, int blue)`
- `argb(int alpha, int red, int green, int blue)`
- `gray(int value)`
- `agray(int alpha, int value)`
- `rgbPct(int redPercent, int greenPercent, int bluePercent)`
- `argbPct(int alphaPercent, int redPercent, int greenPercent, int bluePercent)`
- `grayPct(int valuePercent)`
- `agrayPct(int alphaPercent, int valuePercent)`

## Interpolation

`lerpColor(int c1, int c2, float t)` linearly interpolates between two packed
ARGB colors. Alpha, red, green, and blue are read from each input color and
interpolated separately.

The `t` parameter is the mix factor:

- `0.0f` returns the first color
- `1.0f` returns the second color
- values outside `0..1` are clamped before interpolation

Each interpolated channel is rounded to the nearest byte value and packed back
into a `0xAARRGGBB` color.

```java
int idle = Colors.rgb(40, 120, 220);
int hover = Colors.rgb(80, 180, 120);

int halfway = Colors.lerpColor(idle, hover, 0.5f);
```

This is useful for hover states, simple transitions, or gradual mixes between
two existing packed colors.

## Reading Channels

Channel reader methods interpret the input as `0xAARRGGBB` and return a value in
the `0..255` range.

```java
int color = Colors.argb(128, 32, 96, 160);

int alpha = Colors.alpha(color);
int red = Colors.red(color);
int green = Colors.green(color);
int blue = Colors.blue(color);
```

Available reader methods include:

- `alpha(int color)`
- `red(int color)`
- `green(int color)`
- `blue(int color)`

These methods are pure channel extraction helpers. They do not clamp the input
color because the input is already treated as a packed integer value.

## Alpha Handling

`withAlpha(int alpha, int color)` returns a new packed color with the alpha
channel replaced. Red, green, and blue are preserved from the input color.

```java
int opaqueBlue = Colors.rgb(32, 96, 160);
int translucentBlue = Colors.withAlpha(128, opaqueBlue);
```

`alpha(int color)` reads the existing alpha channel from a packed color.

```java
int alpha = Colors.alpha(translucentBlue);
```

`alpha(int alpha, int color)` is a deprecated two-argument alias for
`withAlpha(int alpha, int color)`. It remains available for compatibility with
older code, but new code should use `withAlpha(...)` when replacing alpha and
`alpha(int color)` when reading alpha.

## Design Principles

`Colors` is intentionally small:

- static utility methods only
- packed `int` ARGB representation
- explicit channel ranges
- clamped construction inputs
- no framework dependency
- no hidden rendering behavior

The class is intended as a reusable low-level utility for projects that need a
simple shared color representation.

## Usage Examples

Create a color:

```java
int accent = Colors.rgb(40, 120, 220);
```

Modify alpha:

```java
int accent = Colors.rgb(40, 120, 220);
int overlay = Colors.withAlpha(96, accent);
```

Read channels:

```java
int color = Colors.argb(192, 40, 120, 220);

int a = Colors.alpha(color);
int r = Colors.red(color);
int g = Colors.green(color);
int b = Colors.blue(color);
```

Use construction and reading together:

```java
int base = Colors.grayPct(75);
int faded = Colors.withAlpha(128, base);

if (Colors.alpha(faded) < 255) {
    int red = Colors.red(faded);
}
```

Interpolate between two colors:

```java
int start = Colors.argb(255, 255, 64, 64);
int end = Colors.argb(255, 64, 128, 255);

int midTransition = Colors.lerpColor(start, end, 0.5f);
```

## Notes / Non-goals

`Colors` does not provide:

- HSL or HSV conversion
- CSS color parsing
- gradient generation
- named color palettes
- high-level color objects
- rendering or framework adapters

Those features are outside the scope of this first color utility. The current
class stays focused on packed ARGB construction, alpha adjustment, and component
reading, plus direct interpolation between two packed colors.

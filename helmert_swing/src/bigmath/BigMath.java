package bigmath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Arbitrary-precision math functions for BigDecimal, mirroring java.lang.Math.
 * Every method takes a MathContext controlling the precision of the result.
 * Internally, extra guard digits are used so rounding error does not leak
 * into the returned precision.
 */
public final class BigMath {

    private BigMath() {}

    private static final BigDecimal TWO = new BigDecimal("2");
    private static final BigDecimal THREE = new BigDecimal("3");
    private static final int GUARD_DIGITS = 12;

    private static MathContext extend(MathContext mc) {
        return new MathContext(mc.getPrecision() + GUARD_DIGITS, RoundingMode.HALF_UP);
    }

    // ---------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------

    /** Computes pi via Machin's formula: pi = 16*atan(1/5) - 4*atan(1/239). */
    public static BigDecimal pi(MathContext mc) {
        MathContext wc = extend(mc);
        BigDecimal a = atanTaylor(BigDecimal.ONE.divide(new BigDecimal("5"), wc), wc);
        BigDecimal b = atanTaylor(BigDecimal.ONE.divide(new BigDecimal("239"), wc), wc);
        BigDecimal result = new BigDecimal("16").multiply(a, wc).subtract(new BigDecimal("4").multiply(b, wc), wc);
        return result.round(mc);
    }

    /** Computes e = exp(1). */
    public static BigDecimal e(MathContext mc) {
        return exp(BigDecimal.ONE, mc);
    }

    // ---------------------------------------------------------------
    // Roots and powers
    // ---------------------------------------------------------------

    public static BigDecimal sqrt(BigDecimal x, MathContext mc) {
        if (x.signum() < 0) throw new ArithmeticException("sqrt of negative value");
        return x.sqrt(mc);
    }

    /** Real cube root, defined for negative x too. */
    public static BigDecimal cbrt(BigDecimal x, MathContext mc) {
        if (x.signum() == 0) return BigDecimal.ZERO;
        MathContext wc = extend(mc);
        boolean negative = x.signum() < 0;
        BigDecimal ax = x.abs(wc);

        // Newton's method: y_{n+1} = (2*y_n + ax/y_n^2) / 3
        BigDecimal y = BigDecimal.valueOf(Math.cbrt(ax.doubleValue()));
        if (y.signum() == 0) y = BigDecimal.ONE;
        for (int i = 0; i < 60; i++) {
            BigDecimal y2 = y.multiply(y, wc);
            BigDecimal next = TWO.multiply(y, wc).add(ax.divide(y2, wc), wc).divide(THREE, wc);
            if (next.subtract(y, wc).abs().compareTo(tolerance(wc)) < 0) {
                y = next;
                break;
            }
            y = next;
        }
        return (negative ? y.negate() : y).round(mc);
    }

    /** x raised to an arbitrary (possibly non-integer, possibly negative) power. */
    public static BigDecimal pow(BigDecimal base, BigDecimal exponent, MathContext mc) {
        if (exponent.signum() == 0) return BigDecimal.ONE.round(mc);
        if (isIntegerValue(exponent)) {
            return powInt(base, exponent.toBigInteger(), mc);
        }
        if (base.signum() < 0) {
            throw new ArithmeticException("pow of negative base with non-integer exponent is undefined (real result)");
        }
        MathContext wc = extend(mc);
        BigDecimal result = exp(exponent.multiply(ln(base, wc), wc), wc);
        return result.round(mc);
    }

    private static BigDecimal powInt(BigDecimal base, BigInteger exponent, MathContext mc) {
        MathContext wc = extend(mc);
        boolean negative = exponent.signum() < 0;
        BigInteger n = exponent.abs();
        BigDecimal result = BigDecimal.ONE;
        BigDecimal b = base;
        while (n.signum() > 0) {
            if (n.testBit(0)) {
                result = result.multiply(b, wc);
            }
            b = b.multiply(b, wc);
            n = n.shiftRight(1);
        }
        if (negative) {
            result = BigDecimal.ONE.divide(result, wc);
        }
        return result.round(mc);
    }

    // ---------------------------------------------------------------
    // Exponential and logarithm
    // ---------------------------------------------------------------

    public static BigDecimal exp(BigDecimal x, MathContext mc) {
        MathContext wc = extend(mc);

        // Range-reduce: exp(x) = exp(x / 2^k) ^ (2^k), chosen so |x/2^k| < 1
        int k = 0;
        BigDecimal reduced = x;
        BigDecimal bound = BigDecimal.ONE;
        while (reduced.abs().compareTo(bound) > 0) {
            reduced = reduced.divide(TWO, wc);
            k++;
        }

        // Taylor series: sum x^n / n!
        BigDecimal sum = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal tol = tolerance(wc);
        for (int n = 1; n < 500; n++) {
            term = term.multiply(reduced, wc).divide(new BigDecimal(n), wc);
            sum = sum.add(term, wc);
            if (term.abs().compareTo(tol) < 0) break;
        }

        // Square back k times
        for (int i = 0; i < k; i++) {
            sum = sum.multiply(sum, wc);
        }
        return sum.round(mc);
    }

    /** Natural logarithm, x must be > 0. */
    public static BigDecimal ln(BigDecimal x, MathContext mc) {
        if (x.signum() <= 0) throw new ArithmeticException("ln of non-positive value");
        MathContext wc = extend(mc);

        if (x.compareTo(BigDecimal.ONE) == 0) return BigDecimal.ZERO;

        // Range-reduce via repeated sqrt until close to 1, track k (ln(x) = 2^k * ln(reduced))
        BigDecimal reduced = x;
        int k = 0;
        BigDecimal lower = new BigDecimal("0.75");
        BigDecimal upper = new BigDecimal("1.5");
        while (reduced.compareTo(lower) < 0 || reduced.compareTo(upper) > 0) {
            reduced = reduced.sqrt(wc);
            k++;
            if (k > 2000) throw new ArithmeticException("ln failed to converge");
        }

        // y = (x-1)/(x+1); ln(x) = 2*(y + y^3/3 + y^5/5 + ...)
        BigDecimal y = reduced.subtract(BigDecimal.ONE, wc).divide(reduced.add(BigDecimal.ONE, wc), wc);
        BigDecimal y2 = y.multiply(y, wc);
        BigDecimal term = y;
        BigDecimal sum = y;
        BigDecimal tol = tolerance(wc);
        for (int n = 3; n < 2000; n += 2) {
            term = term.multiply(y2, wc);
            BigDecimal addend = term.divide(new BigDecimal(n), wc);
            sum = sum.add(addend, wc);
            if (addend.abs().compareTo(tol) < 0) break;
        }
        BigDecimal result = TWO.multiply(sum, wc).multiply(new BigDecimal(BigInteger.TWO.pow(k)), wc);
        return result.round(mc);
    }

    public static BigDecimal log10(BigDecimal x, MathContext mc) {
        MathContext wc = extend(mc);
        BigDecimal result = ln(x, wc).divide(ln(BigDecimal.TEN, wc), wc);
        return result.round(mc);
    }

    public static BigDecimal log(BigDecimal x, BigDecimal base, MathContext mc) {
        MathContext wc = extend(mc);
        BigDecimal result = ln(x, wc).divide(ln(base, wc), wc);
        return result.round(mc);
    }

    // ---------------------------------------------------------------
    // Trigonometric functions
    // ---------------------------------------------------------------

    public static BigDecimal sin(BigDecimal x, MathContext mc) {
        MathContext wc = extend(mc);
        BigDecimal reduced = reduceAngle(x, wc);
        return sinTaylor(reduced, wc).round(mc);
    }

    public static BigDecimal cos(BigDecimal x, MathContext mc) {
        MathContext wc = extend(mc);
        BigDecimal reduced = reduceAngle(x, wc);
        return cosTaylor(reduced, wc).round(mc);
    }

    public static BigDecimal tan(BigDecimal x, MathContext mc) {
        MathContext wc = extend(mc);
        BigDecimal reduced = reduceAngle(x, wc);
        BigDecimal s = sinTaylor(reduced, wc);
        BigDecimal c = cosTaylor(reduced, wc);
        if (c.signum() == 0) throw new ArithmeticException("tan undefined (cos = 0)");
        return s.divide(c, wc).round(mc);
    }

    /** Reduces x into (-pi, pi]. */
    private static BigDecimal reduceAngle(BigDecimal x, MathContext wc) {
        BigDecimal twoPi = TWO.multiply(pi(wc), wc);
        BigDecimal n = x.divide(twoPi, 0, RoundingMode.HALF_EVEN);
        BigDecimal reduced = x.subtract(n.multiply(twoPi, wc), wc);
        BigDecimal pi = pi(wc);
        if (reduced.compareTo(pi) > 0) reduced = reduced.subtract(twoPi, wc);
        if (reduced.compareTo(pi.negate()) <= -1) reduced = reduced.add(twoPi, wc);
        return reduced;
    }

    private static BigDecimal sinTaylor(BigDecimal x, MathContext wc) {
        BigDecimal term = x;
        BigDecimal sum = x;
        BigDecimal x2 = x.multiply(x, wc);
        BigDecimal tol = tolerance(wc);
        for (int n = 3; n < 2000; n += 2) {
            term = term.multiply(x2, wc).negate().divide(new BigDecimal((long) n * (n - 1)), wc);
            sum = sum.add(term, wc);
            if (term.abs().compareTo(tol) < 0) break;
        }
        return sum;
    }

    private static BigDecimal cosTaylor(BigDecimal x, MathContext wc) {
        BigDecimal term = BigDecimal.ONE;
        BigDecimal sum = BigDecimal.ONE;
        BigDecimal x2 = x.multiply(x, wc);
        BigDecimal tol = tolerance(wc);
        for (int n = 2; n < 2000; n += 2) {
            term = term.multiply(x2, wc).negate().divide(new BigDecimal((long) n * (n - 1)), wc);
            sum = sum.add(term, wc);
            if (term.abs().compareTo(tol) < 0) break;
        }
        return sum;
    }

    // ---------------------------------------------------------------
    // Inverse trigonometric functions
    // ---------------------------------------------------------------

    public static BigDecimal atan(BigDecimal x, MathContext mc) {
        MathContext wc = extend(mc);
        return atanFull(x, wc).round(mc);
    }

    private static BigDecimal atanFull(BigDecimal x, MathContext wc) {
        if (x.signum() == 0) return BigDecimal.ZERO;
        if (x.signum() < 0) return atanFull(x.negate(), wc).negate();
        if (x.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal inv = BigDecimal.ONE.divide(x, wc);
            return pi(wc).divide(TWO, wc).subtract(atanFull(inv, wc), wc);
        }
        return atanTaylor(x, wc);
    }

    /** Assumes 0 <= x <= 1. Uses the half-angle identity to shrink x before summing the series. */
    private static BigDecimal atanTaylor(BigDecimal x, MathContext wc) {
        int k = 0;
        BigDecimal reduced = x;
        BigDecimal threshold = new BigDecimal("0.1");
        while (reduced.compareTo(threshold) > 0) {
            // atan(x) = 2*atan(x / (1 + sqrt(1+x^2)))
            BigDecimal denom = BigDecimal.ONE.add(BigDecimal.ONE.add(reduced.multiply(reduced, wc), wc).sqrt(wc), wc);
            reduced = reduced.divide(denom, wc);
            k++;
        }

        BigDecimal term = reduced;
        BigDecimal sum = reduced;
        BigDecimal x2 = reduced.multiply(reduced, wc);
        BigDecimal tol = tolerance(wc);
        for (int n = 3; n < 2000; n += 2) {
            term = term.multiply(x2, wc).negate();
            BigDecimal addend = term.divide(new BigDecimal(n), wc);
            sum = sum.add(addend, wc);
            if (addend.abs().compareTo(tol) < 0) break;
        }

        for (int i = 0; i < k; i++) {
            sum = sum.multiply(TWO, wc);
        }
        return sum;
    }

    /** Two-argument arctangent, matching the quadrant conventions of Math.atan2. */
    public static BigDecimal atan2(BigDecimal y, BigDecimal x, MathContext mc) {
        MathContext wc = extend(mc);
        if (x.signum() > 0) {
            return atanFull(y.divide(x, wc), wc).round(mc);
        }
        if (x.signum() < 0) {
            BigDecimal base = atanFull(y.divide(x, wc), wc);
            BigDecimal pi = pi(wc);
            return (y.signum() >= 0 ? base.add(pi, wc) : base.subtract(pi, wc)).round(mc);
        }
        // x == 0
        if (y.signum() > 0) return pi(wc).divide(TWO, wc).round(mc);
        if (y.signum() < 0) return pi(wc).divide(TWO, wc).negate().round(mc);
        return BigDecimal.ZERO; // atan2(0,0) is conventionally 0
    }

    public static BigDecimal asin(BigDecimal x, MathContext mc) {
        if (x.abs().compareTo(BigDecimal.ONE) > 0) throw new ArithmeticException("asin domain is [-1, 1]");
        MathContext wc = extend(mc);
        if (x.compareTo(BigDecimal.ONE) == 0) return pi(wc).divide(TWO, wc).round(mc);
        if (x.compareTo(BigDecimal.ONE.negate()) == 0) return pi(wc).divide(TWO, wc).negate().round(mc);
        BigDecimal denom = BigDecimal.ONE.subtract(x.multiply(x, wc), wc).sqrt(wc);
        return atanFull(x.divide(denom, wc), wc).round(mc);
    }

    public static BigDecimal acos(BigDecimal x, MathContext mc) {
        MathContext wc = extend(mc);
        BigDecimal result = pi(wc).divide(TWO, wc).subtract(asin(x, wc), wc);
        return result.round(mc);
    }

    // ---------------------------------------------------------------
    // Hyperbolic functions
    // ---------------------------------------------------------------

    public static BigDecimal sinh(BigDecimal x, MathContext mc) {
        MathContext wc = extend(mc);
        BigDecimal ex = exp(x, wc);
        BigDecimal enx = BigDecimal.ONE.divide(ex, wc);
        return ex.subtract(enx, wc).divide(TWO, wc).round(mc);
    }

    public static BigDecimal cosh(BigDecimal x, MathContext mc) {
        MathContext wc = extend(mc);
        BigDecimal ex = exp(x, wc);
        BigDecimal enx = BigDecimal.ONE.divide(ex, wc);
        return ex.add(enx, wc).divide(TWO, wc).round(mc);
    }

    public static BigDecimal tanh(BigDecimal x, MathContext mc) {
        MathContext wc = extend(mc);
        BigDecimal ex = exp(x, wc);
        BigDecimal enx = BigDecimal.ONE.divide(ex, wc);
        return ex.subtract(enx, wc).divide(ex.add(enx, wc), wc).round(mc);
    }

    // ---------------------------------------------------------------
    // Angle conversion and misc
    // ---------------------------------------------------------------

    public static BigDecimal toRadians(BigDecimal degrees, MathContext mc) {
        MathContext wc = extend(mc);
        return degrees.multiply(pi(wc), wc).divide(new BigDecimal("180"), wc).round(mc);
    }

    public static BigDecimal toDegrees(BigDecimal radians, MathContext mc) {
        MathContext wc = extend(mc);
        return radians.multiply(new BigDecimal("180"), wc).divide(pi(wc), wc).round(mc);
    }

    public static BigDecimal abs(BigDecimal x) {
        return x.abs();
    }

    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    public static BigDecimal hypot(BigDecimal x, BigDecimal y, MathContext mc) {
        MathContext wc = extend(mc);
        BigDecimal result = x.multiply(x, wc).add(y.multiply(y, wc), wc).sqrt(wc);
        return result.round(mc);
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    private static boolean isIntegerValue(BigDecimal x) {
        return x.stripTrailingZeros().scale() <= 0;
    }

    private static BigDecimal tolerance(MathContext wc) {
        return BigDecimal.ONE.scaleByPowerOfTen(-(wc.getPrecision() + 2));
    }
}

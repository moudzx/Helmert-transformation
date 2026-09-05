package helmert;

import bigmath.BigMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Geodesy {

    public static final MathContext MC = new MathContext(50, RoundingMode.HALF_UP);
    private static final BigDecimal TWO = new BigDecimal("2");
    private static final BigDecimal ONE_MILLION = new BigDecimal("1000000");

    public static class Ellipsoid {
        public final BigDecimal a, f, eSquared;
        public final String name;

        public Ellipsoid(String name, BigDecimal a, BigDecimal invF) {
            this.name = name;
            this.a = a;
            this.f = invF.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.ONE.divide(invF, MC);
            this.eSquared = TWO.multiply(this.f, MC).subtract(this.f.multiply(this.f, MC), MC);
        }

        @Override
        public String toString() { return name; }
    }

    public static class XYZ {
        public final BigDecimal x, y, z;
        public XYZ(BigDecimal x, BigDecimal y, BigDecimal z) { this.x = x; this.y = y; this.z = z; }
    }

    public static class LLH {
        public final BigDecimal lat, lon, h;
        public LLH(BigDecimal lat, BigDecimal lon, BigDecimal h) { this.lat = lat; this.lon = lon; this.h = h; }
    }

    public static class HelmertParams {
        public BigDecimal Tx, Ty, Tz;
        public BigDecimal Rx, Ry, Rz;
        public BigDecimal s;
    }

    public static class Result {
        public final LLH srcLLH;
        public final XYZ srcXYZ;
        public final XYZ dstXYZ;
        public final LLH dstLLH;

        public Result(LLH srcLLH, XYZ srcXYZ, XYZ dstXYZ, LLH dstLLH) {
            this.srcLLH = srcLLH;
            this.srcXYZ = srcXYZ;
            this.dstXYZ = dstXYZ;
            this.dstLLH = dstLLH;
        }
    }

    private static final BigDecimal SEC_TO_RAD =
            BigMath.pi(MC).divide(new BigDecimal("3600").multiply(new BigDecimal("180")), MC);

    public static BigDecimal toRadians(BigDecimal degrees) {
        return BigMath.toRadians(degrees, MC);
    }

    public static BigDecimal toDegrees(BigDecimal radians) {
        return BigMath.toDegrees(radians, MC);
    }

    private static BigDecimal sin(BigDecimal x) {
        return BigMath.sin(x, MC);
    }

    private static BigDecimal cos(BigDecimal x) {
        return BigMath.cos(x, MC);
    }

    private static BigDecimal atan2(BigDecimal y, BigDecimal x) {
        return BigMath.atan2(y, x, MC);
    }

    private static BigDecimal sqrt(BigDecimal x) {
        return BigMath.sqrt(x, MC);
    }

    public static XYZ geographicToGeocentric(LLH llh, Ellipsoid e) {
        BigDecimal sinLat = sin(llh.lat);
        BigDecimal cosLat = cos(llh.lat);
        BigDecimal sinLon = sin(llh.lon);
        BigDecimal cosLon = cos(llh.lon);

        BigDecimal n = e.a.divide(sqrt(BigDecimal.ONE.subtract(e.eSquared.multiply(sinLat.multiply(sinLat, MC), MC), MC)), MC);

        BigDecimal x = n.add(llh.h, MC).multiply(cosLat, MC).multiply(cosLon, MC);
        BigDecimal y = n.add(llh.h, MC).multiply(cosLat, MC).multiply(sinLon, MC);
        BigDecimal z = n.multiply(BigDecimal.ONE.subtract(e.eSquared, MC), MC).add(llh.h, MC).multiply(sinLat, MC);

        return new XYZ(x, y, z);
    }

    public static XYZ helmertTransform(XYZ p, HelmertParams h) {
        BigDecimal Rx = h.Rx.multiply(SEC_TO_RAD, MC);
        BigDecimal Ry = h.Ry.multiply(SEC_TO_RAD, MC);
        BigDecimal Rz = h.Rz.multiply(SEC_TO_RAD, MC);
        BigDecimal s = BigDecimal.ONE.add(h.s.divide(ONE_MILLION, MC), MC);

        BigDecimal x2 = h.Tx.add(s.multiply(p.z.multiply(Ry, MC).subtract(p.y.multiply(Rz, MC), MC).add(p.x, MC), MC), MC);
        BigDecimal y2 = h.Ty.add(s.multiply(p.x.multiply(Rz, MC).subtract(p.z.multiply(Rx, MC), MC).add(p.y, MC), MC), MC);
        BigDecimal z2 = h.Tz.add(s.multiply(p.y.multiply(Rx, MC).subtract(p.x.multiply(Ry, MC), MC).add(p.z, MC), MC), MC);

        return new XYZ(x2, y2, z2);
    }

    public static LLH geocentricToGeographic(XYZ pos, Ellipsoid e) {
        BigDecimal x = pos.x, y = pos.y, z = pos.z;
        BigDecimal r = sqrt(x.multiply(x, MC).add(y.multiply(y, MC), MC));
        BigDecimal lat = atan2(z, r.multiply(BigDecimal.ONE.subtract(e.f, MC), MC));

        BigDecimal n;
        BigDecimal tolerance = new BigDecimal("1e-15");
        while (true) {
            BigDecimal sinLat = sin(lat);
            n = e.a.divide(sqrt(BigDecimal.ONE.subtract(e.eSquared.multiply(sinLat.multiply(sinLat, MC), MC), MC)), MC);
            BigDecimal newLat = atan2(z.add(e.eSquared.multiply(n, MC).multiply(sinLat, MC), MC), r);
            if (newLat.subtract(lat, MC).abs().compareTo(tolerance) < 0) {
                lat = newLat;
                break;
            }
            lat = newLat;
        }

        BigDecimal sinLat = sin(lat);
        n = e.a.divide(sqrt(BigDecimal.ONE.subtract(e.eSquared.multiply(sinLat.multiply(sinLat, MC), MC), MC)), MC);
        BigDecimal lon = atan2(y, x);
        BigDecimal h = r.divide(cos(lat), MC).subtract(n, MC);

        return new LLH(lat, lon, h);
    }

    public static Result transform(LLH srcLLH, Ellipsoid srcE, Ellipsoid dstE, HelmertParams params) {
        XYZ srcXYZ = geographicToGeocentric(srcLLH, srcE);
        XYZ dstXYZ = helmertTransform(srcXYZ, params);
        LLH dstLLH = geocentricToGeographic(dstXYZ, dstE);
        return new Result(srcLLH, srcXYZ, dstXYZ, dstLLH);
    }

    public static final Ellipsoid[] PRESETS = {
        new Ellipsoid("WGS 84",           new BigDecimal("6378137.0"), new BigDecimal("298.257223563")),
        new Ellipsoid("Clarke 1880 IGN",  new BigDecimal("6378249.2"), new BigDecimal("293.4660212936")),
        new Ellipsoid("GRS 80",           new BigDecimal("6378137.0"), new BigDecimal("298.257222101")),
        new Ellipsoid("NAD 83",           new BigDecimal("6378137.0"), new BigDecimal("298.257222101")),
        new Ellipsoid("GDA 94",           new BigDecimal("6378137.0"), new BigDecimal("298.257222101")),
        new Ellipsoid("Custom",           new BigDecimal("6378137.0"), new BigDecimal("298.257223563")),
    };
}

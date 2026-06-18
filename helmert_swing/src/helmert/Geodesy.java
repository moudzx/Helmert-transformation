package helmert;

public class Geodesy {

    public static class Ellipsoid {
        public final double a, f, eSquared;
        public final String name;

        public Ellipsoid(String name, double a, double invF) {
            this.name = name;
            this.a = a;
            this.f = invF == 0 ? 0 : 1.0 / invF;
            this.eSquared = 2 * this.f - this.f * this.f;
        }

        @Override
        public String toString() { return name; }
    }

    public static class XYZ {
        public final double x, y, z;
        public XYZ(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
    }

    public static class LLH {
        public final double lat, lon, h;
        public LLH(double lat, double lon, double h) { this.lat = lat; this.lon = lon; this.h = h; }
    }

    public static class HelmertParams {
        public double Tx, Ty, Tz;
        public double Rx, Ry, Rz;
        public double s;
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

    private static final double SEC_TO_RAD = Math.PI / (3600.0 * 180.0);

    public static XYZ geographicToGeocentric(LLH llh, Ellipsoid e) {
        double sinLat = Math.sin(llh.lat);
        double cosLat = Math.cos(llh.lat);
        double sinLon = Math.sin(llh.lon);
        double cosLon = Math.cos(llh.lon);
        double n = e.a / Math.sqrt(1.0 - e.eSquared * sinLat * sinLat);
        double x = (n + llh.h) * cosLat * cosLon;
        double y = (n + llh.h) * cosLat * sinLon;
        double z = (n * (1.0 - e.eSquared) + llh.h) * sinLat;
        return new XYZ(x, y, z);
    }

    public static XYZ helmertTransform(XYZ p, HelmertParams h) {
        double Rx = h.Rx * SEC_TO_RAD;
        double Ry = h.Ry * SEC_TO_RAD;
        double Rz = h.Rz * SEC_TO_RAD;
        double s = 1.0 + h.s * 1e-6;
        double x2 = h.Tx + s * (p.z * Ry - p.y * Rz + p.x);
        double y2 = h.Ty + s * (p.x * Rz - p.z * Rx + p.y);
        double z2 = h.Tz + s * (p.y * Rx - p.x * Ry + p.z);
        return new XYZ(x2, y2, z2);
    }

    public static LLH geocentricToGeographic(XYZ pos, Ellipsoid e) {
        double x = pos.x, y = pos.y, z = pos.z;
        double r = Math.sqrt(x * x + y * y);
        double lat = Math.atan2(z, r * (1.0 - e.f));
        double n;
        while (true) {
            n = e.a / Math.sqrt(1.0 - e.eSquared * Math.sin(lat) * Math.sin(lat));
            double newLat = Math.atan2(z + e.eSquared * n * Math.sin(lat), r);
            if (Math.abs(newLat - lat) < 1e-12) { lat = newLat; break; }
            lat = newLat;
        }
        n = e.a / Math.sqrt(1.0 - e.eSquared * Math.sin(lat) * Math.sin(lat));
        double lon = Math.atan2(y, x);
        double h = r / Math.cos(lat) - n;
        return new LLH(lat, lon, h);
    }

    public static Result transform(LLH srcLLH, Ellipsoid srcE, Ellipsoid dstE, HelmertParams params) {
        XYZ srcXYZ = geographicToGeocentric(srcLLH, srcE);
        XYZ dstXYZ = helmertTransform(srcXYZ, params);
        LLH dstLLH = geocentricToGeographic(dstXYZ, dstE);
        return new Result(srcLLH, srcXYZ, dstXYZ, dstLLH);
    }

    public static final Ellipsoid[] PRESETS = {
        new Ellipsoid("WGS 84",           6378137.0,   298.257223563),
        new Ellipsoid("Clarke 1880 IGN",  6378249.2,   293.4660212936),
        new Ellipsoid("GRS 80",           6378137.0,   298.257222101),
        new Ellipsoid("NAD 83",           6378137.0,   298.257222101),
        new Ellipsoid("GDA 94",           6378137.0,   298.257222101),
        new Ellipsoid("Custom",           6378137.0,   298.257223563),
    };
}

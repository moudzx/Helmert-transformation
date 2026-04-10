#include <stdio.h>
#include <math.h>

#define PI 3.141592653589793

double a, f, a2, f2;
double eSquared, eSquared2;
double X, Y, Z, X2, Y2, Z2;
double Tx, Ty, Tz, Rx, Ry, Rz, s;
double lat, lon, h, lat2, lon2, h2;
double N, N2;

void geographic_to_geocentric(){
    N  = a / sqrt(1.0 - eSquared * sin(lat) * sin(lat));
    X = (N + h) * cos(lat) * cos(lon);
    Y = (N + h) * cos(lat) * sin(lon);
    Z = (N * (1.0 - eSquared) + h) * sin(lat);
}

void helmert_transform(){
    X2 = Tx + (1.0 + s*1e-6) * (X - Y*Rz*(PI/(180.0*3600.0)) + Z*Ry*(PI/(180.0*3600.0)));
    Y2 = Ty + (1.0 + s*1e-6) * (X*Rz*(PI/(180.0*3600.0)) + Y - Z*Rx*(PI/(180.0*3600.0)));
    Z2 = Tz + (1.0 + s*1e-6) * (-X*Ry*(PI/(180.0*3600.0)) + Y*Rx*(PI/(180.0*3600.0)) + Z);
}

void geocentric_to_geographic(){
    double r = sqrt(X2*X2 + Y2*Y2);
    double latTemp = atan2(Z2, r * (1.0 - f2));

    while(1){
	N2 = a2 / sqrt(1.0 - eSquared2 * sin(latTemp) * sin(latTemp));
        lat2 = atan2(Z2 + eSquared2 * N2 * sin(latTemp), r);
        if (fabs(lat2 - latTemp) < 1e-12) break;
        latTemp = lat2;
    }

    lon2 = atan2(Y2, X2);
    N2 = a2 / sqrt(1.0 - eSquared2 * sin(lat2) * sin(lat2));
    h2 = r / cos(lat2) - N2;  
}

int main(){
    printf("Geographic Coordinate Transformation using Helmet7-parameter\n\n");

    printf("- Source ellipsoid:\n");
    printf("Semi-major axis (a): ");	scanf("%lf", &a);
    printf("Flattening (f): ");		scanf("%lf", &f);
    eSquared = 2*f - f*f;

    printf("\n- Helmet parameters\n");
    printf("\nTx Ty Tz (m)     : ");	scanf("%lf %lf %lf", &Tx, &Ty, &Tz);
    printf("Rx Ry Rz (arcsec): ");	scanf("%lf %lf %lf", &Rx, &Ry, &Rz);
    printf("Scale    (ppm)   : ");	scanf("%lf", &s);

    printf("\n- Target ellipsoid\n");
    printf("Semi-major axis (a): ");	scanf("%lf", &a2);
    printf("Flattening (f): ");		scanf("%lf", &f2);
    eSquared2 = 2*f2 - f2*f2;

    printf("\n- Source coordinates:\n");
    printf("\nLatitude  (deg): ");	scanf("%lf", &lat);
    printf("Longitude (deg): ");	scanf("%lf", &lon);
    printf("Height    (m)  : ");	scanf("%lf", &h);

    // trig functions expect radian
    lat = lat * (PI / 180.0);
    lon = lon * (PI / 180.0);

    geographic_to_geocentric();
    helmert_transform();
    geocentric_to_geographic();

    printf("\n- Results:\n");

    printf("Source geographic coordinates: Lat=%lf  Lon=%lf  H=%lf m\n\n", lat * (180.0/PI), lon * (180.0/PI), h);
    printf("Source geocentric position vector: X=%lf  Y=%lf  Z=%lf\n", X, Y, Z);

    printf("Target geographic coordinates: Lat=%lf  Lon=%lf  H=%lf m\n", lat2 * (180.0/PI), lon2 * (180.0/PI), h2);
    printf("Target geocentric position vector: X=%lf  Y=%lf  Z=%lf\n", X2, Y2, Z2);
    return 0;
}
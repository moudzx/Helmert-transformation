#include <stdio.h>
#include <math.h>

typedef struct {
	double a, b, f, eSquared;
} Ellipsoid;

typedef struct {
	double x, y, z;
} PositionVector;

typedef struct {
	double lat, lon, h;
} Coordinate;

typedef struct {
	Ellipsoid ellipse;
	Coordinate coordinate;
	PositionVector position;
} Datum;

typedef struct {
	double Tx, Ty, Tz;
	double Rx, Ry, Rz;
	double s;
} Helmert;

#define PI 3.141592653589793
#define SEC_TO_RAD(x) ((x) * PI/(3600.0 * 180.0))

PositionVector geographic_to_geocentric(Datum datum){

	PositionVector position;

    double sin_lon = sin(datum.coordinate.lon);
    double cos_lon = cos(datum.coordinate.lon);
	double sin_lat = sin(datum.coordinate.lat);
    double cos_lat = cos(datum.coordinate.lat);

	double n = datum.ellipse.a / sqrt(1.0 - datum.ellipse.eSquared * sin_lat * sin_lat);

	position.x = (n + datum.coordinate.h) * cos_lat * cos_lon;
    position.y = (n + datum.coordinate.h) * cos_lat * sin_lon;
	position.z = (n * (1.0 - datum.ellipse.eSquared) + datum.coordinate.h) * sin_lat;

	return position;
}

PositionVector helmert_transform(PositionVector position1, Helmert helmert){

	PositionVector position2;

	double Rx = SEC_TO_RAD(helmert.Rx);
	double Ry = SEC_TO_RAD(helmert.Ry);
	double Rz = SEC_TO_RAD(helmert.Rz);

	position2.x = helmert.Tx + (1.0 + helmert.s * 1e-6) * (position1.z * Ry - position1.y * Rz + position1.x);
	position2.y = helmert.Ty + (1.0 + helmert.s * 1e-6) * (position1.x * Rz - position1.z * Rx + position1.y);
	position2.z = helmert.Tz + (1.0 + helmert.s * 1e-6) * (position1.y * Rx - position1.x * Ry + position1.z);

	return position2;
}

Coordinate geocentric_to_geographic(Datum datum){

	Coordinate coordinate;

	double x = datum.position.x;
    double y = datum.position.y;
    double z = datum.position.z;

	double n;
	double r = sqrt(x * x + y * y);
	double tempLat = atan2(z, r * (1.0 - datum.ellipse.f));

    while(1) {
		n = datum.ellipse.a / sqrt(1.0 - datum.ellipse.eSquared * sin(tempLat) * sin(tempLat));
		coordinate.lat = atan2(z + datum.ellipse.eSquared * n * sin(tempLat), r);
    	if (fabs(coordinate.lat - tempLat) < 1e-12) break;
    	tempLat = coordinate.lat;
	}

    n = datum.ellipse.a / sqrt(1.0 - datum.ellipse.eSquared * sin(coordinate.lat) * sin(coordinate.lat));

    coordinate.lon = atan2(y, x);
    coordinate.h = r / cos(coordinate.lat) - n;

	return coordinate;
}

int main(){

	Ellipsoid WGS84;
	WGS84.a = 6378137;
	WGS84.f = 1/298.257223563;
	WGS84.eSquared = 2 * WGS84.f - WGS84.f * WGS84.f;

	Ellipsoid Clarke1880;
	Clarke1880.a = 6378249.2;
	Clarke1880.f = 1/293.4660212936;
	Clarke1880.eSquared = 2 * Clarke1880.f - Clarke1880.f * Clarke1880.f;

	Ellipsoid GRS80;
	GRS80.a = 6378137;
	GRS80.f = 1/298.257222101;
	GRS80.eSquared = 2 * GRS80.f - GRS80.f * GRS80.f;

	// same parameters
	Ellipsoid NAD83 = GRS80;
	Ellipsoid GDA94 = GRS80;


	printf("Geographic Coordinate Transformation using Helmet 7-parameter\n\n");

	Ellipsoid sourceEllipse;
	printf("1) WGS 80 \n2) Clarke 1880 IGN \n3) GRS 80 \n4) NAD 83 \n5) GDA 94 \n6) Custom \n");
	printf("- Select source ellipsoid: \n");
	int choice;
	scanf("%d",&choice);
	
	switch(choice){
		case 1: sourceEllipse = WGS84; break;
		case 2: sourceEllipse = Clarke1880; break;
		case 3: sourceEllipse = GRS80; break;
		case 4: sourceEllipse = NAD83; break;
		case 5: sourceEllipse = GDA94; break;
		case 6: printf("Semi-major axis (a): ");
				scanf("%lf", &sourceEllipse.a);
				printf("Flattening (f): ");
				scanf("%lf", &sourceEllipse.f);
				sourceEllipse.eSquared = 2 * sourceEllipse.f - sourceEllipse.f * sourceEllipse.f;
				break;
		default: printf("Invalid choice. Please enter 1, 2, 3, 4, 5 or 6.\n");
	}


	Ellipsoid targetEllipse;
	printf("- Select target ellipsoid: \n");
	scanf("%d",&choice);
	
	switch(choice){
		case 1: targetEllipse = WGS84; break;
		case 2: targetEllipse = Clarke1880; break;
		case 3: targetEllipse = GRS80; break;
		case 4: targetEllipse = NAD83; break;
		case 5: targetEllipse = GDA94; break;
		case 6: printf("Semi-major axis (a): ");
				scanf("%lf", &targetEllipse.a);
				printf("Flattening (f): ");
				scanf("%lf", &targetEllipse.f);
				targetEllipse.eSquared = 2 * targetEllipse.f - targetEllipse.f * targetEllipse.f;
				break;
		default: printf("Invalid choice. Please enter 1, 2, 3, 4, 5 or 6.\n");
	}


    Helmert helmert;
    printf("\n- Enter Helmet parameters\n");
    printf("\nTx Ty Tz (m)   : ");
    scanf("%lf %lf %lf", &helmert.Tx, &helmert.Ty, &helmert.Tz);
    printf("Rx Ry Rz (arcsec): ");
    scanf("%lf %lf %lf", &helmert.Rx, &helmert.Ry, &helmert.Rz);
    printf("Scale    (ppm)   : ");
    scanf("%lf", &helmert.s);

	Coordinate sourceCoord;
	printf("\n- Enter your coordinates:\n");
	printf("\nLatitude  (deg): ");
	scanf("%lf", &sourceCoord.lat);
	printf("Longitude (deg): ");
	scanf("%lf", &sourceCoord.lon);
	printf("Height    (m)  : ");
	scanf("%lf", &sourceCoord.h);

	// trig functions expect radian
	sourceCoord.lat *= (PI / 180.0);
	sourceCoord.lon *= (PI / 180.0);

	Datum datum1;
	datum1.ellipse = sourceEllipse;
	datum1.coordinate = sourceCoord;
	datum1.position = geographic_to_geocentric(datum1);

	Datum datum2;
	datum2.ellipse = targetEllipse;
	datum2.position = helmert_transform(datum1.position, helmert);
	datum2.coordinate = geocentric_to_geographic(datum2);


	// radian to degree
	datum1.coordinate.lat *= (180.0 / PI);
    datum1.coordinate.lon *= (180.0 / PI);
    datum2.coordinate.lat *= (180.0 / PI);
    datum2.coordinate.lon *= (180.0 / PI);

	printf("\n- Results:\n");

	printf("Source geographic coordinates:\nLat=%lf  Lon=%lf  H=%lf m\n\n",
		datum1.coordinate.lat, datum1.coordinate.lon, datum1.coordinate.h);
	printf("Source geocentric position vector:\nX=%lf  Y=%lf  Z=%lf\n\n",
		datum1.position.x, datum1.position.y, datum1.position.z);

	printf("Target geographic coordinates:\nLat=%lf  Lon=%lf  H=%lf m\n\n",
		datum2.coordinate.lat, datum2.coordinate.lon, datum2.coordinate.h);
	printf("Target geocentric position vector:\nX=%lf  Y=%lf  Z=%lf\n\n",
		datum2.position.x, datum2.position.y, datum2.position.z);

	return 0;
}

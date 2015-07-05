#ifndef __POINT_H__
#define __POINT_H__

#include <vector>

struct PointLite
{
    double p[3];
    PointLite() {}
    PointLite(double x, double y, double z)
    {
        p[0] = x;
        p[1] = y;
        p[2] = z;
    }
};


struct Point
{
    char met[16];
    char utc[24];
    char rangeStr[12];
    double range;
    double time;
    double scpos[3];
    double targetpos[3];
    double closestpoint[3]; /* closest point on asteroid to targetpos */
    double boredir[3];
    double ancillary1[3]; /* arbitrary place for ancillary data as needed */
    double intersectpos[3]; /* intersection point on asteroid in direction of boresight. may be different from closest point */
    unsigned char isNoise; /* 1 if considered noise, 0 otherwise */
    Point() {}
    Point(double t, double p[3]):
        time(t)
    {
        scpos[0] = p[0];
        scpos[1] = p[1];
        scpos[2] = p[2];
    }
};

typedef std::vector<Point> Track;


#endif

#ifndef LIDARDATA_H
#define LIDARDATA_H

#include <vector>
#include <string>
#include "constants.h"

using namespace std;

struct LidarPoint
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
    unsigned char isNoise; /* 1 if considered noise, 0 otherwise */
    LidarPoint() {}
    LidarPoint(double t, double p[3]):
        time(t)
    {
        scpos[0] = p[0];
        scpos[1] = p[1];
        scpos[2] = p[2];
    }
};

typedef std::vector<LidarPoint> LidarTrack;

class LidarData
{
public:
    LidarData();

    static LidarTrack loadTrack(const string& filename,
                                bool convertToJ2000,
                                double startTime,
                                double stopTime);

    static void saveTrack(const string& filename,
                          const LidarTrack& track,
                          bool convertFromJ2000);

    static void setBodyType(BodyType bt)
    {
        bodyType = bt;
    }
    static BodyType getBodyType()
    {
        return bodyType;
    }
    static const char* getBodyName()
    {
        if (bodyType == ITOKAWA)
            return ITOKAWA_NAME;
        else
            return EROS_NAME;
    }
    static const char* getBodyFrame()
    {
        if (bodyType == ITOKAWA)
            return ITOKAWA_FRAME;
        else
            return EROS_FRAME;
    }

private:

    static BodyType bodyType;
};

#endif // LIDARDATA_H

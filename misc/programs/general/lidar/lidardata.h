#ifndef LIDARDATA_H
#define LIDARDATA_H

#include <string>
#include "track.h"

using namespace std;

class LidarData
{
public:
    LidarData();

    static Track loadTrack(const string& filename,
                                bool convertToJ2000,
                                const string& bodyName,
                                double startTime,
                                double stopTime);

    static void saveTrack(const string& filename,
                          const Track& track,
                          bool convertFromJ2000,
                          const string& bodyName);

    static Track loadTrack(const string& filename,
                           bool convertUtcToEt);

    static void saveTrack(const string& filename,
                          const Track& track);

};

#endif // LIDARDATA_H

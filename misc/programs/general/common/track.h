#ifndef __TRACK_H__
#define __TRACK_H__

#include <vector>
#include "point.h"

typedef std::vector<Point> Track;

void computeTrackStats(const Track& track, double& minError, double& maxError, double& rms);

void computeTracksStats(const std::vector<Track>& tracks, double& minError, double& maxError, double& rms);


#endif

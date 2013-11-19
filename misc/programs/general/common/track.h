#ifndef __TRACK_H__
#define __TRACK_H__

#include <vector>
#include "point.h"

typedef std::vector<Point> Track;

void computeTrackStats(const Track& track, double& minError, double& maxError, double& rms, double& meanError, double& std);

void computeMeanTranslationBetweenTracks(const Track& track1, const Track& track2, double distance[3]);

void timeSortTrack(Track& track);

Track concatTracks(const std::vector<Track>& tracks, bool timeSort);

#endif

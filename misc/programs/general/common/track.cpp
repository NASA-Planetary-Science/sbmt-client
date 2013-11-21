#include <limits>
#include <iostream>
#include <algorithm>
#include <cstdlib>
#include "track.h"
#include "closest-point-vtk.h"
#include "mathutil.h"


void computeTrackStats(const Track& track, double& minError, double& maxError, double& rms, double& meanError, double& std)
{
    int size = track.size();
    int found;
    minError = std::numeric_limits<double>::max();
    maxError = 0.0;
    double errorSum  = 0.0;
    double errorSquaredSum = 0.0;
    for (int i=0; i<size; ++i)
    {
        const Point& p = track.at(i);

        // compute distance to closest point on shape model
        double closestPoint[3];
        findClosestPointVtk(p.targetpos, closestPoint, &found);

        double errorSquared = Distance2BetweenPoints(p.targetpos, closestPoint);
        double error = sqrt(errorSquared);

        errorSquaredSum += errorSquared;
        errorSum += error;

        if (error < minError)
            minError = error;
        if (error > maxError)
            maxError = error;
    }

    rms = sqrt(errorSquaredSum / (double)size);
    meanError = errorSum / (double)size;
    std = sqrt(errorSquaredSum / (double)size - meanError * meanError);
}

void computeMeanTranslationBetweenTracks(const Track& track1, const Track& track2, double translation[3])
{
    translation[0] = 0.0;
    translation[1] = 0.0;
    translation[2] = 0.0;

    int size = track1.size();
    for (int i=0; i<size; ++i)
    {
        const Point& p1 = track1.at(i);
        const Point& p2 = track2.at(i);

        translation[0] += (p2.targetpos[0] - p1.targetpos[0]);
        translation[1] += (p2.targetpos[1] - p1.targetpos[1]);
        translation[2] += (p2.targetpos[2] - p1.targetpos[2]);
    }

    translation[0] /= size;
    translation[1] /= size;
    translation[2] /= size;
}

static bool trackTimeFunction (const Point& p1, const Point& p2)
{
    return (p1.time < p2.time);
}

void timeSortTrack(Track& track)
{
    std::sort(track.begin(), track.end(), trackTimeFunction);
}

Track concatTracks(const std::vector<Track>& tracks, bool timeSort)
{
    Track track;

    for (size_t i=0; i<tracks.size(); ++i)
    {
        track.insert(track.end(), tracks.at(i).begin(), tracks.at(i).end());
    }

    if (timeSort)
        timeSortTrack(track);

    return track;
}

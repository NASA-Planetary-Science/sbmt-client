#include <limits>
#include "track.h"
#include "closest-point-vtk.h"
#include "mathutil.h"


void computeTrackStats(const Track& track, double& minError, double& maxError, double& rms)
{
    int size = track.size();
    int found;
    minError = std::numeric_limits<double>::max();
    maxError = 0.0;
    rms = 0.0;
    for (int i=0; i<size; ++i)
    {
        const Point& p = track.at(i);

        // compute distance to closest point on shape model
        struct PointLite closestPoint;
        findClosestPointVtk(p.targetpos, closestPoint.p, &found);

        double errorSquared = Distance2BetweenPoints(p.targetpos, closestPoint.p);
        rms += errorSquared;
        double error = sqrt(errorSquared);
        if (error < minError)
            minError = error;
        if (error > maxError)
            maxError = error;
    }

    rms = sqrt(rms / (double)size);
}

void computeTracksStats(const std::vector<Track>& tracks, double& minError, double& maxError, double& rms)
{
    int numTracks = tracks.size();
    int totalNumPoints = 0;
    int found;
    minError = std::numeric_limits<double>::max();
    maxError = 0.0;
    rms = 0.0;
    for (int j=0; j<numTracks; ++j)
    {
        const Track& track = tracks.at(j);
        int size = track.size();
        for (int i=0; i<size; ++i)
        {
            const Point& p = track.at(i);

            // compute distance to closest point on shape model
            struct PointLite closestPoint;
            findClosestPointVtk(p.targetpos, closestPoint.p, &found);

            double errorSquared = Distance2BetweenPoints(p.targetpos, closestPoint.p);
            rms += errorSquared;
            double error = sqrt(errorSquared);
            if (error < minError)
                minError = error;
            if (error > maxError)
                maxError = error;
        }

        totalNumPoints += size;
    }

    rms = sqrt(rms / (double)totalNumPoints);
}

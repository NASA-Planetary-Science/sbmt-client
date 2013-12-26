#include "track-optimizer.h"
#include "SpiceUsr.h"
#include "icp.h"
#include "icp-vtk.h"
#include "icp-intersection.h"

void TrackOptimizer::setTrack(const Track &track)
{
    track__ = track;
}

void TrackOptimizer::optimize()
{
    int trackSize = track__.size();
    printf("Optimizing track with size %d\n", trackSize);
    printf("From %s to %s\n\n", track__[0].utc, track__[trackSize-1].utc);


    printf("Beginning ICP\n");

    std::vector<PointLite> sources(trackSize);
    std::vector<PointLite> targets(trackSize);
    std::vector<PointLite> scpos(trackSize);
    for (int i=0; i<trackSize; ++i)
    {
        Point pt = track__[i];
        sources[i].p[0] = pt.targetpos[0];
        sources[i].p[1] = pt.targetpos[1];
        sources[i].p[2] = pt.targetpos[2];

        scpos[i].p[0] = pt.scpos[0];
        scpos[i].p[1] = pt.scpos[1];
        scpos[i].p[2] = pt.scpos[2];
    }


    icp2(&sources[0], trackSize, &scpos[0], optimalTranslation__, &targets[0]);


    optimizedTrack__ = track__;
    for (int i=0; i<trackSize; ++i)
    {
        Point pt = optimizedTrack__[i];

        pt.scpos[0] = scpos[i].p[0];
        pt.scpos[1] = scpos[i].p[1];
        pt.scpos[2] = scpos[i].p[2];

        pt.targetpos[0] = sources[i].p[0];
        pt.targetpos[1] = sources[i].p[1];
        pt.targetpos[2] = sources[i].p[2];

        pt.closestpoint[0] = targets[i].p[0];
        pt.closestpoint[1] = targets[i].p[1];
        pt.closestpoint[2] = targets[i].p[2];

        optimizedTrack__[i] = pt;
    }

    printf("Finished optimizing track\n\n\n\n");
}

Track TrackOptimizer::getOptimizedTrack() const
{
    return optimizedTrack__;
}

void TrackOptimizer::getOptimalTranslation(double translation[3]) const
{
    translation[0] = optimalTranslation__[0];
    translation[1] = optimalTranslation__[1];
    translation[2] = optimalTranslation__[2];
}

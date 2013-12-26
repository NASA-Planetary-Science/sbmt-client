#ifndef TRACK_OPTIMIZER_H
#define TRACK_OPTIMIZER_H

#include "track.h"

/**
 * Class for finding the optimal translation of a lidar track that best aligns it with
 * a shape model. To use this class, set the track by calling setTrack. Then call optimize().
 * Finally, you can get the optimized (translated) track by calling getOptimizedTrack() or the
 * optimal translation by calling getOptimalTranslation.
 */
class TrackOptimizer
{
public:

    void setTrack(const Track& track);

    void optimize();

    Track getOptimizedTrack() const;

    void getOptimalTranslation(double translation[3]) const;

private:

    Track track__;

    Track optimizedTrack__;

    double optimalTranslation__[3];
};

#endif

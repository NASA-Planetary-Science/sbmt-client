#ifndef PROPAGATORFIT_H
#define PROPAGATORFIT_H


#include "lidardata.h"
#include "propagator.h"
#include <string>

class PropagatorFit
{
public:
    PropagatorFit();

    typedef enum WhatToEstimate
    {
        ESTIMATE_DENSITY,
        ESTIMATE_PRESSURE,
        ESTIMATE_POSITION,
        ESTIMATE_VELOCITY,
        ESTIMATE_ALL
    } WhatToEstimate;

    typedef enum WhatToOptimizeOver
    {
        OPTIMIZE_DISTANCE_TO_REFERENCE_TRAJECTORY,
        OPTIMIZE_RANGE_ERROR
    } WhatToOptimizeOver;

    void setDensity(double density)
    {
        density__ = density;
    }
    void setPressure(double pressure)
    {
        pressure__ = pressure;
    }
    void setMass(double mass)
    {
        mass__ = mass;
    }
    void setInitialPosition(double pos[3])
    {
        initialPosition__[0] = pos[0];
        initialPosition__[1] = pos[1];
        initialPosition__[2] = pos[2];
        initialPositionProvided__ = true;
    }
    void setInitialVelocity(double vel[3])
    {
        initialVelocity__[0] = vel[0];
        initialVelocity__[1] = vel[1];
        initialVelocity__[2] = vel[2];
        initialVelocityProvided__ = true;
    }
    void setReferenceTrajectory(const LidarTrack& traj)
    {
        referenceTrajectory__ = traj;
        propagator__.setReferenceTrajectory(traj);
    }
    void setShapeModelFilename(const string& filename)
    {
        shapeModelFilename = filename;
        propagator__.setShapeModelFilename(filename);
    }
    void setWhatToEstimate(WhatToEstimate whatToEst)
    {
        whatToEstimate__ = whatToEst;
    }
    void setWhatToOptimizeOver(WhatToOptimizeOver whatToOpt)
    {
        whatToOptimizeOver__ = whatToOpt;
    }


    LidarTrack run();
    double funcLeastSquares(const double *x);


private:

    void doLeastSquares();
    double errorToReferenceTrajectory();
    double rangeError();
    void computeInitialVelocity(double velocity[]);
    void computeInitialPosition(double pos[]);

    double density__;
    double pressure__;
    double mass__;
    double initialPosition__[3];
    double initialVelocity__[3];
    bool initialPositionProvided__;
    bool initialVelocityProvided__;
    LidarTrack referenceTrajectory__;
    LidarTrack optimalTrajectory__;
    WhatToEstimate whatToEstimate__;
    WhatToOptimizeOver whatToOptimizeOver__;
    Propagator propagator__;
    double error__;
    std::string shapeModelFilename;
    bool estimateEverythingCyclicly;
    bool estimateEverythingAtOnce;
};

#endif // PROPAGATORFIT_H

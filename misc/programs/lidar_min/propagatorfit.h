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
        ESTIMATE_POSITION_AND_VELOCITY,
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
    void setShapeModelFilename(const string& vtkfilename, const string& pltfilename)
    {
        vtkShapeModelFilename = vtkfilename;
        pltShapeModelFilename = pltfilename;
        propagator__.setShapeModelFilename(pltfilename);
    }
    void setWhatToEstimate(WhatToEstimate whatToEst)
    {
        whatToEstimate__ = whatToEst;
    }
    void setWhatToOptimizeOver(WhatToOptimizeOver whatToOpt)
    {
        whatToOptimizeOver__ = whatToOpt;
    }
    double getError()
    {
        return error__;
    }
    double getDensity()
    {
        return density__;
    }
    double getPressure()
    {
        return pressure__;
    }
    const double* getInitialPosition()
    {
        return &initialPosition__[0];
    }
    const double* getInitialVelocity()
    {
        return &initialVelocity__[0];
    }


    LidarTrack run();
    double funcLeastSquares(const double *x);

    //! Get the full optimal trajectory with data at time for
    //! for which the reference trajectory has data.
    LidarTrack getFullOptimalTrajectory();

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
    std::string vtkShapeModelFilename;
    std::string pltShapeModelFilename;
    bool spiceKernelsContainVelocity__;
    bool estimateEverythingCyclicly;
    bool estimateEverythingAtOnce;
};

#endif // PROPAGATORFIT_H

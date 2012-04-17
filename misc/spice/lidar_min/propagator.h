#ifndef PROPAGATOR_H
#define PROPAGATOR_H

#include "lidardata.h"

class Propagator
{
public:
    Propagator();

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
    }
    void setInitialVelocity(double vel[3])
    {
        initialVelocity__[0] = vel[0];
        initialVelocity__[1] = vel[1];
        initialVelocity__[2] = vel[2];
    }
    void setReferenceTrajectory(const LidarTrack& traj)
    {
        referenceTrajectory__ = traj;
    }
    void setShapeModelFilename(const string& filename);

    LidarTrack run();

    void totalAcceleration(double t, const double pos[], double acc[]);


private:

    void getSunPosition(double et, double sunpos[]);
    void gravitationAcceleration(double t, const double pos[], double acc[]);
    void solarPressureAcceleration(double t, const double pos[], double acc[]);
    void getInitialState(double initialState[]);
    LidarTrack computePropagatedTrajectory();


    double density__;
    double pressure__;
    double mass__;
    double initialPosition__[3];
    double initialVelocity__[3];
    LidarTrack referenceTrajectory__;
};

#endif // PROPAGATOR_H

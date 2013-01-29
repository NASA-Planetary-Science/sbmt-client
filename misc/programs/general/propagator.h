#ifndef PROPAGATOR_H
#define PROPAGATOR_H

#include "point.h"
#include <string>
#include <gsl/gsl_odeiv2.h>

using namespace std;

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
    void setReferenceTrajectory(const Track& traj)
    {
        referenceTrajectory__ = traj;
    }
    void setShapeModelFilename(const string& filename);

    void setIncrement(int inc)
    {
        increment__ = inc;
    }

    void setStartTime(double startTime)
    {
        startTime__ = startTime;
    }

    void setStopTime(double stopTime)
    {
        stopTime__ = stopTime;
    }

    void setDt(double dt)
    {
        dt__ = dt;
    }

    void setBody(const string& body)
    {
        body__ = body;
        bodyFrame__ = "IAU_" + body__;
    }

    Track run();

    void totalAcceleration(double t, const double pos[], double acc[]);


private:

    void getSunPosition(double et, double sunpos[]);
    void gravitationAcceleration(double t, const double pos[], double acc[]);
    void solarPressureAcceleration(double t, const double pos[], double acc[]);
    void getInitialState(double initialState[]);
    bool isInsideBody(double t, const double pos[3]);
    void findIntersectionPoint(gsl_odeiv2_driver* d, double initTime, const double initState[6], double* t, double y[6]);
    Track computePropagatedTrajectory();
    Track computePropagatedTrajectoryNoReferenceTrajectory();


    double density__;
    double pressure__;
    double mass__;
    double initialPosition__[3];
    double initialVelocity__[3];
    int increment__;
    Track referenceTrajectory__;
    double startTime__;
    double stopTime__;
    double dt__;
    string body__;
    string bodyFrame__;
};

#endif // PROPAGATOR_H

#include <gsl/gsl_errno.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_odeiv2.h>
#include "propagator.h"
#include "SpiceUsr.h"
#include "gravity-werner.h"
//#include "gravity-cheng.h"
//#include "gravity-point.h"
#include <iostream>

const double g_G = 6.67384e-11 * 1.0e-9;

Propagator::Propagator():
    increment__(500)
{
}

void Propagator::setShapeModelFilename(const string &filename)
{
    initializeGravityWerner(filename.c_str());
    //initializeGravityCheng(filename.c_str());
}

Track Propagator::run()
{
    if (referenceTrajectory__.empty())
        return computePropagatedTrajectoryNoReferenceTrajectory();
    else
        return computePropagatedTrajectory();
}

void Propagator::getSunPosition(double et, double sunpos[3])
{
    double lt;
    const char* target = "SUN";
    const char* ref = "J2000";
    const char* abcorr = "LT+S";
    const char* obs = body__.c_str();

    spkpos_c(target, et, ref, abcorr, obs, sunpos, &lt);
    if (failed_c())
        return;

    //cout.precision(16);
    //cout << "Sun position: " << sunpos[0] << " " << sunpos[1] << " " << sunpos[2] << endl;
}

void Propagator::gravitationAcceleration(double t, const double pos[3], double acc[3])
{
    // First convert position to body fixed coordinates
    double newPos[3];
    const char* ref = "J2000";
    const char* frame = bodyFrame__.c_str();
    double i2bmat[3][3];
    pxform_c(ref, frame, t, i2bmat);
    mxv_c(i2bmat, pos, newPos);

    //getGravityCheng(newPos, acc);
    getGravityWerner(newPos, acc);
    acc[0] *= (g_G*density__*1.0e12);
    acc[1] *= (g_G*density__*1.0e12);
    acc[2] *= (g_G*density__*1.0e12);

//    getGravityPoint(newPos, acc);
//    acc[0] *= (g_G*mass__*1.0e12);
//    acc[1] *= (g_G*mass__*1.0e12);
//    acc[2] *= (g_G*mass__*1.0e12);

    // Now transform the acceleration back from body fixed to inertial coordinates
    pxform_c(frame, ref, t, i2bmat);
    mxv_c(i2bmat, acc, acc);
}

void Propagator::solarPressureAcceleration(double t, const double pos[3], double acc[3])
{
    double sunpos[3];
    getSunPosition(t, sunpos);

    double sunToScVec[3] =
    {
        pos[0] - sunpos[0],
        pos[1] - sunpos[1],
        pos[2] - sunpos[2],
    };

    vhat_c(sunToScVec, sunToScVec);

    acc[0] = 1.0e-10 * pressure__ * sunToScVec[0];
    acc[1] = 1.0e-10 * pressure__ * sunToScVec[1];
    acc[2] = 1.0e-10 * pressure__ * sunToScVec[2];
}

void Propagator::totalAcceleration(double t, const double pos[3], double acc[3])
{
    double gravity[3] = {0.0, 0.0, 0.0};
    gravitationAcceleration(t, pos, gravity);

    double solarPressure[3] = {0.0, 0.0, 0.0};
    solarPressureAcceleration(t, pos, solarPressure);

//    cout << "pos " << pos[0] << " " << pos[1] << " " << pos[2] << " " << vnorm_c(solarPressure) << " " << vnorm_c(gravity) << endl;
//    cout << "solar pressure to gravity ratio: " << vnorm_c(solarPressure)/vnorm_c(gravity) << endl;

    acc[0] = gravity[0] + solarPressure[0];
    acc[1] = gravity[1] + solarPressure[1];
    acc[2] = gravity[2] + solarPressure[2];
}

static int func(double t, const double y[], double f[],
                void *params)
{
    Propagator* propagator = (Propagator*)params;

    f[0] = y[3];
    f[1] = y[4];
    f[2] = y[5];

    double acc[3];
    propagator->totalAcceleration(t, y, acc);

    f[3] = acc[0];
    f[4] = acc[1];
    f[5] = acc[2];

    return GSL_SUCCESS;
}

void Propagator::getInitialState(double initialState[6])
{
    initialState[0] = initialPosition__[0];
    initialState[1] = initialPosition__[1];
    initialState[2] = initialPosition__[2];
    initialState[3] = initialVelocity__[0];
    initialState[4] = initialVelocity__[1];
    initialState[5] = initialVelocity__[2];
}

Track Propagator::computePropagatedTrajectory()
{
    // Use GSL's ordinary differential equation solver to
    // propagate the trajectory forward in time
    gsl_odeiv2_system sys = {func, 0, 6, this};
    gsl_odeiv2_driver * d =
            gsl_odeiv2_driver_alloc_y_new (&sys, gsl_odeiv2_step_rkf45,
                                           1e-6, 1e-6, 0.0);


    double y[6];
    getInitialState(y);

    Track propagatedTrajectory;
    propagatedTrajectory.push_back(referenceTrajectory__[0]);
    propagatedTrajectory[0].scpos[0] = y[0];
    propagatedTrajectory[0].scpos[1] = y[1];
    propagatedTrajectory[0].scpos[2] = y[2];
    propagatedTrajectory[0].ancillary1[0] = referenceTrajectory__[0].scpos[0];
    propagatedTrajectory[0].ancillary1[1] = referenceTrajectory__[0].scpos[1];
    propagatedTrajectory[0].ancillary1[2] = referenceTrajectory__[0].scpos[2];

    int size = referenceTrajectory__.size();
    double t = referenceTrajectory__[0].time;
    double tf = referenceTrajectory__[size-1].time;
    for (int i=increment__; i<size; )
    {
        //cout << "Step " << i << " / " << size << endl;
        //printf ("%f %f %f %f %f %f %f\n", t, y[0], y[1], y[2], y[3], y[4], y[5]);
        double ti = referenceTrajectory__[i].time;
        if (ti > tf)
            ti = tf;
        int status = gsl_odeiv2_driver_apply (d, &t, ti, y);
        if (status != GSL_SUCCESS)
        {
            printf ("error, return value=%d\n", status);
            break;
        }
        Point p = referenceTrajectory__[i];

        // Keep the reference position around so we can compute the error to it.
        p.ancillary1[0] = p.scpos[0];
        p.ancillary1[1] = p.scpos[1];
        p.ancillary1[2] = p.scpos[2];

        if(p.time != t)
            cout << "Problem! Times are not equal!" << endl;

        p.time = t;
        p.scpos[0] = y[0];
        p.scpos[1] = y[1];
        p.scpos[2] = y[2];
        propagatedTrajectory.push_back(p);

        // Move forward by several timesteps, otherwise this will take forever
        if (i == size -1)
            break;
        i += increment__;
        if (i >= size)
            i = size -1;
    }
    gsl_odeiv2_driver_free (d);

    return propagatedTrajectory;
}

bool Propagator::isInsideBody(double t, const double pos[3])
{
    // First convert position to body fixed coordinates
    double newPos[3];
    const char* ref = "J2000";
    const char* frame = bodyFrame__.c_str();
    double i2bmat[3][3];
    pxform_c(ref, frame, t, i2bmat);
    mxv_c(i2bmat, pos, newPos);
    return isInsidePolyhedron(newPos);
}

void Propagator::findIntersectionPoint(gsl_odeiv2_driver* d, double initTime, const double initState[6], double* t, double y[6])
{
    double minTime = initTime;
    double maxTime = *t;
    double prevTi = initTime - 1.0;

    for (;;)
    {
        double ti = 0.5*(minTime + maxTime);
        *t = initTime;
        y[0] = initState[0];
        y[1] = initState[1];
        y[2] = initState[2];
        y[3] = initState[3];
        y[4] = initState[4];
        y[5] = initState[5];
        int status = gsl_odeiv2_driver_apply (d, t, ti, y);
        if (status != GSL_SUCCESS)
        {
            printf ("error, return value=%d\n", status);
            break;
        }

        bool isInside = this->isInsideBody(*t, y);
        if (isInside)
        {
            maxTime = *t;
        }
        else
        {
            minTime = *t;
        }

        // If the new time has not changed, we're done.
        if (prevTi == ti)
            break;

        prevTi = ti;
    }
}

Track Propagator::computePropagatedTrajectoryNoReferenceTrajectory()
{
    // Use GSL's ordinary differential equation solver to
    // propagate the trajectory forward in time
    gsl_odeiv2_system sys = {func, 0, 6, this};
    gsl_odeiv2_driver * d =
            gsl_odeiv2_driver_alloc_y_new (&sys, gsl_odeiv2_step_rkf45,
                                           1e-6, 1e-6, 0.0);


    double y[6];
    getInitialState(y);


    Track propagatedTrajectory;
    propagatedTrajectory.push_back(Point());
    propagatedTrajectory[0].time = startTime__;
    propagatedTrajectory[0].scpos[0] = y[0];
    propagatedTrajectory[0].scpos[1] = y[1];
    propagatedTrajectory[0].scpos[2] = y[2];
    propagatedTrajectory[0].ancillary1[0] = y[3];
    propagatedTrajectory[0].ancillary1[1] = y[4];
    propagatedTrajectory[0].ancillary1[2] = y[5];

    bool wasPrevInside = this->isInsideBody(startTime__, y);

    double prevTime = startTime__;
    double prevState[6] = {y[0],y[1],y[2],y[3],y[4],y[5]};

    int size = ceil((stopTime__-startTime__)/dt__);
    double t = startTime__;
    double tf = stopTime__;
    for (int i=1; i<size; ++i)
    {
        double ti = startTime__ + (double)i*dt__;

        cout << "Elapsed time: " << ti << " sec" << endl;

        if (ti > tf)
            break;
        int status = gsl_odeiv2_driver_apply (d, &t, ti, y);
        if (status != GSL_SUCCESS)
        {
            printf ("error, return value=%d\n", status);
            break;
        }

        bool isInside = this->isInsideBody(t, y);
        if (isInside && !wasPrevInside)
        {
            findIntersectionPoint(d, prevTime, prevState, &t, y);
        }

        Point p;

        p.time = t;
        p.scpos[0] = y[0];
        p.scpos[1] = y[1];
        p.scpos[2] = y[2];
        p.ancillary1[0] = y[3];
        p.ancillary1[1] = y[4];
        p.ancillary1[2] = y[5];

        propagatedTrajectory.push_back(p);

        if (isInside && !wasPrevInside)
        {
            break;
        }
        wasPrevInside = isInside;

        prevTime = t;
        prevState[0] = y[0]; prevState[1] = y[1]; prevState[2] = y[2];
        prevState[3] = y[3]; prevState[4] = y[4]; prevState[5] = y[5];
    }
    gsl_odeiv2_driver_free (d);

    return propagatedTrajectory;
}

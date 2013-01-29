#include "propagatorfit.h"
#include "optimize-gsl.h"
#include "optimize.h"
#include "closest-point-vtk.h"
#include <iostream>
#include <math.h>

/* Return distance squared between points x and y */
static double vdist2(const double x[3], const double y[3])
{
    return ( ( x[0] - y[0] ) * ( x[0] - y[0] )
             + ( x[1] - y[1] ) * ( x[1] - y[1] )
             + ( x[2] - y[2] ) * ( x[2] - y[2] ) );
}


PropagatorFit::PropagatorFit()
{
    initialPositionProvided__ = false;
    initialVelocityProvided__ = false;
    whatToOptimizeOver__ = OPTIMIZE_DISTANCE_TO_REFERENCE_TRAJECTORY;
    //whatToOptimizeOver__ = OPTIMIZE_RANGE_ERROR;
    spiceKernelsContainVelocity__ = false;
    estimateEverythingCyclicly = false;
    estimateEverythingAtOnce = false;
}

Track PropagatorFit::run()
{
    initializeVtk(vtkShapeModelFilename.c_str());

    doLeastSquares();

    cout << "final density : " << density__ << endl;
    cout << "final pressure: " << pressure__ << endl;
    cout << "final init position: " << initialPosition__[0] << " " << initialPosition__[1] << " " << initialPosition__[2] << endl;
    cout << "final init velocity: " << initialVelocity__[0] << " " << initialVelocity__[1] << " " << initialVelocity__[2] << endl;

    return optimalTrajectory__;
}

void PropagatorFit::computeInitialPosition(double pos[3])
{
    const Point& p = referenceTrajectory__[0];
    pos[0] = p.scpos[0];
    pos[1] = p.scpos[1];
    pos[2] = p.scpos[2];
    initialPositionProvided__ = true;
}

void PropagatorFit::computeInitialVelocity(double velocity[3])
{
    if (spiceKernelsContainVelocity__)
    {

    }
    else
    {
        // Use the reference trajectory to estimate
        // and initial velocity by taking the average of the
        // first N segments
        unsigned int N = 20;
        if (referenceTrajectory__.size() < N)
            N = 1;

        double meanVelocity[3] = {0.0, 0.0, 0.0};

        for (unsigned int i=1; i<N; ++i)
        {
            const Point& p0 = referenceTrajectory__[i-1];
            const Point& p1 = referenceTrajectory__[i];

            meanVelocity[0] += (p1.scpos[0] - p0.scpos[0]) / (p1.time - p0.time);
            meanVelocity[1] += (p1.scpos[1] - p0.scpos[1]) / (p1.time - p0.time);
            meanVelocity[2] += (p1.scpos[2] - p0.scpos[2]) / (p1.time - p0.time);
        }

        velocity[0] = meanVelocity[0] / (double)N;
        velocity[1] = meanVelocity[1] / (double)N;
        velocity[2] = meanVelocity[2] / (double)N;
    }

    initialVelocityProvided__ = true;
}

static double func(const double* x, void* params)
{
    PropagatorFit* propFit = (PropagatorFit*)params;
    return propFit->funcLeastSquares(x);
}

double PropagatorFit::errorToReferenceTrajectory()
{
    unsigned int size = optimalTrajectory__.size();

    double totalDistance = 0.0;
    for (unsigned int i=0; i<size; ++i)
    {
        double dist = vdist2(optimalTrajectory__[i].scpos, optimalTrajectory__[i].ancillary1);
        totalDistance += dist;
    }

    totalDistance /= (double)size;

    totalDistance = sqrt(totalDistance);

    return totalDistance;
}

double PropagatorFit::rangeError()
{
    double totalDistance = 0.0;
    int found = 0;
    double closestPoint[3];
    unsigned int size = optimalTrajectory__.size();
    int totalNotFound = 0;
    for (unsigned int i=0; i<size; ++i)
    {
        // Convert position to body fixed coordinates. Note that boredir is
        // already in the body-fixed frame.
        double newPos[3];
        const char* ref = "J2000";
        const char* frame = bodyFrame__.c_str();
        double i2bmat[3][3];
        pxform_c(ref, frame, optimalTrajectory__[i].time, i2bmat);
        mxv_c(i2bmat, optimalTrajectory__[i].scpos, newPos);

        // Compute the range to the asteroid from this position
        intersectWithLineVtk(newPos, optimalTrajectory__[i].boredir, closestPoint, &found);

        if (found)
        {
            double range = sqrt(vdist2(newPos, closestPoint));
            totalDistance += fabs(range - optimalTrajectory__[i].range);
        }
        else
        {
            totalNotFound++;
            totalDistance += optimalTrajectory__[i].range;
        }
    }

    cout << "not found/size " << totalNotFound << " / " << size << endl;
    totalDistance /= (double)size;

    totalDistance = sqrt(totalDistance);

    return totalDistance;
}

double PropagatorFit::funcLeastSquares(const double* x/*, void* params*/)
{
    if (whatToEstimate__ == ESTIMATE_DENSITY)
    {
        density__ = *x;
    }
    else if (whatToEstimate__ == ESTIMATE_PRESSURE)
    {
        pressure__ = *x;
    }
    else if (whatToEstimate__ == ESTIMATE_VELOCITY)
    {
        initialVelocity__[0] = x[0] * 1.0e-6;
        initialVelocity__[1] = x[1] * 1.0e-6;
        initialVelocity__[2] = x[2] * 1.0e-6;
    }
    else if (whatToEstimate__ == ESTIMATE_POSITION)
    {
        initialPosition__[0] = x[0];
        initialPosition__[1] = x[1];
        initialPosition__[2] = x[2];
    }
    else if (whatToEstimate__ == ESTIMATE_POSITION_AND_VELOCITY)
    {
        initialPosition__[0] = x[0];
        initialPosition__[1] = x[1];
        initialPosition__[2] = x[2];
        initialVelocity__[0] = x[3] * 1.0e-6;
        initialVelocity__[1] = x[4] * 1.0e-6;
        initialVelocity__[2] = x[5] * 1.0e-6;
    }
    else if (whatToEstimate__ == ESTIMATE_ALL)
    {
        density__            = x[0];
        pressure__           = x[1];
        initialPosition__[0] = x[2];
        initialPosition__[1] = x[3];
        initialPosition__[2] = x[4];
        initialVelocity__[0] = x[5] * 1.0e-6;
        initialVelocity__[1] = x[6] * 1.0e-6;
        initialVelocity__[2] = x[7] * 1.0e-6;
    }


    cout << "current density : " << density__ << endl;
    cout << "current pressure: " << pressure__ << endl;
    cout << "current init position: " << initialPosition__[0] << " " << initialPosition__[1] << " " << initialPosition__[2] << endl;
    cout << "current init velocity: " << initialVelocity__[0] << " " << initialVelocity__[1] << " " << initialVelocity__[2] << endl;

    propagator__.setDensity(density__);
    propagator__.setPressure(pressure__);
    propagator__.setMass(mass__);
    propagator__.setInitialPosition(initialPosition__);
    propagator__.setInitialVelocity(initialVelocity__);
    propagator__.setIncrement(400);
    propagator__.setBody(body__);

    optimalTrajectory__ = propagator__.run();

    if (whatToOptimizeOver__ == OPTIMIZE_DISTANCE_TO_REFERENCE_TRAJECTORY)
        error__ = errorToReferenceTrajectory();
    else
        error__ = rangeError();

    cout << "error : " << error__ << endl;
    return error__;
}

Track PropagatorFit::getFullOptimalTrajectory()
{
    propagator__.setDensity(density__);
    propagator__.setPressure(pressure__);
    propagator__.setMass(mass__);
    propagator__.setInitialPosition(initialPosition__);
    propagator__.setInitialVelocity(initialVelocity__);
    propagator__.setIncrement(1);
    propagator__.setBody(body__);

    Track fullOptTraj = propagator__.run();

    int found = 0;
    int size = fullOptTraj.size();

    // Now intersect each point with the asteroid
    for (int i=0; i<size; ++i)
    {
        // Convert position to body fixed coordinates. Note that boredir is
        // already in the body-fixed frame.
        double newPos[3];
        const char* ref = "J2000";
        const char* frame = bodyFrame__.c_str();
        double i2bmat[3][3];
        pxform_c(ref, frame, fullOptTraj[i].time, i2bmat);
        mxv_c(i2bmat, fullOptTraj[i].scpos, newPos);

        // Compute the range to the asteroid from this position
        intersectWithLineVtk(newPos, fullOptTraj[i].boredir, fullOptTraj[i].intersectpos, &found);

        if (!found)
        {
            fullOptTraj[i].intersectpos[0] = 0.0;
            fullOptTraj[i].intersectpos[1] = 0.0;
            fullOptTraj[i].intersectpos[2] = 0.0;
        }
    }

    return fullOptTraj;
}

void PropagatorFit::doLeastSquares()
{
    int numVar = 1;
    double x[8];

    if (!initialPositionProvided__)
        computeInitialPosition(initialPosition__);
    if (!initialVelocityProvided__)
        computeInitialVelocity(initialVelocity__);

    if (true)
    {
        error__ = 1.0e100;
        WhatToEstimate toEst[] = {
            ESTIMATE_VELOCITY,
            ESTIMATE_POSITION,
            ESTIMATE_VELOCITY,
            ESTIMATE_POSITION,
        };
        WhatToOptimizeOver toOpt[] =
        {
            OPTIMIZE_DISTANCE_TO_REFERENCE_TRAJECTORY,
            OPTIMIZE_DISTANCE_TO_REFERENCE_TRAJECTORY,
            OPTIMIZE_RANGE_ERROR,
            OPTIMIZE_RANGE_ERROR
        };
        for (unsigned int i=0; i<sizeof(toEst)/sizeof(WhatToEstimate); ++i)
        {
            whatToOptimizeOver__ = toOpt[i];
            whatToEstimate__ = toEst[i];
            if (toEst[i] == ESTIMATE_DENSITY)
            {
                x[0] = density__;
                numVar = 1;
            }
            else if (toEst[i] == ESTIMATE_PRESSURE)
            {
                x[0] = pressure__;
                numVar = 1;
            }
            else if (toEst[i] == ESTIMATE_POSITION)
            {
                x[0] = initialPosition__[0];
                x[1] = initialPosition__[1];
                x[2] = initialPosition__[2];
                numVar = 3;
            }
            else if (toEst[i] == ESTIMATE_VELOCITY)
            {
                x[0] = initialVelocity__[0] / 1.0e-6;
                x[1] = initialVelocity__[1] / 1.0e-6;
                x[2] = initialVelocity__[2] / 1.0e-6;
                numVar = 3;
            }
            else if (toEst[i] == ESTIMATE_POSITION_AND_VELOCITY)
            {
                x[0] = initialPosition__[0];
                x[1] = initialPosition__[1];
                x[2] = initialPosition__[2];
                x[3] = initialVelocity__[0] / 1.0e-6;
                x[4] = initialVelocity__[1] / 1.0e-6;
                x[5] = initialVelocity__[2] / 1.0e-6;
                numVar = 6;
            }
            cout << "Beginning new optimization. Estimating: " << toEst[i] << " " << toOpt[i] << endl;
            //optimizeGsl(func, 0, x, numVar, this);
            optimizeLbfgs(func, 0, x, numVar, this);
        }
    }
    /*
    else if (estimateEverythingCyclicly)
    {
        error__ = 1.0e100;
        double prevError;
        WhatToEstimate toEst[] = {
            ESTIMATE_VELOCITY,
            ESTIMATE_POSITION,
            //ESTIMATE_DENSITY,
            //ESTIMATE_PRESSURE
        };
        int numCycles = 1;
        while (true)
        {
            prevError = error__;
            for (unsigned int i=0; i<sizeof(toEst)/sizeof(WhatToEstimate); ++i)
            {
                if (toEst[i] == ESTIMATE_DENSITY)
                {
                    x[0] = density__;
                    numVar = 1;
                    whatToEstimate__ = ESTIMATE_DENSITY;
                }
                else if (toEst[i] == ESTIMATE_PRESSURE)
                {
                    x[0] = pressure__;
                    numVar = 1;
                    whatToEstimate__ = ESTIMATE_PRESSURE;
                }
                else if (toEst[i] == ESTIMATE_POSITION)
                {
                    x[0] = initialPosition__[0];
                    x[1] = initialPosition__[1];
                    x[2] = initialPosition__[2];
                    numVar = 3;
                    whatToEstimate__ = ESTIMATE_POSITION;
                }
                else if (toEst[i] == ESTIMATE_VELOCITY)
                {
                    x[0] = initialVelocity__[0] / 1.0e-6;
                    x[1] = initialVelocity__[1] / 1.0e-6;
                    x[2] = initialVelocity__[2] / 1.0e-6;
                    numVar = 3;
                    whatToEstimate__ = ESTIMATE_VELOCITY;
                }
                cout << "Beginning new optimization. Estimating: " << toEst[i] << endl;
                //optimizeGsl(func, 0, x, numVar, this);
                optimizeLbfgs(func, 0, x, numVar, this);
            }
            if (error__ >= prevError || numCycles >= 1) // no error reduction, so terminate
                return;
            ++numCycles;
        }
    }
    else if (estimateEverythingAtOnce)
    {
        whatToEstimate__ = ESTIMATE_ALL;

        x[0] = density__;
        x[1] = pressure__;
        x[2] = initialPosition__[0];
        x[3] = initialPosition__[1];
        x[4] = initialPosition__[2];
        x[5] = initialVelocity__[0] / 1.0e-6;
        x[6] = initialVelocity__[1] / 1.0e-6;
        x[7] = initialVelocity__[2] / 1.0e-6;
        numVar = 8;

        optimizeGsl(func, 0, x, numVar, this);
        //optimizeLbfgs(func, 0, x, numVar, this);
    }
    else
    {
        if (whatToEstimate__ == ESTIMATE_DENSITY)
        {
            x[0] = density__;
            numVar = 1;
        }
        else if (whatToEstimate__ == ESTIMATE_PRESSURE)
        {
            x[0] = pressure__;
            numVar = 1;
        }
        else if (whatToEstimate__ == ESTIMATE_POSITION)
        {
            x[0] = initialPosition__[0];
            x[1] = initialPosition__[1];
            x[2] = initialPosition__[2];
            numVar = 3;
        }
        else if (whatToEstimate__ == ESTIMATE_VELOCITY)
        {
            x[0] = initialVelocity__[0] / 1.0e-6;
            x[1] = initialVelocity__[1] / 1.0e-6;
            x[2] = initialVelocity__[2] / 1.0e-6;
            numVar = 3;
        }

        //optimizeGsl(func, 0, x, numVar, this);
        optimizeLbfgs(func, 0, x, numVar, this);
    }
    */
}

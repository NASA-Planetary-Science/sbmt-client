#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include <vector>
#include <cmath>
#include <string.h>
#include <string>
#include "SpiceUsr.h"
#include "util.h"
#include "propagatorfit.h"
#include "lidardata.h"


using namespace std;


/**
   This program takes a shape model, a reference trajectory, and an
   initial state at a specified time and attempts to compute the
   density of the asteroid using the following least squares procedure:

   The state is propagated forward in time based on the gravitational
   force of the shape model using an initial guess for the density.
   It then computes the error between the propagated trajectory and
   the reference trajectory. The using, a least squares approach, this
   procedure is iterated until the optimal density is found that
   results in the minimum error between the propagated trajectory and
   the reference trajectory.

   The optimal propagated trajectory is then saved out to a file.

 */
int main(int argc, char** argv)
{
    if (argc < 8)
    {
        printf("Usage (one of the following):\n\n");
        printf("  gravity [-ed|-ep|-ev] -d <density> -p <pressure> -v<vx>,<vy>,<vz> -b <body> -s <vtkfile> -k <kernelfiles> -i <trajectoryfile> -o <outputfile>\n");
        printf("where:\n\n");
        printf("  -ed means estimate density and -ep means estimate pressure\n");
        printf("  <density> is the density value to use if -ep is set or the initial value of density if -ed is set\n");
        printf("  <pressure> is the pressure value to use if -ed is set or the initial value of pressure if -ep is set\n");
        printf("  <body> is either EROS or ITOKAWA\n");
        printf("  <vtkfile> path to shape model\n");
        printf("  <kernelfile> path to metakernel file\n");
        printf("  <trajectoryfile> path to reference trajectory file\n");
        printf("  <outputfile> optimized trajectory file in body fixed coordinates. First column is time and next 3 columns are position.\n");
        return 1;
    }

    char* body = 0;
    char* dskfile = 0;
    const char* kernelfiles = 0;
    string trajectoryfile;
    string outfile;
    double density = 1.0;
    double pressure = 1.0;
    double initialVelocity[3] = {0.0, 0.0, 0.0};
    double initialPosition[3] = {0.0, 0.0, 0.0};
    bool initialVelocityProvided = false;
    bool initialPositionProvided = false;
    PropagatorFit::WhatToEstimate whatToEstimate = PropagatorFit::ESTIMATE_DENSITY;
    bool optimizeRange = false;
    BodyType bodyType;
    double startTime = 0.0;
    double stopTime = 0.0;
    for(int i = 1; i<argc; ++i)
    {
        if (!strcmp(argv[i], "-ed"))
        {
            whatToEstimate = PropagatorFit::ESTIMATE_DENSITY;
        }
        else if (!strcmp(argv[i], "-ep"))
        {
            whatToEstimate = PropagatorFit::ESTIMATE_PRESSURE;
        }
        else if (!strcmp(argv[i], "-epo"))
        {
            whatToEstimate = PropagatorFit::ESTIMATE_POSITION;
        }
        else if (!strcmp(argv[i], "-ev"))
        {
            whatToEstimate = PropagatorFit::ESTIMATE_VELOCITY;
        }
        else if (!strcmp(argv[i], "-d"))
        {
            density = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "-p"))
        {
            pressure = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "-po"))
        {
            string values = argv[++i];
            vector<string> vel = split(values, ",");
            initialPosition[0] = atof(vel[0].c_str());
            initialPosition[1] = atof(vel[1].c_str());
            initialPosition[2] = atof(vel[2].c_str());
            initialPositionProvided = true;
        }
        else if (!strcmp(argv[i], "-v"))
        {
            string values = argv[++i];
            vector<string> vel = split(values, ",");
            initialVelocity[0] = atof(vel[0].c_str());
            initialVelocity[1] = atof(vel[1].c_str());
            initialVelocity[2] = atof(vel[2].c_str());
            initialVelocityProvided = true;
        }
        else if (!strcmp(argv[i], "-start"))
        {
            startTime = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "-stop"))
        {
            stopTime = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "-r"))
        {
            optimizeRange = true;
        }
        else if (!strcmp(argv[i], "-b"))
        {
            body = argv[++i];
        }
        else if (!strcmp(argv[i], "-s"))
        {
            dskfile = argv[++i];
        }
        else if (!strcmp(argv[i], "-k"))
        {
            kernelfiles = argv[++i];
        }
        else if (!strcmp(argv[i], "-i"))
        {
            trajectoryfile = argv[++i];
        }
        else if (!strcmp(argv[i], "-o"))
        {
            outfile = argv[++i];
        }
    }

    bodyType = EROS;
    if (!strcmp(body, "ITOKAWA"))
        bodyType = ITOKAWA;

    furnsh_c(kernelfiles);

    LidarTrack referenceTrajectory = LidarData::loadTrack(trajectoryfile, bodyType, true, startTime, stopTime);

    PropagatorFit propFit;
    propFit.setDensity(density);
    propFit.setPressure(pressure);
    if (initialPositionProvided)
        propFit.setInitialPosition(initialPosition);
    if (initialVelocityProvided)
        propFit.setInitialVelocity(initialVelocity);
    propFit.setReferenceTrajectory(referenceTrajectory);
    propFit.setWhatToEstimate(whatToEstimate);
    propFit.setShapeModelFilename(dskfile);

    LidarTrack optimalTrack = propFit.run();

    LidarData::saveTrack(outfile, optimalTrack, bodyType, true);
    LidarData::saveTrack(outfile+"-ref", referenceTrajectory, bodyType, true);
}

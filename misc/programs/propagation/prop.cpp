#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include <vector>
#include <cmath>
#include <fstream>
#include <string.h>
#include <string>
#include <algorithm>
#include "SpiceUsr.h"
#include "util.h"
#include "propagator.h"
#include "point.h"
#include "gravity-werner.h"


using namespace std;


/*
   This program takes a shape model, an initial position and
   initial velocity at a specified time and computes the trajectory
   of an object with these initial conditions by propagating the object
   forward in time by integrating the equations of motion.

   The state is propagated forward in time based on the gravitational
   force of the shape model using the supplied densitiy.

   The propagated trajectory is saved out to a file.

*/

static void saveTrajectory(const string &filename,
                           const Track& track,
                           string body,
                           double density)
{
    ofstream fout(filename.c_str());

    if (fout.is_open())
    {
        fout.precision(16);

        string bodyFrame = "IAU_" + body;

        for (unsigned int i = 0; i<track.size(); ++i)
        {
            const Point& p = track[i];

            char strTime[32];
            et2utc_c(p.time, "ISOC", 3, 32, strTime);

            // transform to body fixed
            double y[6] = {p.scpos[0], p.scpos[1], p.scpos[2], p.ancillary1[0], p.ancillary1[1], p.ancillary1[2]};
            const char* ref = "J2000";
            const char* frame = bodyFrame.c_str();
            double i2bmat[6][6];
            sxform_c(ref, frame, p.time, i2bmat);
            mxvg_c(i2bmat, y, 6, 6, y);

            // Compute acceleration, potential and elevation
            double acc[3];
            const double G = 6.67384e-11 * 1.0e-9;
            double potential = 1.0e6*1.0e12*G*density*getGravityWerner(y, acc);

            acc[0] *= 1.0e3 * 1.0e12 * G * density;
            acc[1] *= 1.0e3 * 1.0e12 * G * density;
            acc[2] *= 1.0e3 * 1.0e12 * G * density;

            fout << strTime << " "
                 << y[0] << " "
                 << y[1] << " "
                 << y[2] << " "
                 << y[3] << " "
                 << y[4] << " "
                 << y[5] << " "
                 << acc[0] << " "
                 << acc[1] << " "
                 << acc[2] << " "
                 << potential << "\n";
        }
    }

    fout.close();
}

static void usage()
{
    printf("Usage:\n\n");
    printf("  prop -b <body> -s <pltfile> -k <kernelfiles> -d <density> -ip <px>,<py>,<pz> -iv <vx>,<vy>,<vz> -o <outputfile>\n\n");
    printf("where:\n\n");
    printf("  -d <density> is the density of the body (in g/cm^3). Default: 1.0\n");
    printf("  -ip <px>,<py>,<pz> is the initial position in body fixed coordinates. Default: 0.0,0.0,0.0\n");
    printf("  -iv <vx>,<vy>,<vz> is the initial velocity in body fixed coordinates. Default: 0.0,0.0,0.0\n");
    printf("  -b <body> is a supported body such as EROS, ITOKAWA, PHOBOS, or DEIMOS. Default: none\n");
    printf("  -t <max-time> max time in seconds to propagate forward to. Must be positive. Default: 100.0\n");
    printf("  -dt <interval> time step in seconds. Default: 1.0\n");
    printf("  -s <pltfile> path to shape model in PLT format. Default: none\n");
    printf("  -k <kernelfile> path to SPICE metakernel file. Default: none\n");
    printf("  -o <outputfile> output trajectory file in body fixed coordinates. Default: output.txt\n");
    exit(1);
}

int main(int argc, char** argv)
{
    if (argc < 8)
    {
        usage();
    }

    string body;
    const char* pltfile = 0;
    const char* kernelfiles = 0;
    string outfile = "output.txt";
    double density = 1.0;
    double pressure = 0.0;
    double initialVelocity[3] = {0.0, 0.0, 0.0};
    double initialPosition[3] = {0.0, 0.0, 0.0};
    double maxTime = 100.0;
    double dt = 1.0;
    for(int i = 1; i<argc; ++i)
    {
        if (!strcmp(argv[i], "-d"))
        {
            density = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "-p"))
        {
            pressure = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "-ip"))
        {
            string values = argv[++i];
            vector<string> vel = split(values, ",");
            initialPosition[0] = atof(vel[0].c_str());
            initialPosition[1] = atof(vel[1].c_str());
            initialPosition[2] = atof(vel[2].c_str());
        }
        else if (!strcmp(argv[i], "-iv"))
        {
            string values = argv[++i];
            vector<string> vel = split(values, ",");
            initialVelocity[0] = atof(vel[0].c_str());
            initialVelocity[1] = atof(vel[1].c_str());
            initialVelocity[2] = atof(vel[2].c_str());
        }
        else if (!strcmp(argv[i], "-t"))
        {
            maxTime = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "-dt"))
        {
            dt = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "-b"))
        {
            body = argv[++i];
            // convert body to upper case
            std::transform(body.begin(), body.end(), body.begin(), ::toupper);
        }
        else if (!strcmp(argv[i], "-s"))
        {
            pltfile = argv[++i];
        }
        else if (!strcmp(argv[i], "-k"))
        {
            kernelfiles = argv[++i];
        }
        else if (!strcmp(argv[i], "-o"))
        {
            outfile = argv[++i];
        }
        else
        {
            usage();
        }
    }

    if (pltfile == 0 || kernelfiles == 0 || body.empty())
    {
        usage();
    }

    furnsh_c(kernelfiles);

    // convert initial state to J2000
    const char* ref = "J2000";
    const char* frame = string("IAU_" + body).c_str();
    double i2bmat[6][6];
    sxform_c(frame, ref, 0.0, i2bmat);
    double y[6] = {initialPosition[0], initialPosition[1], initialPosition[2],
                   initialVelocity[0], initialVelocity[1], initialVelocity[2]};
    mxvg_c(i2bmat, y, 6, 6, y);


    Propagator propagator;
    propagator.setDensity(density);
    propagator.setPressure(pressure);
    propagator.setInitialPosition(&y[0]);
    propagator.setInitialVelocity(&y[3]);
    propagator.setStartTime(0.0);
    propagator.setStopTime(maxTime);
    propagator.setDt(dt);
    propagator.setShapeModelFilename(pltfile);
    propagator.setBody(body.c_str());

    Track optimalTrack = propagator.run();

    saveTrajectory(outfile, optimalTrack, body, density);

    return 0;
}

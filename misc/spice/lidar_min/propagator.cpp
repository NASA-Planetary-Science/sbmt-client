#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include <fstream>
#include <vector>
#include <cmath>
#include <string.h>
#include <string>
#include <gsl/gsl_errno.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_odeiv2.h>
#include "SpiceUsr.h"
#include "util.h"
#include "optimize-gsl.h"

using namespace std;

struct Point2
{
    double time;
    double pos[3];
    Point2() {}
    Point2(double t, double p[3]):
        time(t)
    {
        pos[0] = p[0];
        pos[1] = p[1];
        pos[2] = p[2];
    }
};

typedef enum WhatToEstimate
{
    ESTIMATE_DENSITY,
    ESTIMATE_PRESSURE
} WhatToEstimate;

typedef enum BodyType
{
    ITOKAWA,
    EROS
} BodyType;

vector<Point2> g_referenceTrajectory;
const double g_G = 6.67384e-11 * 1.0e-9;
WhatToEstimate g_whatToEstimate;
BodyType g_bodyType;
double g_volume;
double g_density;
double g_pressure;


/* Return distance squared between points x and y */
double vdist2(const double x[3], const double y[3])
{
    return ( ( x[0] - y[0] ) * ( x[0] - y[0] )
             + ( x[1] - y[1] ) * ( x[1] - y[1] )
             + ( x[2] - y[2] ) * ( x[2] - y[2] ) );
}

void getSunPosition(double et, double sunpos[3])
{
    double lt;
    const char* target = "SUN";
    const char* ref = "IAU_VESTA";
    const char* abcorr = "LT+S";
    const char* obs = "VESTA";

    spkpos_c(target, et, ref, abcorr, obs, sunpos, &lt);
    if (failed_c())
        return;

    cout.precision(16);
    cout << "Sun position: " << sunpos[0] << " " << sunpos[1] << " " << sunpos[2] << endl;
}

void pointParticleGravitationalAcceleration(double t, const double pos[3], double acc[3])
{
    double mass = g_density * g_volume;

    // Get the position of the center of the asteroid in J2000
    double center[3];

    double scToBodyVec[3] =
    {
        center[0] - pos[0],
        center[1] - pos[1],
        center[2] - pos[2]
    };

    double r = 0.0;
    unorm_c(scToBodyVec, scToBodyVec, &r);

    acc[0] = scToBodyVec[0] * g_G * mass / (r*r);
    acc[1] = scToBodyVec[1] * g_G * mass / (r*r);
    acc[2] = scToBodyVec[2] * g_G * mass / (r*r);
}

void gravitationAcceleration(double t, const double pos[3], double acc[3])
{

}

void solarPressureAcceleration(double t, const double pos[3], double acc[3])
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

    acc[0] = g_pressure * sunToScVec[0];
    acc[1] = g_pressure * sunToScVec[1];
    acc[2] = g_pressure * sunToScVec[2];
}

void totalAcceleration(double t, const double pos[3], double acc[3])
{
    double gravity[3];
    gravitationAcceleration(t, pos, gravity);

    double solarPressure[3];
    solarPressureAcceleration(t, pos, solarPressure);

    acc[0] = gravity[0] + solarPressure[0];
    acc[1] = gravity[1] + solarPressure[1];
    acc[2] = gravity[2] + solarPressure[2];
}

int func (double t, const double y[], double f[],
          void *params)
{
    f[0] = y[3];
    f[1] = y[4];
    f[2] = y[5];

    double acc[3];
    totalAcceleration(t, y, acc);

    f[3] = acc[0];
    f[4] = acc[1];
    f[5] = acc[2];

    return GSL_SUCCESS;
}

/*
int jac (double t, const double y[], double *dfdy,
         double dfdt[], void *params)
{
    double mu = *(double *)params;
    gsl_matrix_view dfdy_mat
            = gsl_matrix_view_array (dfdy, 2, 2);
    gsl_matrix * m = &dfdy_mat.matrix;
    gsl_matrix_set (m, 0, 0, 0.0);
    gsl_matrix_set (m, 0, 1, 1.0);
    gsl_matrix_set (m, 1, 0, -2.0*mu*y[0]*y[1] - 1.0);
    gsl_matrix_set (m, 1, 1, -mu*(y[0]*y[0] - 1.0));
    dfdt[0] = 0.0;
    dfdt[1] = 0.0;
    return GSL_SUCCESS;
}
*/

void getInitialState(double initialState[6])
{

}

vector<Point2> computePropagatedTrajectory()
{
    // Use GSL's ordinary differential equation solver to
    // propagate the trajectory forward in time
    gsl_odeiv2_system sys = {func, 0, 6, 0};
    gsl_odeiv2_driver * d =
            gsl_odeiv2_driver_alloc_y_new (&sys, gsl_odeiv2_step_rk8pd,
                                           1e-6, 1e-6, 0.0);


    double y[6];
    getInitialState(y);

    vector<Point2> propagatedTrajectory;
    propagatedTrajectory.push_back(g_referenceTrajectory[0]);

    int size = g_referenceTrajectory.size();
    double t = g_referenceTrajectory[0].time;
    double tf = g_referenceTrajectory[size-1].time;
    while (t < tf)
    {
        double ti = t + 1.0;
        if (ti > tf)
            ti = tf;
        int status = gsl_odeiv2_driver_apply (d, &t, ti, y);
        if (status != GSL_SUCCESS)
        {
            printf ("error, return value=%d\n", status);
            break;
        }
        propagatedTrajectory.push_back(Point2(t, y));
        //printf ("%.5e %.5e %.5e\n", t0, y[0], y[1]);
    }
    gsl_odeiv2_driver_free (d);

    return propagatedTrajectory;
}

double funcLeastSquares(const double* x, void* params)
{
    if (g_whatToEstimate == ESTIMATE_DENSITY)
        g_density = *x;
    else if (g_whatToEstimate == ESTIMATE_PRESSURE)
        g_pressure = *x;

    vector<Point2> propagatedTrajectory =
        computePropagatedTrajectory();

    unsigned int size = propagatedTrajectory.size();
    if (size != g_referenceTrajectory.size())
    {
        cout << "Major error!" << endl;
        abort();
    }

    double totalDistance = 0.0;
    for (unsigned int i=0; i<size; ++i)
    {
        double dist = vdist2(propagatedTrajectory[i].pos, g_referenceTrajectory[i].pos);
        dist = sqrt(dist);
        totalDistance += dist;
    }

    totalDistance /= (double)size;

    return totalDistance;
}

double doLeastSquares()
{
    double x;
    if (g_whatToEstimate == ESTIMATE_DENSITY)
        x = g_density;
    else if (g_whatToEstimate == ESTIMATE_PRESSURE)
        x = g_pressure;

    optimizeGsl(funcLeastSquares, 0, &x, 1, 0);
    return x;
}

void loadReferenceTrajectory(const string& filename)
{
    ifstream fin(filename.c_str());

    g_referenceTrajectory.clear();

    if (fin.is_open())
    {
        string line;
        while (getline(fin, line))
        {
            vector<string> tokens = split(line);
            Point2 p;
            p.time = atof(tokens[0].c_str());
            p.pos[0] = atof(tokens[1].c_str());
            p.pos[1] = atof(tokens[2].c_str());
            p.pos[2] = atof(tokens[3].c_str());

            // transform to J2000
            g_referenceTrajectory.push_back(p);
        }
    }
    else
    {
        cerr << "Error: Unable to open file '" << filename << "'" << endl;
        exit(1);
    }
}


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
        printf("Usage:\n");
        printf("  gravity -d <density> <body> <vtkfile> <kernelfiles> <trajectoryfile> <outputfile>\n");
        printf("  gravity -p <pressure> <body> <vtkfile> <kernelfiles> <trajectoryfile> <outputfile>\n");
        printf("where:\n");
        printf("<body> is either EROS or ITOKAWA\n");
        printf("<vtkfile> path to shape model\n");
        printf("<kernelfile> path to metakernel file\n");
        printf("<trajectoryfile> path to reference trajectory file\n");
        printf("<density>\n");
        printf("\n");
        printf("\n");
        printf("\n");
        printf("\n");
        printf("\n");
        return 1;
    }


    if (!strcmp(argv[1], "-d"))
        g_whatToEstimate = ESTIMATE_DENSITY;
    else if (!strcmp(argv[1], "-p"))
        g_whatToEstimate = ESTIMATE_PRESSURE;

    g_density = atof(argv[2]);
    g_pressure = atof(argv[3]);
    char* body = argv[3];
    char* dskfile = argv[4];
    const char* kernelfiles = argv[5];
    string trajectoryfile = argv[6];
    const char* outfile = argv[7];

    g_bodyType = EROS;
    if (!strcmp(body, "ITOKAWA"))
        g_bodyType = ITOKAWA;

//    initializeVtk(dskfile);

    furnsh_c(kernelfiles);

    loadReferenceTrajectory(trajectoryfile);
    double value = doLeastSquares();
    cout << "optimal value: " << value << endl;
}

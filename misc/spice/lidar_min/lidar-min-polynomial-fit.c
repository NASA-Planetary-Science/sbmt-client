#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include "SpiceUsr.h"
#include "SpiceDLA.h"
#include "SpiceDSK.h"
#include "lbfgs.h"
#include "optimize.h"


/************************************************************************
* Enumeration listing available solvers
************************************************************************/
typedef enum SolverType
{
    LIBLBFGS,
    GSL,
    NLOPT,
    NR
} SolverType;


/************************************************************************
* Constants
************************************************************************/
#define NUMBER_POINTS 1114386
#define START_POINT 0
#define STOP_POINT 36
#define PATH_SIZE 256
#define LINE_SIZE 1024
#define UTC_SIZE 128
#define MAX_TRACK_SIZE 1000
#define TRACK_BREAK_THRESHOLD 60
#define DX 0.00001
#define NUMBER_FILES 3
#define POLYNOMIAL_ORDER 3
#define PENALTY_WEIGHT 1.0
const char Tabfiles[NUMBER_FILES][PATH_SIZE] =
{
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_f_20050911_20050930.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_f_20051001_20051031.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_f_20051101_20051118.tab"
};
const char Outfiles[NUMBER_FILES][PATH_SIZE] =
{
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized_20050911_20050930.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized_20051001_20051031.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized_20051101_20051118.tab"
};
const char* const kernelfiles = "/project/nearsdc/spice-kernels/hayabusa/kernels.txt";
const char* const dskfile = "/project/nearsdc/data/ITOKAWA/quad512q.bds";


/************************************************************************
* Structure for storing a lidar point
************************************************************************/
struct Point
{
    double time;
    double scpos[3];
    double targetpos[3];
    double boredir[3];
};


/************************************************************************
* Global varaiables
************************************************************************/

/* Array for storing all lidar points */
struct Point g_points[NUMBER_POINTS];

struct Point g_pointsOptimized[NUMBER_POINTS];

int g_numberOptimizationsPerPoint[NUMBER_POINTS];

/* The first point within the points variable to be optimized */
int g_trackStartPoint;

/* The number of points within */
int g_trackSize;

/* Used to perform ray intersection with shape model */
SpiceDLADescr g_dladsc;

/* Used to perform ray intersection with shape model */
SpiceInt g_handle;


void printPoint(int i)
{
    printf("point %d: %f %f %f %f %f %f %f \n",
           i,
           g_points[i].time,
           g_points[i].scpos[0],
           g_points[i].scpos[1],
           g_points[i].scpos[2],
           g_points[i].targetpos[0],
           g_points[i].targetpos[1],
           g_points[i].targetpos[2]
        );
}


void printpt(const char* str, const double* pt)
{
    printf("%s - %f %f %f\n", str, pt[0], pt[1], pt[2]);
}
        

/************************************************************************
* Function which loads points from "tab" files into points
* global variable
************************************************************************/
void loadPoints()
{
    printf("Loading data\n");
    int i;
    int count = 0;
    for (i=0; i<NUMBER_FILES; ++i)
    {
        const char* filename = Tabfiles[i];
        FILE *f = fopen(filename, "r");
        if (f == NULL)
        {
            printf("Could not open %s", filename);
            exit(1);
        }

        char line[LINE_SIZE];
        char utc[UTC_SIZE];
        double time;
        double sx;
        double sy;
        double sz;
        double x;
        double y;
        double z;
        
        while ( fgets ( line, sizeof line, f ) != NULL ) /* read a line */
        {
            sscanf(line, "%*s %s %*s %lf %lf %lf %lf %lf %lf", utc, &sx, &sy, &sz, &x, &y, &z);

            utc2et_c(utc, &time);

            struct Point point;
            point.time = time;
            point.scpos[0] = sx;
            point.scpos[1] = sy;
            point.scpos[2] = sz;
            point.targetpos[0] = x;
            point.targetpos[1] = y;
            point.targetpos[2] = z;
            point.boredir[0] = x - sx;
            point.boredir[1] = y - sy;
            point.boredir[2] = z - sz;
            
            g_points[count] = point;

            ++count;
        }

        printf("points read %d\n", count);
        fflush(NULL);
        fclose ( f );
    }
    printf("Finished loading data\n\n\n");
}


/************************************************************************
* Loads the dsk shape model
************************************************************************/
void loadDsk(const char* dskfile)
{
    SpiceBoolean found;
    dasopr_c ( dskfile, &g_handle );
    dlabfs_c ( g_handle, &g_dladsc, &found );
    if ( !found  )
    {
        setmsg_c ( "No segments found in DSK file #.");
        errch_c  ( "#",  dskfile                     );
        sigerr_c ( "SPICE(NODATA)"                   );
        exit(1);
    }
}


/************************************************************************
* Evaluate polynomial q at time t and put result in result
************************************************************************/
void evaluatePolynomial(double t, const double* q, double result[3])
{
    if (POLYNOMIAL_ORDER == 3)
    {
        result[0] = q[0] + q[1]*t + q[2]*t*t;
        result[1] = q[3] + q[4]*t + q[5]*t*t;
        result[2] = q[6] + q[7]*t + q[8]*t*t;
    }
    else if (POLYNOMIAL_ORDER == 4)
    {
        result[0] = q[0] + q[1]*t + q[2]*t*t  + q[3]*t*t*t;
        result[1] = q[4] + q[5]*t + q[6]*t*t  + q[7]*t*t*t;
        result[2] = q[8] + q[9]*t + q[10]*t*t + q[11]*t*t*t;
    }
}


/************************************************************************
* Before doing the optimization we need to initialize it. This is
* done by doing a least squares fit to the spacecraft trajectory
* ignoring lidar data. However, we still need to initialize this
* initialization lease squares. This is what the following function
* does by connecting a straight line to the first and last points
* of the track. The coefficients q are initialized with these lines.
* Only the first 2 coefficients of the each coordinate polynomial is
* used and the the others are zero since this is second order polynomial.
************************************************************************/
void initializeInitializationLeastSquares(double* q, int startPoint, int trackSize)
{
    int endPoint = startPoint + trackSize;
    struct Point pt0 = g_points[startPoint];
    struct Point pt1 = g_points[endPoint];

    double vec[3] = {
        pt1.scpos[0] - pt0.scpos[0],
        pt1.scpos[1] - pt0.scpos[1],
        pt1.scpos[2] - pt0.scpos[2]
    };

    int i;
    for (i = 0;i < 3*POLYNOMIAL_ORDER;++i)
        q[i] = 0.0;

    if (POLYNOMIAL_ORDER == 3)
    {
        q[0] = pt0.scpos[0];
        q[1] = vec[0];
        q[3] = pt0.scpos[1];
        q[4] = vec[1];
        q[6] = pt0.scpos[2];
        q[7] = vec[2];
    }
    else if (POLYNOMIAL_ORDER == 4)
    {
        q[0] = pt0.scpos[0];
        q[1] = vec[0];
        q[4] = pt0.scpos[1];
        q[5] = vec[1];
        q[8] = pt0.scpos[2];
        q[9] = vec[2];
    }
}


/************************************************************************
* Function used for initializing the optimization. Simply fits a polynomial
* to the spacecraft trajectory ignoring lidar data.
************************************************************************/
double funcForInitialization(const double* q)
{
    int i;
    int endPoint = g_trackStartPoint + g_trackSize;
    double startTime = g_points[g_trackStartPoint].time;
    double ssd = 0.0;
    for (i=g_trackStartPoint; i<endPoint; ++i)
    {
        struct Point pt = g_points[i];

        double t = pt.time - startTime;

        /* Evaluate x polynomial */
        double scpos[3];
        evaluatePolynomial(t, q, scpos);
        
        double dist = vdist_c(scpos, pt.scpos);
        ssd += dist*dist;
    }

    return ssd;
}


/************************************************************************
* The main objective function to be optimized.
* The independent variables are a 3*POLYNOMIAL_ORDER element vector
* representing the coefficients of polynomials fitting the x, y, and z coordinates
************************************************************************/
double func(const double* q)
{
    int plid;
    SpiceBoolean found;
    int i;
    int endPoint = g_trackStartPoint + g_trackSize;
    double startTime = g_points[g_trackStartPoint].time;
    double ssd = 0.0;
    double penalty = 0.0;
    for (i=g_trackStartPoint; i<endPoint; ++i)
    {
        struct Point pt = g_points[i];

        double t = pt.time - startTime;

        /* Evaluate x polynomial */
        double scpos[3];
        evaluatePolynomial(t, q, scpos);
        
        
        double targetShifted[3];
        vadd_c(scpos, pt.boredir, targetShifted);

        /* Shoot this ray into shape model */
        double xpt[3];
        dskx02_c ( g_handle, &g_dladsc, scpos, pt.boredir,
                   &plid,  xpt,     &found          );

        if (found)
        {
            double dist = vdist_c(xpt, targetShifted);
            ssd += dist*dist;
        }
        else
        {
            /* If did not intersect asteroid then compute distance between
               target point and origin */
            double dist = vnorm_c(targetShifted);
            ssd += dist*dist;
        }


        /* added a smoothing term that penalizes unrealistic spacecraft trajectories.
         * This term consists of several parts:
         * 1. compute distance of polynomial to original point. If too large, penalize
         * 2. compute curvature of point
         */

        double dist = vdist_c(scpos, pt.scpos);
        penalty += dist * dist;

        double vx, ax, vy, ay, vz, az;
        if (POLYNOMIAL_ORDER == 3)
        {
            vx = q[1] + 2.0*q[2]*t;
            vy = q[4] + 2.0*q[5]*t;
            vz = q[7] + 2.0*q[8]*t;
            ax = 2.0*q[2];
            ay = 2.0*q[5];
            az = 2.0*q[8];
        }
        else if (POLYNOMIAL_ORDER == 4)
        {
            vx = q[1] + 2.0*q[2]*t  + 3.0*q[3]*t*t;
            vy = q[5] + 2.0*q[6]*t  + 3.0*q[7]*t*t;
            vz = q[9] + 2.0*q[10]*t + 3.0*q[11]*t*t;
            ax = 2.0*q[2]  + 6.0*q[3]*t;
            ay = 2.0*q[6]  + 6.0*q[7]*t;
            az = 2.0*q[10] + 6.0*q[11]*t;
        }
        double f1 = az*vy - ay*vz;
        double f2 = ax*vz - az*vx;
        double f3 = ay*vx - ax*vy;
        double v = vx*vx + vy*vy + vz*vz;
        if (v > 0.0)
        {
            double curvature = sqrt(f1*f1 + f2*f2 + f3*f3) / pow(v, 1.5);
            penalty += curvature;
        }
    }

    return ssd + PENALTY_WEIGHT*penalty;
}


/************************************************************************
* Does the optimization of a single track (Polynomial)
************************************************************************/
void optimizeTrack(int startId, int trackSize, SolverType solverType)
{
    printf("Optimizing track starting at %d with size %d\n\n", startId, trackSize);
        
    /* do the optimization here */
    g_trackStartPoint = startId;
    g_trackSize = trackSize;

    /* First we need to find an initial solution to the problem. Do
     * this by fitting a polynomial via least squares to the original spacecraft trajectory
     * ignoring the lidar data. Initialize this least squares by connecting a straight line
     * to the first and last points of the track.
     */
    printf("Beginning initialization fit\n");
    double minimizer[3*POLYNOMIAL_ORDER];
    initializeInitializationLeastSquares(minimizer, startId, trackSize);
    optimizeLbfgs(funcForInitialization, minimizer, 3*POLYNOMIAL_ORDER);


    
    /* Now that we have a good initial guess, do the real optimization */
    printf("Beginning Real optimization\n");
    if (solverType == LIBLBFGS)
    {
        optimizeLbfgs(func, minimizer, 3*POLYNOMIAL_ORDER);
    }


    
    double startTime = g_points[g_trackStartPoint].time;
    int endPoint = g_trackStartPoint + g_trackSize;
    int i;
    for (i=g_trackStartPoint; i<endPoint; ++i)
    {
        struct Point pt = g_points[i];

        double t = pt.time - startTime;

        evaluatePolynomial(t, minimizer, pt.scpos);
        
        vadd_c(pt.scpos, pt.boredir, pt.targetpos);

        
        struct Point ptOpt = g_pointsOptimized[i];
        
        vadd_c(ptOpt.scpos, pt.scpos, ptOpt.scpos);

        vadd_c(ptOpt.targetpos, pt.targetpos, ptOpt.targetpos);

        g_pointsOptimized[i] = ptOpt;
        
        ++g_numberOptimizationsPerPoint[i];
    }    

    printf("Finished optimizing track\n\n\n\n");
}


/************************************************************************
* 
************************************************************************/
void initializePointsOptimized()
{
    int i;
    for (i=0; i<NUMBER_POINTS; ++i)
    {
        g_pointsOptimized[i].time         = g_points[i].time;
        g_pointsOptimized[i].scpos[0]     = 0.0;
        g_pointsOptimized[i].scpos[1]     = 0.0;
        g_pointsOptimized[i].scpos[2]     = 0.0;
        g_pointsOptimized[i].targetpos[0] = 0.0;
        g_pointsOptimized[i].targetpos[1] = 0.0;
        g_pointsOptimized[i].targetpos[2] = 0.0;
        g_numberOptimizationsPerPoint[i] = 0;
    }
}


/************************************************************************
* 
************************************************************************/
void finalizePointsOptimized()
{
    int i;
    for (i=0; i<NUMBER_POINTS; ++i)
    {
        g_pointsOptimized[i].scpos[0]     /= g_numberOptimizationsPerPoint[i];
        g_pointsOptimized[i].scpos[1]     /= g_numberOptimizationsPerPoint[i];
        g_pointsOptimized[i].scpos[2]     /= g_numberOptimizationsPerPoint[i];
        g_pointsOptimized[i].targetpos[0] /= g_numberOptimizationsPerPoint[i];
        g_pointsOptimized[i].targetpos[1] /= g_numberOptimizationsPerPoint[i];
        g_pointsOptimized[i].targetpos[2] /= g_numberOptimizationsPerPoint[i];
    }
}


/************************************************************************
* This function determines if there is a break in a track due to a large
* enough time difference between two points. If there is a break, it
* returns the maximum size of the track such that there is no break.
************************************************************************/
int checkForBreakInTrack(int startId, int trackSize)
{
    int i;
    double t0 = g_points[startId].time;
    int endPoint = startId + trackSize;
    if (endPoint > NUMBER_POINTS)
        endPoint = NUMBER_POINTS;
    
    for (i=startId+1; i<endPoint; ++i)
    {
        double t1 = g_points[i].time;

        if (t1 - t0 > TRACK_BREAK_THRESHOLD)
            return (i - startId);
        
        t0 = t1;
    }

    return (i - startId);
}


/************************************************************************
* 
************************************************************************/
void optimizeAllTracks(SolverType solverType)
{
    initializePointsOptimized();

    int currentStartPoint = START_POINT;
    while(currentStartPoint < STOP_POINT)
    {
        int trackSize = checkForBreakInTrack(currentStartPoint, MAX_TRACK_SIZE);
        optimizeTrack(currentStartPoint, trackSize, solverType);
        
        if (trackSize == MAX_TRACK_SIZE)
        {
            trackSize = checkForBreakInTrack(currentStartPoint+1, MAX_TRACK_SIZE);
            if (trackSize == MAX_TRACK_SIZE)
                ++currentStartPoint;
            else
                currentStartPoint += trackSize + 1;           
        }
        else
        {
            currentStartPoint += trackSize;
        }
    }

    finalizePointsOptimized();
}


/************************************************************************
* 
************************************************************************/
void savePointsOptimized()
{
    int i;
    int count = 0;

    for (i=0; i<NUMBER_FILES; ++i)
    {
        const char* tabfilename = Tabfiles[i];
        FILE *fin = fopen(tabfilename, "r");
        if (fin == NULL)
        {
            printf("Could not open %s", tabfilename);
            exit(1);
        }

        const char* outfilename = Outfiles[i];
        FILE *fout = fopen(outfilename, "w");
        if (fout == NULL)
        {
            printf("Could not open %s", outfilename);
            exit(1);
        }
        
        char line[LINE_SIZE];
        char utc[UTC_SIZE];
        double sx;
        double sy;
        double sz;
        double x;
        double y;
        double z;
        
        while ( fgets ( line, sizeof line, fin ) != NULL ) /* read a line */
        {
            if (count >= START_POINT && count < STOP_POINT)
            {
                struct Point point = g_pointsOptimized[count];
                sx = point.scpos[0];
                sy = point.scpos[1];
                sz = point.scpos[2];
                x = point.targetpos[0];
                y = point.targetpos[1];
                z = point.targetpos[2];

                et2utc_c(point.time, "ISOC", 3, UTC_SIZE, utc);
                
                fprintf(fout, "0 %s 0 %f %f %f %f %f %f\n", utc, sx, sy, sz, x, y, z);
            }
            
            ++count;
        }

        printf("points written %d\n", count);
        fflush(NULL);
        fclose ( fin );
        fclose ( fout );
    }
}


/************************************************************************
* This program tries to find better positions of the lidar points than
* is published on the PDS.
************************************************************************/
int main(int argc, char** argv)
{
    SolverType solverType = LIBLBFGS;
    
    furnsh_c(kernelfiles);

    loadDsk(dskfile);
    
    loadPoints();

    optimizeAllTracks(solverType);
    
    savePointsOptimized();

    return 0;
}

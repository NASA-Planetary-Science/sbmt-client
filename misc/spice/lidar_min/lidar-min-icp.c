#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include "SpiceUsr.h"
#include "lbfgs.h"
#include "optimize.h"
#include "icp.h"
#include "closest-point.h"


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
#define MAX_NUMBER_POINTS 2000000
#define PATH_SIZE 256
#define LINE_SIZE 1024
#define UTC_SIZE 128
#define MAX_TRACK_SIZE 1000
#define TRACK_BREAK_THRESHOLD 500
#define NUMBER_FILES 3
#define USE_VTK_CLOSEST_POINT 1
#define USE_VTK_ICP 1
#define MAX_DIAGONAL_LENGTH 0.1
#define NOISE_THRESHOLD 0.01
const char Tabfiles[NUMBER_FILES][PATH_SIZE] =
{
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_uf2_20050911_20050930.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_uf2_20051001_20051031.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_uf2_20051101_20051118.tab"
};
const char* const Outfile = "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized";
const char* const kernelfiles = "/project/nearsdc/spice-kernels/hayabusa/kernels.txt";


/************************************************************************
* Structure for storing a lidar point
************************************************************************/
struct LidarPoint
{
    double time;
    double scpos[3];
    double targetpos[3];
    double closestpoint[3]; /* closest point on asteroid to targetpos */
    unsigned char isNoise; /* 1 if considered noise, 0 otherwise */
};


/************************************************************************
* Global varaiables
************************************************************************/

/* Array for storing all lidar points */
struct LidarPoint g_points[MAX_NUMBER_POINTS];

struct LidarPoint g_pointsOptimized[MAX_NUMBER_POINTS];

int g_numberOptimizationsPerPoint[MAX_NUMBER_POINTS];

/* The first point within the points variable to be optimized */
int g_trackStartPoint;

/* The number of points within */
int g_trackSize;

/* Start point to begin optimization with */
int g_startPoint;

/* Stop point to end optimization with */
int g_stopPoint;

/* The actual number of points read in from the files */
int g_actual_number_points;


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
            if (count >= MAX_NUMBER_POINTS)
            {
                printf("Error: Max number of allowable points exceeded!");
                exit(1);
            }
            
            sscanf(line, "%*s %s %*s %lf %lf %lf %lf %lf %lf", utc, &sx, &sy, &sz, &x, &y, &z);

            utc2et_c(utc, &time);

            struct LidarPoint point;
            point.time = time;
            point.scpos[0] = sx;
            point.scpos[1] = sy;
            point.scpos[2] = sz;
            point.targetpos[0] = x;
            point.targetpos[1] = y;
            point.targetpos[2] = z;
            point.closestpoint[0] = 0.0;
            point.closestpoint[1] = 0.0;
            point.closestpoint[2] = 0.0;
            point.isNoise = 0;
            
            g_points[count] = point;

            ++count;
        }

        printf("points read %d\n", count);
        fflush(NULL);
        fclose ( f );
    }

    g_actual_number_points = count;
    printf("Finished loading data\n\n\n");
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



    
    printf("Beginning ICP\n");
    int endPoint = g_trackStartPoint + g_trackSize;
    struct Point sources[trackSize];
    struct Point targets[trackSize];
    struct Point scpos[trackSize];
    int i,j;
    for (i=g_trackStartPoint,j=0; i<endPoint; ++i,++j)
    {
        struct LidarPoint pt = g_points[i];
        sources[j].p[0] = pt.targetpos[0];
        sources[j].p[1] = pt.targetpos[1];
        sources[j].p[2] = pt.targetpos[2];

        targets[j].p[0] = pt.closestpoint[0];
        targets[j].p[1] = pt.closestpoint[1];
        targets[j].p[2] = pt.closestpoint[2];

        scpos[j].p[0] = pt.scpos[0];
        scpos[j].p[1] = pt.scpos[1];
        scpos[j].p[2] = pt.scpos[2];
    }

    double minimizer[3];
    if (USE_VTK_ICP)
        icpVtk(sources, targets, trackSize, scpos);
    else
        icp(sources, targets, trackSize, minimizer);
    

    
    for (i=g_trackStartPoint,j=0; i<endPoint; ++i)
    {
        struct LidarPoint pt = g_points[i];

        if (USE_VTK_ICP)
        {
            pt.scpos[0] = scpos[j].p[0];
            pt.scpos[1] = scpos[j].p[1];
            pt.scpos[2] = scpos[j].p[2];

            pt.targetpos[0] = sources[j].p[0];
            pt.targetpos[1] = sources[j].p[1];
            pt.targetpos[2] = sources[j].p[2];
        }
        else
        {
            vadd_c(pt.scpos, minimizer, pt.scpos);

            vadd_c(pt.targetpos, minimizer, pt.targetpos);
        }

        struct LidarPoint ptOpt = g_pointsOptimized[i];
        
        vadd_c(ptOpt.scpos, pt.scpos, ptOpt.scpos);

        vadd_c(ptOpt.targetpos, pt.targetpos, ptOpt.targetpos);

        g_pointsOptimized[i] = ptOpt;
        
        ++g_numberOptimizationsPerPoint[i];
    }    

    printf("Finished optimizing track\n\n\n\n");
}


void initializeClosestPoints()
{
    SpiceBoolean found;
    int i;
    for (i=0; i<g_actual_number_points; ++i)
    {
        if (i% 1000 == 0)
            printf("finding closest point %d\n", i);
        
        struct LidarPoint pt = g_points[i];
        double closestPoint[3];
        double boredir[3] = {
            pt.targetpos[0] - pt.scpos[0],
            pt.targetpos[1] - pt.scpos[1],
            pt.targetpos[2] - pt.scpos[2] };
        
        if (USE_VTK_CLOSEST_POINT)
            findClosestPointVtk(pt.targetpos, closestPoint, &found);
        else
            findClosestPointDsk(pt.scpos, boredir, closestPoint, &found);

        g_points[i].closestpoint[0] = closestPoint[0];
        g_points[i].closestpoint[1] = closestPoint[1];
        g_points[i].closestpoint[2] = closestPoint[2];

        /* If the distance to the asteroid is too large, mark this point as noise */
        double dist = vdist_c(pt.targetpos, closestPoint);
        if (dist > NOISE_THRESHOLD)
            g_points[i].isNoise = 1;
        else
            g_points[i].isNoise = 0;
    }
}


/************************************************************************
* 
************************************************************************/
void initializePointsOptimized()
{
    int i;
    for (i=0; i<g_actual_number_points; ++i)
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


double computeBoundingBoxDiagonalOfTrack(int startId, int trackSize)
{
    int i;
    int endPoint = startId + trackSize;
    if (endPoint > g_actual_number_points)
        endPoint = g_actual_number_points;

    double xmin = 1.0e10;
    double xmax = -1.0e10;
    double ymin = 1.0e10;
    double ymax = -1.0e10;
    double zmin = 1.0e10;
    double zmax = -1.0e10;
    int count = 0;
    for (i=startId; i<endPoint; ++i)
    {
        if (g_points[i].isNoise)
            continue;
        
        if (g_points[i].targetpos[0] < xmin)
            xmin = g_points[i].targetpos[0];
        if (g_points[i].targetpos[0] > xmax)
            xmax = g_points[i].targetpos[0];

        if (g_points[i].targetpos[1] < ymin)
            ymin = g_points[i].targetpos[1];
        if (g_points[i].targetpos[1] > ymax)
            ymax = g_points[i].targetpos[1];

        if (g_points[i].targetpos[2] < zmin)
            zmin = g_points[i].targetpos[2];
        if (g_points[i].targetpos[2] > zmax)
            zmax = g_points[i].targetpos[2];

        ++count;
    }

    if (count == 0)
        return 0.0;
    
    double xext = xmax - xmin;
    double yext = ymax - ymin;
    double zext = zmax - zmin;
    
    return sqrt(xext*xext + yext*yext + zext*zext);
}


/************************************************************************
* This function determines if there is a break in a track due to a large
* enough time difference between two points or because the bounding box
* that encloses the track is too large. If there is a break, it
* returns the maximum size of the track such that there is no break.
* If for some reason, a data point preceded in time the previous point,
* that is considered a break, no matter how large.
************************************************************************/
int checkForBreakInTrack(int startId, int trackSize)
{
    int i;
    double t0 = g_points[startId].time;
    int endPoint = startId + trackSize;
    if (endPoint > g_actual_number_points)
        endPoint = g_actual_number_points;
    
    for (i=startId+1; i<endPoint; ++i)
    {
        double t1 = g_points[i].time;

        if (t1 - t0 > TRACK_BREAK_THRESHOLD || t1 - t0 < 0.0)
        {
            printf("Max time break exceeded. Length: %f, size: %d\n", t1-t0, i-startId);
            return (i - startId);
        }
        
        double diagLength = computeBoundingBoxDiagonalOfTrack(startId, i-startId+1);
        if (diagLength > MAX_DIAGONAL_LENGTH)
        {
            printf("Max diag length exceeded. Length: %f, size: %d\n", diagLength, i-startId);
            return (i - startId);
        }

        t0 = t1;
    }

    printf("No break in track found! size: %d\n", i-startId);
    return (i - startId);
}


/************************************************************************
* 
************************************************************************/
void optimizeAllTracks(SolverType solverType)
{
    initializePointsOptimized();

    int prevEndPoint = -1;
    int currentStartPoint = g_startPoint;
    while(currentStartPoint < g_stopPoint)
    {
        int trackSize = checkForBreakInTrack(currentStartPoint, MAX_TRACK_SIZE);

        int endPoint = currentStartPoint + trackSize;
        if (endPoint > prevEndPoint)
        {
            optimizeTrack(currentStartPoint, trackSize, solverType);
        }
        else
        {
            printf("Skipping this track since end point has not incremented. End point: %d\n", endPoint);
        }
        
        ++currentStartPoint;
        prevEndPoint = endPoint;
    }
}


/************************************************************************
* 
************************************************************************/
void savePointsOptimized()
{
    char outfilename[256];
    sprintf(outfilename, "%s_%d-%d", Outfile, g_startPoint, g_stopPoint);
    FILE *fout = fopen(outfilename, "w");
    if (fout == NULL)
    {
        printf("Could not open %s", outfilename);
        exit(1);
    }

    int numOptimizations;
    double sx;
    double sy;
    double sz;
    double x;
    double y;
    double z;

    int i;
    for (i=0; i<g_actual_number_points; ++i)
    {
        struct LidarPoint point = g_pointsOptimized[i];

        numOptimizations = g_numberOptimizationsPerPoint[i];
        sx = point.scpos[0];
        sy = point.scpos[1];
        sz = point.scpos[2];
        x = point.targetpos[0];
        y = point.targetpos[1];
        z = point.targetpos[2];

        fprintf(fout, "%d %.16e %.16e %.16e %.16e %.16e %.16e\n", numOptimizations, sx, sy, sz, x, y, z);
    }

    fclose ( fout );
}


/************************************************************************
* This program tries to find better positions of the lidar points than
* is published on the PDS.
************************************************************************/
int main(int argc, char** argv)
{
    if (argc < 3)
    {
        printf("Usage: lidar-min-icp <start-point> <stop-point>\n");;
        return 1;
    }

    g_startPoint = atoi(argv[1]);
    g_stopPoint  = atoi(argv[2]);

    SolverType solverType = LIBLBFGS;
    
    furnsh_c(kernelfiles);

    loadPoints();

    initializeClosestPoints();
    
    optimizeAllTracks(solverType);
    
    savePointsOptimized();

    return 0;
}

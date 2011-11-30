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
#define USE_VTK_CLOSEST_POINT 1
#define USE_VTK_ICP 0
#define MAX_TRACK_EXTENT 0.1
#define NOISE_THRESHOLD 0.01


/************************************************************************
* Structure for storing a lidar point
************************************************************************/
struct LidarPoint
{
    char met[11];
    char utc[24];
    char range[10];
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
void loadPoints(int argc, char** argv)
{
    printf("Loading data\n");
    int i;
    int count = 0;
    /* Element at index 5 of argv is start of input files */
    for (i=5; i<argc; ++i)
    {
        const char* filename = argv[i];
        FILE *f = fopen(filename, "r");
        if (f == NULL)
        {
            printf("Could not open %s", filename);
            exit(1);
        }

        char line[LINE_SIZE];
        
        while ( fgets ( line, LINE_SIZE, f ) != NULL ) /* read a line */
        {
            if (count >= MAX_NUMBER_POINTS)
            {
                printf("Error: Max number of allowable points exceeded!");
                exit(1);
            }
            
            struct LidarPoint point;

            sscanf(line, "%s %s %s %lf %lf %lf %lf %lf %lf",
                   point.met,
                   point.utc,
                   point.range,
                   &point.scpos[0],
                   &point.scpos[1],
                   &point.scpos[2],
                   &point.targetpos[0],
                   &point.targetpos[1],
                   &point.targetpos[2]);

            utc2et_c(point.utc, &point.time);

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

    if (USE_VTK_ICP)
        icpVtk(sources, targets, trackSize, scpos);
    else
        icp(sources, targets, trackSize, scpos);
    

    
    for (i=g_trackStartPoint,j=0; i<endPoint; ++i,++j)
    {
        struct LidarPoint pt = g_points[i];

        pt.scpos[0] = scpos[j].p[0];
        pt.scpos[1] = scpos[j].p[1];
        pt.scpos[2] = scpos[j].p[2];

        pt.targetpos[0] = sources[j].p[0];
        pt.targetpos[1] = sources[j].p[1];
        pt.targetpos[2] = sources[j].p[2];

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
        g_pointsOptimized[i]              = g_points[i];
        g_pointsOptimized[i].scpos[0]     = 0.0;
        g_pointsOptimized[i].scpos[1]     = 0.0;
        g_pointsOptimized[i].scpos[2]     = 0.0;
        g_pointsOptimized[i].targetpos[0] = 0.0;
        g_pointsOptimized[i].targetpos[1] = 0.0;
        g_pointsOptimized[i].targetpos[2] = 0.0;
        g_numberOptimizationsPerPoint[i] = 0;
    }
}

/* replaced with computeExtentOfTrack()
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
*/

/* Computes a measure of the overall extent of a track by computing
   the mean deviation of each point of the track from the centroid
   of the track */
double computeExtentOfTrack(int startId, int trackSize)
{
    int i;
    int endPoint = startId + trackSize;
    if (endPoint > g_actual_number_points)
        endPoint = g_actual_number_points;

    /* First compute centroid of points */
    double centroid[3] = {0.0, 0.0, 0.0};

    for (i=startId; i<endPoint; ++i)
    {
        centroid[0] += g_points[i].targetpos[0];
        centroid[1] += g_points[i].targetpos[1];
        centroid[2] += g_points[i].targetpos[2];
    }

    centroid[0] /= (double)trackSize;
    centroid[1] /= (double)trackSize;
    centroid[2] /= (double)trackSize;

    /* Next compute mean distance from the centroid */
    double meandist = 0.0;
    
    for (i=startId; i<endPoint; ++i)
    {
        meandist += vdist_c(centroid, g_points[i].targetpos);
    }

    meandist /= (double)trackSize;

    return meandist;
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

        double trackExtent = computeExtentOfTrack(startId, i-startId+1);
        if (trackExtent > MAX_TRACK_EXTENT)
        {
            printf("Max track extent exceeded. Length: %f, size: %d\n", trackExtent, i-startId);
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
void savePointsOptimized(const char* outfile)
{
    FILE *fout = fopen(outfile, "w");
    if (fout == NULL)
    {
        printf("Could not open %s", outfile);
        exit(1);
    }

    int i;
    for (i=0; i<g_actual_number_points; ++i)
    {
        struct LidarPoint point = g_pointsOptimized[i];

        fprintf(fout, "%d %s %s %8s %.16e %.16e %.16e %.16e %.16e %.16e\n",
                g_numberOptimizationsPerPoint[i],
                point.met,
                point.utc,
                point.range,
                point.scpos[0],
                point.scpos[1],
                point.scpos[2],
                point.targetpos[0],
                point.targetpos[1],
                point.targetpos[2]);
    }

    fclose ( fout );
}


/************************************************************************
* This program tries to find better positions of the lidar points than
* is published on the PDS.
************************************************************************/
int main(int argc, char** argv)
{
    if (argc < 6)
    {
        printf("Usage: lidar-min-icp <start-point> <stop-point> <kernelfiles> <outputfile> <inputfile1> [<inputfile2> ...]\n");;
        return 1;
    }

    g_startPoint = atoi(argv[1]);
    g_stopPoint  = atoi(argv[2]);

    const char* const kernelfiles = argv[3];
    const char* const outfile = argv[4];


    SolverType solverType = LIBLBFGS;
    
    furnsh_c(kernelfiles);

    loadPoints(argc, argv);

    initializeClosestPoints();
    
    optimizeAllTracks(solverType);
    
    savePointsOptimized(outfile);

    return 0;
}

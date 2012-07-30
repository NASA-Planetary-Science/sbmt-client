#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include "SpiceUsr.h"
#include "lbfgs.h"
#include "optimize.h"
#include "icp.h"
#include "icp-vtk.h"
#include "icp-intersection.h"
#include "closest-point-dsk.h"
#include "closest-point-vtk.h"


/************************************************************************
* Enumeration listing available small bodies
************************************************************************/
typedef enum BodyType
{
    ITOKAWA,
    EROS
} BodyType;


/************************************************************************
* Constants
************************************************************************/
#define PATH_SIZE 256
#define LINE_SIZE 1024
#define UTC_SIZE 128
#define MAX_TRACK_SIZE 100000
#define TRACK_BREAK_THRESHOLD 500
#define USE_VTK_CLOSEST_POINT 0
#define USE_VTK_ICP 0
#define MAX_TRACK_EXTENT 100000.1
#define NOISE_THRESHOLD 0.01


/************************************************************************
* Structure for storing a lidar point
************************************************************************/
struct LidarPoint
{
    char met[16];
    char utc[24];
    char rangeStr[12];
    double range;
    double time;
    double scpos[3];
    double targetpos[3];
    double closestpoint[3]; /* closest point on asteroid to targetpos */
    double boredir[3];
    unsigned char isNoise; /* 1 if considered noise, 0 otherwise */
};


/************************************************************************
* Global varaiables
************************************************************************/

/* Array for storing all lidar points */
std::vector<LidarPoint> g_points;

std::vector<LidarPoint> g_pointsOptimized;

std::vector<int> g_numberOptimizationsPerPoint;

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

BodyType g_bodyType;

/* The translation computed for the last track */
double g_translation[3];

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
    /* Element at index 7 of argv is start of input files */
    for (i=7; i<argc; ++i)
    {
        const char* filename = argv[i];
        FILE *f = fopen(filename, "r");
        if (f == NULL)
        {
            printf("Could not open %s", filename);
            exit(1);
        }

        char line[LINE_SIZE];
        double x;
        double y;
        double z;

        while ( fgets ( line, LINE_SIZE, f ) != NULL ) /* read a line */
        {
            struct LidarPoint point;

            if (g_bodyType == ITOKAWA)
            {
                sscanf(line, "%s %s %s %lf %lf %lf %lf %lf %lf",
                       point.met,
                       point.utc,
                       point.rangeStr,
                       &point.scpos[0],
                       &point.scpos[1],
                       &point.scpos[2],
                       &x,
                       &y,
                       &z);

                point.range = atof(point.rangeStr);
            }
            else if (g_bodyType == EROS)
            {
                // The first 2 lines have as the first character either an 'L' or 'l'.
                // Ignore them
                if (line[0] == 'L' || line[0] == 'l')
                    continue;

                int noise;
                double sclon;
                double sclat;
                double scrdst;
                sscanf(line, "%*s %*s %*s %*s %s %s %*s %d %lf %lf %lf %*s %*s %s %lf %lf %lf",
                       point.utc,
                       point.rangeStr,
                       &noise,
                       &sclon,
                       &sclat,
                       &scrdst,
                       point.met,
                       &x,
                       &y,
                       &z);

                if (noise == 1)
                    continue;

                point.range = atof(point.rangeStr);
                point.range /= 1000.0;

                x /= 1000.0;
                y /= 1000.0;
                z /= 1000.0;

                scrdst /= 1000.0;
                latrec_c(scrdst, sclon*M_PI/180.0, sclat*M_PI/180.0, point.scpos);
            }

            point.targetpos[0] = x;
            point.targetpos[1] = y;
            point.targetpos[2] = z;

            point.boredir[0] = x - point.scpos[0];
            point.boredir[1] = y - point.scpos[1];
            point.boredir[2] = z - point.scpos[2];
            vhat_c(point.boredir, point.boredir);

            utc2et_c(point.utc, &point.time);

            g_points.push_back(point);

            ++count;

            if (count % 10000 == 0)
                printf("points read %d\n", count);

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
void optimizeTrack(int startId, int trackSize)
{
    printf("Optimizing track starting at %d with size %d\n", startId, trackSize);

    /* do the optimization here */
    g_trackStartPoint = startId;
    g_trackSize = trackSize;


    int endPoint = g_trackStartPoint + g_trackSize;

    char fromTime[32];
    char toTime[32];
    et2utc_c(g_points[startId].time, "ISOD", 3, 32, fromTime);
    et2utc_c(g_points[endPoint-1].time, "ISOD", 3, 32, toTime);
    printf("From %s to %s\n\n", fromTime, toTime);


    printf("Beginning ICP\n");

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
//        icp(sources, targets, trackSize, scpos);
        icp2(sources, trackSize, scpos, g_translation);


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
    g_pointsOptimized.resize(g_actual_number_points);
    g_numberOptimizationsPerPoint.resize(g_actual_number_points);
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

/* Return distance squared between points x and y */
static double vdist2(const double x[3], const double y[3])
{
    return ( ( x[0] - y[0] ) * ( x[0] - y[0] )
             + ( x[1] - y[1] ) * ( x[1] - y[1] )
             + ( x[2] - y[2] ) * ( x[2] - y[2] ) );
}

/* Computes a measure of the overall extent of a track */
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

    /* Next compute a measure of the size of this set of points by
       computing the size a bounding box enclosing these points
       excluding points that are noise. Noisy points are those that
       are too far from the mean. */
    double xmin = 1.0e10;
    double xmax = -1.0e10;
    double ymin = 1.0e10;
    double ymax = -1.0e10;
    double zmin = 1.0e10;
    double zmax = -1.0e10;
    int count = 0;
    for (i=startId; i<endPoint; ++i)
    {
        if (vdist2(centroid, g_points[i].targetpos) > 4.0*MAX_TRACK_EXTENT*MAX_TRACK_EXTENT)
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

    return (xext*xext + yext*yext + zext*zext);
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
    /* double t0 = g_points[startId].time; */
    int endPoint = startId + trackSize;
    if (endPoint > g_actual_number_points)
        endPoint = g_actual_number_points;

    /*
    for (i=startId+1; i<endPoint; ++i)
    {
        double t1 = g_points[i].time;

        if (t1 - t0 > TRACK_BREAK_THRESHOLD || t1 - t0 < 0.0)
        {
            printf("Max time break exceeded. Length: %f, size: %d\n", t1-t0, i-startId);
            return (i - startId);
        }

        t0 = t1;
    }
    */

    for (i=endPoint-1; i>=startId+1; --i)
    {
        double trackExtent = computeExtentOfTrack(startId, i-startId+1);
        if (trackExtent <= MAX_TRACK_EXTENT*MAX_TRACK_EXTENT)
        {
            return (i - startId + 1);
        }

        if (i-startId+1 > 700)
            i = i - 9;
    }

    return 1;
}

int checkForBreakInTrack2(int startId, int trackSize)
{
    int i;
    double t0 = g_points[startId].time;
    int endPoint = startId + trackSize;
    if (endPoint > g_actual_number_points)
        endPoint = g_actual_number_points;


    double maxTrackLength = 0.0; // in km
    if (g_bodyType == EROS)
        maxTrackLength = 10.0;
    else if (g_bodyType == ITOKAWA)
        maxTrackLength = 0.25;


    double length = 0.0;

    for (i=startId+1; i<endPoint; ++i)
    {
        double t1 = g_points[i].time;
        if (t1 - t0 > TRACK_BREAK_THRESHOLD || t1 - t0 < 0.0)
        {
            printf("Max time break exceeded. Length: %f seconds, size: %d\n", t1-t0, i-startId);
            return (i - startId);
        }
        t0 = t1;


        double vecFromLastPoint[3] = {
            g_points[i].targetpos[0] - g_points[i-1].targetpos[0],
            g_points[i].targetpos[1] - g_points[i-1].targetpos[1],
            g_points[i].targetpos[2] - g_points[i-1].targetpos[2]
        };
        double distFromLastPoint = sqrt(
                    vecFromLastPoint[0]*vecFromLastPoint[0] +
                    vecFromLastPoint[1]*vecFromLastPoint[1] +
                    vecFromLastPoint[2]*vecFromLastPoint[2]);
        length += distFromLastPoint;
        if (length > maxTrackLength)
        {
            printf("Max track length exceeded. Length: %f km, size: %d\n", length, i-startId);
            return (i - startId);
        }
    }


    return i - startId;
}


/************************************************************************
*
************************************************************************/
void optimizeAllTracks()
{
    initializePointsOptimized();

    int prevEndPoint = -1;
    int currentStartPoint = g_startPoint;
    while(currentStartPoint < g_stopPoint && currentStartPoint < g_actual_number_points)
    {
        int trackSize = checkForBreakInTrack2(currentStartPoint, MAX_TRACK_SIZE);

        int endPoint = currentStartPoint + trackSize;
        if (endPoint > prevEndPoint)
        {
            optimizeTrack(currentStartPoint, trackSize);
        }
        else
        {
            printf("Skipping this track since end point has not incremented. End point: %d\n", endPoint);
        }

        prevEndPoint = endPoint;

        int amountToIncrement = trackSize / 10;
        if (amountToIncrement < 1)
            amountToIncrement = 1;
        currentStartPoint += amountToIncrement;
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

    if (g_bodyType == EROS)
    {
        fprintf(fout, "Header line 1\n");
        fprintf(fout, "Header line 2\n");
    }

    int i;
    for (i=0; i<g_actual_number_points; ++i)
    {
        struct LidarPoint point = g_pointsOptimized[i];

        if (g_bodyType == ITOKAWA)
        {
            fprintf(fout, "%d %s %s %8s %.16e %.16e %.16e %.16e %.16e %.16e\n",
                    g_numberOptimizationsPerPoint[i],
                    point.met,
                    point.utc,
                    point.rangeStr,
                    point.scpos[0],
                    point.scpos[1],
                    point.scpos[2],
                    point.targetpos[0],
                    point.targetpos[1],
                    point.targetpos[2]);
        }
        else if (g_bodyType == EROS)
        {
            double sclon;
            double sclat;
            double scrdst;
            reclat_c(point.scpos, &scrdst, &sclon, &sclat);
            fprintf(fout, "0 0 0 0 %s %s 0 0 %.16e %.16e %.16e 0 0 %s %.16e %.16e %.16e\n",
                    //g_numberOptimizationsPerPoint[i],
                    point.utc,
                    point.rangeStr,
                    sclon * 180.0 / M_PI,
                    sclat * 180.0 / M_PI,
                    1000.0 * scrdst,
                    point.met,
                    1000.0 * point.targetpos[0],
                    1000.0 * point.targetpos[1],
                    1000.0 * point.targetpos[2]);
        }

    }

    fclose ( fout );
}


void printTranslationOfLastTrack()
{
    printf("\nOptimal Translation:\n%.16g %.16g %.16g\n",
            g_translation[0],
            g_translation[1],
            g_translation[2]);
}


/************************************************************************
* This program tries to find better positions of the lidar points than
* is published on the PDS.
************************************************************************/
int main(int argc, char** argv)
{
    if (argc < 8)
    {
        printf("Usage: lidar-min-icp <body> <dskfile> <start-point> <stop-point> <kernelfiles> <outputfile> <inputfile1> [<inputfile2> ...]\n");;
        return 1;
    }

    char* body = argv[1];
    char* dskfile = argv[2];

    g_startPoint = atoi(argv[3]);
    g_stopPoint  = atoi(argv[4]);

    const char* const kernelfiles = argv[5];
    const char* const outfile = argv[6];

    g_bodyType = EROS;
    if (!strcmp(body, "ITOKAWA"))
        g_bodyType = ITOKAWA;

//    initializeDsk(dskfile);
    initializeVtk(dskfile);

    furnsh_c(kernelfiles);

    loadPoints(argc, argv);

//    initializeClosestPoints();

    optimizeAllTracks();

    savePointsOptimized(outfile);

    printTranslationOfLastTrack();

    return 0;
}

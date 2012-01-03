#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "SpiceUsr.h"
#include "closest-point.h"


/************************************************************************
* Constants
************************************************************************/
#define MAX_NUMBER_POINTS 2000000
#define LINE_SIZE 1024
#define UTC_SIZE 128
#define USE_VTK 0

/************************************************************************
* Structure for storing a lidar point
************************************************************************/
struct LidarPoint
{
    double scpos[3];
    double targetpos[3];
    double closestpoint[3]; /* closest point on asteroid to targetpos */
    double range;
};


/************************************************************************
* Global varaiables
************************************************************************/

/* Array for storing all lidar points */
struct LidarPoint g_points[MAX_NUMBER_POINTS];
int g_actual_number_points;

/************************************************************************
* Function which loads points from "tab" files into points
* global variable
************************************************************************/
void loadPoints(int argc, char** argv)
{
    printf("Loading data\n");
    int i;
    int count = 0;
    for (i=1; i<argc; ++i)
    {
        const char* filename = argv[i];
        FILE *f = fopen(filename, "r");
        if (f == NULL)
        {
            printf("Could not open %s", filename);
            exit(1);
        }

        char line[LINE_SIZE];
        char utc[UTC_SIZE];
        double range;
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
            
            sscanf(line, "%*s %s %lf %lf %lf %lf %lf %lf %lf", utc, &range, &sx, &sy, &sz, &x, &y, &z);

            struct LidarPoint point;
            point.range = range;
            point.scpos[0] = sx;
            point.scpos[1] = sy;
            point.scpos[2] = sz;
            point.targetpos[0] = x;
            point.targetpos[1] = y;
            point.targetpos[2] = z;
            point.closestpoint[0] = 0.0;
            point.closestpoint[1] = 0.0;
            point.closestpoint[2] = 0.0;
            
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


void computeRMS()
{
    double rms = 0.0;
    double meanDist = 0.0;
    double meanRangeError = 0.0;
    int numberWithNoIntersect = 0;
    int i;
    for (i=0; i<g_actual_number_points; ++i)
    {
        if (i % 1000 == 0)
            printf("finding closest point %d\n", i);
        
        struct LidarPoint pt = g_points[i];
        double closestPoint[3];
        double boredir[3] = {
            pt.targetpos[0] - pt.scpos[0],
            pt.targetpos[1] - pt.scpos[1],
            pt.targetpos[2] - pt.scpos[2] };

        SpiceBoolean found = 1;
        if (USE_VTK)
            findClosestPointVtk(pt.targetpos, closestPoint, &found);
        else
            findClosestPointDsk(pt.scpos, boredir, closestPoint, &found);

        if (!found)
        {
            printf("%d not found\n", i);
            ++numberWithNoIntersect;
            continue;
        }
        
        double dist = vdist_c(pt.targetpos, closestPoint);
        rms += dist*dist;
        meanDist += dist;
        
        double computedRange = vdist_c(pt.scpos, closestPoint);
        meanRangeError += fabs(computedRange - pt.range);
    }

    printf("RMS = %f\n", sqrt(rms/g_actual_number_points));
    printf("Mean distance = %f\n", meanDist/g_actual_number_points);
    printf("Mean range error = %f\n", meanRangeError/g_actual_number_points);
    printf("number with no intersect %d\n", numberWithNoIntersect);
}


/************************************************************************
* This program tries to find better positions of the lidar points than
* is published on the PDS.
************************************************************************/
int main(int argc, char** argv)
{
    loadPoints(argc, argv);

    computeRMS();
    
    return 0;
}

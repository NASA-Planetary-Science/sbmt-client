#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <vector>
#include "SpiceUsr.h"
#include "closest-point-dsk.h"
#include "closest-point-vtk.h"


/************************************************************************
* Constants
************************************************************************/
#define LINE_SIZE 1024
#define UTC_SIZE 128
#define USE_VTK 0

/************************************************************************
* Structure for storing a lidar point
************************************************************************/
struct Point
{
    double scpos[3];
    double targetpos[3];
    double range;
};

typedef enum BodyType
{
    ITOKAWA,
    EROS
} BodyType;


/************************************************************************
* Global varaiables
************************************************************************/

/* Array for storing all lidar points */
std::vector<Point> g_points;

/************************************************************************
* Function which loads points from "tab" files into points
* global variable
************************************************************************/
void loadPoints(int argc, char** argv, BodyType bodyType)
{
    printf("Loading data\n");
    int i;
    for (i=3; i<argc; ++i)
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

        int count = -1;

        while ( fgets ( line, sizeof line, f ) != NULL ) /* read a line */
        {
            ++count;
            struct Point point;

            if (bodyType == ITOKAWA)
            {
                sscanf(line, "%*s %s %lf %lf %lf %lf %lf %lf %lf",
                       utc,
                       &point.range,
                       &point.scpos[0],
                       &point.scpos[1],
                       &point.scpos[2],
                       &point.targetpos[0],
                       &point.targetpos[1],
                       &point.targetpos[2]);
            }
            else if (bodyType == EROS)
            {
                if (count < 2)
                    continue;

                int noise;
                double sclon;
                double sclat;
                double scrdst;
                sscanf(line, "%*s %*s %*s %*s %s %lf %*s %d %lf %lf %lf %*s %*s %*s %lf %lf %lf",
                       utc,
                       &point.range,
                       &noise,
                       &sclon,
                       &sclat,
                       &scrdst,
                       &point.targetpos[0],
                       &point.targetpos[1],
                       &point.targetpos[2]);

                if (noise == 1)
                    continue;

                point.range /= 1000.0;

                point.targetpos[0] /= 1000.0;
                point.targetpos[1] /= 1000.0;
                point.targetpos[2] /= 1000.0;

                scrdst /= 1000.0;
                latrec_c(scrdst, sclon*M_PI/180.0, sclat*M_PI/180.0, point.scpos);
            }

            g_points.push_back(point);
        }

        printf("points read %ld\n", g_points.size());
        fflush(NULL);
        fclose ( f );
    }

    printf("Finished loading data\n\n\n");
}


void computeRMS()
{
    double rms = 0.0;
    double meanDist = 0.0;
    double meanRangeError = 0.0;
    int numberWithNoIntersect = 0;
    int i;
    int numPoints = g_points.size();
    for (i=0; i<numPoints; ++i)
    {
        if (i % 1000 == 0)
            printf("finding closest point %d\n", i);

        struct Point pt = g_points[i];
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

    printf("RMS = %f\n", sqrt(rms/numPoints));
    printf("Mean distance = %f\n", meanDist/numPoints);
    printf("Mean range error = %f\n", meanRangeError/numPoints);
    printf("number with no intersect %d\n", numberWithNoIntersect);
}


/************************************************************************
* This program tries to find better positions of the lidar points than
* is published on the PDS.
************************************************************************/
int main(int argc, char** argv)
{
    char* body = argv[1];
    char* dskfile = argv[2];

    initializeDsk(dskfile);

    BodyType bodyType = EROS;
    if (!strcmp(body, "ITOKAWA"))
        bodyType = ITOKAWA;

    loadPoints(argc, argv, bodyType);

    computeRMS();

    return 0;
}

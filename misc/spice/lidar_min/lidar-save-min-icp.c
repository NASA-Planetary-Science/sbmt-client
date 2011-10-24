#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

/************************************************************************
* Constants
************************************************************************/
#define NUMBER_POINTS 1114386
#define PATH_SIZE 256
#define LINE_SIZE 1024
#define UTC_SIZE 128
#define MAX_TRACK_SIZE 500
#define TRACK_BREAK_THRESHOLD 60
#define NUMBER_FILES 3
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


/************************************************************************
* Structure for storing a lidar point
************************************************************************/
struct LidarPoint
{
    double scpos[3];
    double targetpos[3];
};


/************************************************************************
* Global varaiables
************************************************************************/

struct LidarPoint g_pointsOptimized[NUMBER_POINTS];

int g_numberOptimizationsPerPoint[NUMBER_POINTS];


/************************************************************************
* Function which loads points from "tab" files into points
* global variable
************************************************************************/
void loadPoints(int argc, char** argv)
{
    printf("Loading data\n");
    int i;
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
        int numOptimized;
        double sx;
        double sy;
        double sz;
        double x;
        double y;
        double z;
        int count = 0;
        
        while ( fgets ( line, sizeof line, f ) != NULL ) /* read a line */
        {
            sscanf(line, "%d %lf %lf %lf %lf %lf %lf", &numOptimized, &sx, &sy, &sz, &x, &y, &z);

            struct LidarPoint point = g_pointsOptimized[count];
            point.scpos[0] += sx;
            point.scpos[1] += sy;
            point.scpos[2] += sz;
            point.targetpos[0] += x;
            point.targetpos[1] += y;
            point.targetpos[2] += z;
            
            g_pointsOptimized[count] = point;
            g_numberOptimizationsPerPoint[count] += numOptimized;

            ++count;
        }

        printf("points read %d\n", count);
        fflush(NULL);
        fclose ( f );
    }
    printf("Finished loading data\n\n\n");
}


void initializePointsOptimized()
{
    int i;
    for (i=0; i<NUMBER_POINTS; ++i)
    {
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
            sscanf(line, "%*s %s %*s %lf %lf %lf %lf %lf %lf", utc, &sx, &sy, &sz, &x, &y, &z);

            struct LidarPoint point = g_pointsOptimized[count];
            sx = point.scpos[0];
            sy = point.scpos[1];
            sz = point.scpos[2];
            x = point.targetpos[0];
            y = point.targetpos[1];
            z = point.targetpos[2];

            fprintf(fout, "0 %s 0 %f %f %f %f %f %f\n", utc, sx, sy, sz, x, y, z);
            
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
    loadPoints(argc, argv);

    finalizePointsOptimized();
    
    savePointsOptimized();

    return 0;
}

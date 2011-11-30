#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include "closest-point.h"
#include "SpiceUsr.h"


/************************************************************************
* Constants
************************************************************************/
#define MAX_NUMBER_POINTS 2000000
#define PATH_SIZE 256
#define LINE_SIZE 1024
#define WORD_SIZE 128
#define NUMBER_FILES 3
const char Tabfiles[NUMBER_FILES][PATH_SIZE] =
{
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_uf2_20050911_20050930.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_uf2_20051001_20051031.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_uf2_20051101_20051118.tab"
};
const char Outfiles[NUMBER_FILES][PATH_SIZE] =
{
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized2_20050911_20050930.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized2_20051001_20051031.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized2_20051101_20051118.tab"
};


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

struct LidarPoint g_pointsOptimized[MAX_NUMBER_POINTS];

int g_numberOptimizationsPerPoint[MAX_NUMBER_POINTS];

/* The actual number of points read in from the files */
int g_actual_number_points;


/************************************************************************
* Function which loads points from "tab" files into points
* global variable
************************************************************************/
void loadPoints(int argc, char** argv)
{
    printf("Loading data\n");
    int i;
    for (i=2; i<argc; ++i)
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

        g_actual_number_points = count;
        printf("points read %d\n", count);
        fflush(NULL);
        fclose ( f );
    }

    printf("Finished loading data\n\n\n");
    if (g_actual_number_points == 0)
    {
        printf("Error. No points were loaded.\n");
        exit(1);
    }
}


void initializePointsOptimized()
{
    int i;
    for (i=0; i<MAX_NUMBER_POINTS; ++i)
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
    for (i=0; i<g_actual_number_points; ++i)
    {
        g_pointsOptimized[i].scpos[0]     /= g_numberOptimizationsPerPoint[i];
        g_pointsOptimized[i].scpos[1]     /= g_numberOptimizationsPerPoint[i];
        g_pointsOptimized[i].scpos[2]     /= g_numberOptimizationsPerPoint[i];
        g_pointsOptimized[i].targetpos[0] /= g_numberOptimizationsPerPoint[i];
        g_pointsOptimized[i].targetpos[1] /= g_numberOptimizationsPerPoint[i];
        g_pointsOptimized[i].targetpos[2] /= g_numberOptimizationsPerPoint[i];
    }
}


void intersectWithAsteroid(const double sc[3],
                           const double lidarpoint[3],
                           double intersect[3],
                           int* found)
{
    double boredir[3] = {
        lidarpoint[0] - sc[0],
        lidarpoint[1] - sc[1],
        lidarpoint[2] - sc[2] };

    findClosestPointDsk(sc, boredir, intersect, found);
}

/************************************************************************
* 
************************************************************************/
void savePointsOptimized()
{
    int i;
    int count = 0;
    const char* ref = "IAU_ITOKAWA";
    const char* frame = "HAYABUSA_NIRS";
    const double nirs_fov = 0.000872664; /* From nirs10.ti */

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
        char met[WORD_SIZE];
        char utc[WORD_SIZE];
        char range[WORD_SIZE];
        int found;
        
        while ( fgets ( line, sizeof line, fin ) != NULL ) /* read a line */
        {
            sscanf(line, "%s %s %s", met, utc, range);

            double et;
            utc2et_c(utc, &et);

            struct LidarPoint point = g_pointsOptimized[count];
            ++count;

            double spcx = point.scpos[0];              /* Column 4 */
            double spcy = point.scpos[1];              /* Column 5 */
            double spcz = point.scpos[2];              /* Column 6 */
            double blx = point.targetpos[0];           /* Column 7 */
            double bly = point.targetpos[1];           /* Column 8 */
            double blz = point.targetpos[2];           /* Column 9 */
            double intersect[3];
            intersectWithAsteroid(point.scpos, point.targetpos, intersect, &found);
            if (!found)
            {
                printf("no intersection found!\n");
                continue;
            }

            double bex = intersect[0];                 /* Column 10 */
            double bey = intersect[1];                 /* Column 11 */
            double bez = intersect[2];                 /* Column 12 */

            double inc;                                /* Column 13 */
            double emis;                               /* Column 14 */
            double phase;                              /* Column 15 */
            illum_pl02Dsk(et, intersect, &phase, &inc, &emis);
            inc *= dpr_c();
            emis *= dpr_c();
            phase *= dpr_c();

            double fovn =                              /* Column 17 */
                (0.1/dpr_c()) * atof(range) * 1000.0;
            double fovl = fovn * 0.687549766357;       /* Column 16 */

            double rad;
            double lon;                                /* Column 18 */
            double lat;                                /* Column 19 */
            reclat_c(point.targetpos, &rad, &lon, &lat);
            lon *= dpr_c();
            lat *= dpr_c();
            
            double int_lon;                            /* Column 20 */
            double int_lat;                            /* Column 21 */
            reclat_c(intersect, &rad, &int_lon, &int_lat);
            int_lon *= dpr_c();
            int_lat *= dpr_c();

            double mean_inc = 0.;                      /* Column 22 */
            double mean_emis = 0.;                     /* Column 23 */
            double mean_phase = 0.;                    /* Column 24 */
            double mean_lon = 0.;                      /* Column 25 */
            double mean_lat = 0.;                      /* Column 26 */
            double min_lon = 1.0e30;                   /* Column 27 */
            double min_lat = 1.0e30;                   /* Column 28 */
            double max_lon = -1.0e30;                  /* Column 29 */
            double max_lat = -1.0e30;                  /* Column 30 */
            int N = 0;                                 /* Column 31 */
            int j,k;
            double i2bmat[3][3];
            double vpxi[3];
            double boredir[3];
            pxform_c(frame, ref, et, i2bmat);
            for (j=-1; j<=1; ++j)
                for (k=-1; k<=1; ++k)
                {
                    vpxi[0] = (double)j * nirs_fov / 2.0;
                    vpxi[1] = (double)k * nirs_fov / 2.0;
                    vpxi[2] = 1.0;
                    mxv_c(i2bmat, vpxi, &boredir[0]);

                    findClosestPointDsk(point.scpos, boredir, intersect, &found);
                    if (found)
                    {
                        ++N;
                        double tmp_phase, tmp_inc, tmp_emis, tmp_lon, tmp_lat;
                        illum_pl02Dsk(et, intersect, &tmp_phase, &tmp_inc, &tmp_emis);
                        reclat_c(intersect, &rad, &tmp_lon, &tmp_lat);
                        
                        mean_phase += tmp_phase;
                        mean_inc += tmp_inc;
                        mean_emis += tmp_emis;
                        mean_lon += tmp_lon;
                        mean_lat += tmp_lat;

                        if (tmp_lon < min_lon)
                            min_lon = tmp_lon;
                        if (tmp_lat < min_lat)
                            min_lat = tmp_lat;
                        if (tmp_lon > max_lon)
                            max_lon = tmp_lon;
                        if (tmp_lat > max_lat)
                            max_lat = tmp_lat;
                    }
                }
            if (N > 0)
            {
                mean_inc /= (double)N;
                mean_emis /= (double)N;
                mean_phase /= (double)N;
                mean_lon /= (double)N;
                mean_lat /= (double)N;

                mean_inc *= dpr_c();
                mean_emis *= dpr_c();
                mean_phase *= dpr_c();
                mean_lon *= dpr_c();
                mean_lat *= dpr_c();
                min_lon *= dpr_c();
                min_lat *= dpr_c();
                max_lon *= dpr_c();
                max_lat *= dpr_c();
            }
            else
            {
                printf("No intersection found!\n");
            }
            
            fprintf(fout, "%s %s %8s %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f %7.3f %7.3f %7.3f %7.3f %7.3f %8.3f %8.3f %8.3f %8.3f %7.3f %7.3f %7.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %d\r\n",
                    met,
                    utc,
                    range,
                    spcx, spcy, spcz,
                    blx, bly, blz,
                    bex, bey, bez,
                    inc, emis, phase,
                    fovl, fovn,
                    lon, lat,
                    int_lon, int_lat,
                    mean_inc, mean_emis, mean_phase,
                    mean_lon, mean_lat,
                    min_lon, min_lat,
                    max_lon, max_lat,
                    N
                );
        }

        printf("points written %d\n", count);
        fflush(NULL);
        fclose ( fin );
        fclose ( fout );
    }
}


/************************************************************************
* This program saves out the results of the lidar-min-icp program
* in the same format as the cdr_uf_* files. Note the lidar-min-icp
* program is meant to be run several times in parallel. This program
* takes output files from all the individual lidar-min-icp programs
* and creates the output file in the format to be shipped to PDS.
************************************************************************/
int main(int argc, char** argv)
{
    const char* const kernelfiles = argv[1];

    furnsh_c(kernelfiles);

    initializePointsOptimized();

    loadPoints(argc, argv);

    finalizePointsOptimized();
    
    savePointsOptimized();

    return 0;
}

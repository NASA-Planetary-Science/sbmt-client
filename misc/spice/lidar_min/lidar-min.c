#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "SpiceUsr.h"
#include "SpiceDLA.h"
#include "SpiceDSK.h"
#include "lbfgs.h"


/************************************************************************
* Constants
************************************************************************/
#define NUMBER_POINTS 1114386
//#define NUMBER_POINTS 1000
#define PATH_SIZE 256
#define LINE_SIZE 1024
#define UTC_SIZE 128
#define MAX_TRACK_SIZE 50
#define TRACK_BREAK_THRESHOLD 60
#define DX 0.00001
#define NUMBER_FILES 3
const char tabfiles[NUMBER_FILES][PATH_SIZE] =
{
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_f_20050911_20050930.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_f_20051001_20051031.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_f_20051101_20051118.tab"
};
const char outfiles[NUMBER_FILES][PATH_SIZE] =
{
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized_20050911_20050930.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized_20051001_20051031.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized_20051101_20051118.tab"
};


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
* Structure for storing a lidar point
************************************************************************/
struct Point
{
    double time;
    double scpos[3];
    double targetpos[3];
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
    printf("point %d: %lf %lf %lf %lf %lf %lf %lf \n",
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
    printf("%s - %lf %lf %lf\n", str, pt[0], pt[1], pt[2]);
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
        const char* filename = tabfiles[i];
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
            if (count >= NUMBER_POINTS)
                break;

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
void loadDsk(char* dskfile)
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
* The main objective function to be optimized.
* The independent variables are a 3 element vector representing the
* translation of all the points.
************************************************************************/
double func(const double* shift)
{
//    printpt("shift amount", shift);
    
    int plid;
    SpiceBoolean found;
    int i;
    int endPoint = g_trackStartPoint + g_trackSize;
    double ssd = 0.0;
    for (i=g_trackStartPoint; i<endPoint; ++i)
    {
        struct Point pt = g_points[i];

        double scShifted[3];
        vadd_c(pt.scpos, shift, scShifted);

        double targetShifted[3];
        vadd_c(pt.targetpos, shift, targetShifted);

        double boredir[3];
        vsub_c(targetShifted, scShifted, boredir);

        /* Shoot this ray into shape model */
        double xpt[3];
        dskx02_c ( g_handle, &g_dladsc, scShifted, boredir,
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
    }
    
    return ssd;
}

/************************************************************************
* This function numerically computes the gradient of func using finite differences
************************************************************************/
void grad(const double* shift, double* gradient)
{
    double f = func(shift);

    int i;
    for (i=0; i<3; ++i)
    {
        double shift2[3] = {shift[0], shift[1], shift[2]};
        shift2[i] += DX;

        double f2 = func(shift2);

        gradient[i] = (f2 - f) / DX;
    }
}



//-----------------------------------------------------------------------
//-----------------------------------------------------------------------
// LBFGS solver related functions and variables
//-----------------------------------------------------------------------
//-----------------------------------------------------------------------
static lbfgsfloatval_t evaluate(
    void *instance,
    const lbfgsfloatval_t *x,
    lbfgsfloatval_t *g,
    const int n,
    const lbfgsfloatval_t step
    )
{
    lbfgsfloatval_t fx = func(x);
    grad(x, g);
    return fx;
}

static int progress(
    void *instance,
    const lbfgsfloatval_t *x,
    const lbfgsfloatval_t *g,
    const lbfgsfloatval_t fx,
    const lbfgsfloatval_t xnorm,
    const lbfgsfloatval_t gnorm,
    const lbfgsfloatval_t step,
    int n,
    int k,
    int ls
    )
{
    printf("Iteration %d:\n", k);
    printf("  fx = %f, x[0] = %f, x[1] = %f, x[1] = %f\n", fx, x[0], x[1], x[2]);
    printf("  xnorm = %f, gnorm = %f, step = %f\n", xnorm, gnorm, step);
    printf("\n");
    return 0;
}


static lbfgs_parameter_t param;

void initializeLiblbfgs()
{
    lbfgs_parameter_init(&param);
}

void runLiblbfgs(double* minimizer)
{
    const int N = 3;
    lbfgsfloatval_t fx;
    lbfgsfloatval_t *x = lbfgs_malloc(N);

    /* Initialize the variables. */
    int i;
    for (i = 0;i < N;++i)
        x[i] = 0.0;
    
    int ret = lbfgs(N, x, &fx, evaluate, progress, NULL, &param);
    
    /* Report the result. */
    printf("L-BFGS optimization terminated with status code = %d\n", ret);
    printf("  fx = %f, x[0] = %f, x[1] = %f, x[2] = %f\n", fx, x[0], x[1], x[2]);

    /* return the minimizer to the calling function */
    for (i = 0;i < N;++i)
        minimizer[i] = x[i];
    
    lbfgs_free(x);
}
//-----------------------------------------------------------------------
// End LBFGS
//-----------------------------------------------------------------------




/************************************************************************
* Does the optimization of a single track
************************************************************************/
void optimizeTrack(int startId, int trackSize, SolverType solverType)
{
    printf("Optimizing track starting at %d with size %d\n\n", startId, trackSize);
        
    // do the optimization here
    g_trackStartPoint = startId;
    g_trackSize = trackSize;

    double minimizer[3] = {0.0, 0.0, 0.0};
    double value = func(minimizer);
    printf("Initial value of objective function: %lf\n", value);
    if (solverType == LIBLBFGS)
    {
        runLiblbfgs(minimizer);
    }

    
    
    int i;
    int endPoint = g_trackStartPoint + g_trackSize;
    for (i=g_trackStartPoint; i<endPoint; ++i)
    {
        struct Point pt = g_points[i];

        vadd_c(pt.scpos, minimizer, pt.scpos);

        vadd_c(pt.targetpos, minimizer, pt.targetpos);

        struct Point ptOpt = g_pointsOptimized[i];
        
        vadd_c(ptOpt.scpos, pt.scpos, ptOpt.scpos);

        vadd_c(ptOpt.targetpos, pt.targetpos, ptOpt.targetpos);

        g_pointsOptimized[i] = ptOpt;
        
        ++g_numberOptimizationsPerPoint[i];
    }    

    printf("Finished optimizing track\n\n");
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

    if (solverType == LIBLBFGS)
    {
        initializeLiblbfgs();        
    }
    
    int currentStartPoint = 0;
    while(currentStartPoint < NUMBER_POINTS)
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
        const char* tabfilename = tabfiles[i];
        FILE *fin = fopen(tabfilename, "r");
        if (fin == NULL)
        {
            printf("Could not open %s", tabfilename);
            exit(1);
        }

        const char* outfilename = outfiles[i];
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
            if (count >= NUMBER_POINTS)
                break;

            struct Point point = g_pointsOptimized[count];
            sx = point.scpos[0];
            sy = point.scpos[1];
            sz = point.scpos[2];
            x = point.targetpos[0];
            y = point.targetpos[1];
            z = point.targetpos[2];

            et2utc_c(point.time, "ISOC", 3, UTC_SIZE, utc);
            
            fprintf(fout, "0 %s 0 %lf %lf %lf %lf %lf %lf\n", utc, sx, sy, sz, x, y, z);

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
    if (argc < 3) 
	{
	    fprintf(stderr, "usage: lidar-min <kernelfiles> <shapemodel> [<solver>]\n");
	    exit(1);
	}

    char* kernelfiles = argv[1];
    char* dskfile = argv[2];

    SolverType solverType = LIBLBFGS;
    if (argc >= 4)
    {
        char* solver = argv[3];
        if (!strcmp(solver, "LIBLBFGS"))
            solverType = LIBLBFGS;
    }
    
    furnsh_c(kernelfiles);

    loadDsk(dskfile);
    
    loadPoints(tabfiles, 3);

    optimizeAllTracks(solverType);
    
    savePointsOptimized();

    return 0;
}

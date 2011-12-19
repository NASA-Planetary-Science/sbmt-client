#include <iostream>
#include <vector>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include "SpiceUsr.h"
#include "lbfgs.h"
#include "optimize.h"
#include "adolc/adolc.h"
#include "closest-point.h"
#include <gsl/gsl_bspline.h>
#include <gsl/gsl_multifit.h>
#include <gsl/gsl_rng.h>
#include <gsl/gsl_randist.h>
#include <gsl/gsl_statistics.h>

using namespace std;

extern "C"
{
void optimizeGsl(double (*function)(const double*, void *externalParams),
                 void (*gradient)(const double*, double*, void *externalParams),
                 double* minimizer,
                 size_t N,
                 void *externalParams);
}

namespace
{

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
#define MAX_TRACK_SIZE 10000
#define TRACK_BREAK_THRESHOLD 10
#define USE_VTK_CLOSEST_POINT 0
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
    char rangeStr[10];
    double range;
    double time;
    double scpos[3];
    double boredir[3];
};

struct FunctionParams
{
    int startPoint;
    int endPoint;
    int ncoeffsPerDim;
    gsl_bspline_workspace* bw[3];
    gsl_vector* B[3];
    gsl_vector* c[3];
    double weight;
};

/************************************************************************
* Global varaiables
************************************************************************/

/* Array for storing all lidar points */
struct LidarPoint g_points[MAX_NUMBER_POINTS];

struct LidarPoint g_pointsOptimized[MAX_NUMBER_POINTS];

int g_numberOptimizationsPerPoint[MAX_NUMBER_POINTS];

/* The first point within the points variable to be optimized */
//int g_trackStartPoint;

/* The number of points within */
//int g_trackSize;

/* Start point to begin optimization with */
int g_startPoint;

/* Stop point to end optimization with */
int g_stopPoint;

/* The actual number of points read in from the files */
int g_actual_number_points;


void printPoint(int i)
{
    printf("point %d: %f %f %f %f\n",
           i,
           g_points[i].time,
           g_points[i].scpos[0],
           g_points[i].scpos[1],
           g_points[i].scpos[2]
        );
}


void printpt(const char* str, const double* pt)
{
    printf("%s - %f %f %f\n", str, pt[0], pt[1], pt[2]);
}
        

/* Return distance squared between points x and y */
double vdist2(const double x[3], const double y[3])
{
    return ( ( x[0] - y[0] ) * ( x[0] - y[0] )
             + ( x[1] - y[1] ) * ( x[1] - y[1] )
             + ( x[2] - y[2] ) * ( x[2] - y[2] ) );
}


double vdotg ( const double   * v1,
               const double   * v2,
               int        ndim )
{
    double dot = 0.0;
    for ( int i = 0; i < ndim; ++i )
    {
        dot += v1[i] * v2[i];
    }
    return dot;
}
    
void vdotgAdolc ( const adouble   * v1,
                  const double    * v2,
                  int               ndim,
                  adouble         * dot)
{
    *dot = 0.0;
    for ( int i = 0; i < ndim; ++i )
    {
        (*dot) += v1[i] * v2[i];
    }
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
        double x;
        double y;
        double z;
        
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
                   point.rangeStr,
                   &point.scpos[0],
                   &point.scpos[1],
                   &point.scpos[2],
                   &x,
                   &y,
                   &z);

            point.boredir[0] = x - point.scpos[0];
            point.boredir[1] = y - point.scpos[1];
            point.boredir[2] = z - point.scpos[2];
            vhat_c(point.boredir, point.boredir);
            
            utc2et_c(point.utc, &point.time);

            point.range = atof(point.rangeStr);
            
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
/*
double func(const double* coef, void* params)
{
    // First evaluate the splines using the coefficients
    FunctionParams* p = (FunctionParams*)params;

    int startId = p->startPoint;
    int endPoint = p->endPoint;
    double startTime = g_points[startId].time;
    double fitError = 0.0;
    for (int k=0; k<3; ++k)
    {
        const double* startCoef = &coef[k*p->ncoeffsPerDim];
        
        for (int i = startId; i < endPoint; ++i)
        {
            double t = g_points[i].time - startTime;
            gsl_bspline_eval(t, p->B[k], p->bw[k]);

            double valFit = vdotg(startCoef, gsl_vector_const_ptr(p->B[k], 0), p->ncoeffsPerDim);
            double valMeasured = g_points[i].scpos[k];
            double error = valFit - valMeasured;
            fitError += error*error;
        }
    }

    if (p->weight <= 0.0)
        return fitError;
    
    // Next evaluate the difference between the measured range and the
    // computed range based on the position computed from the splines.

    double rangeError = 0.0;
    for (int i = startId, j=0; i < endPoint; ++i,++j)
    {
        
    }

    return fitError + p->weight * rangeError;
}

void grad(const double* coef, double* df, void* params)
{
    // First evaluate the splines using the coefficients
    FunctionParams* p = (FunctionParams*)params;

    int startId = p->startPoint;
    int endPoint = p->endPoint;
    double startTime = g_points[startId].time;

    // Initialize gradient
    for (int i=0; i<3*p->ncoeffsPerDim; ++i)
        df[i] = 0.0;
    
    for (int k=0; k<3; ++k)
    {
        const double* startCoef = &coef[k*p->ncoeffsPerDim];
        double* startDf = &df[k*p->ncoeffsPerDim];
        
        for (int i = startId; i < endPoint; ++i)
        {
            double t = g_points[i].time - startTime;
            gsl_bspline_eval(t, p->B[k], p->bw[k]);

            double valFit = vdotg(startCoef, gsl_vector_const_ptr(p->B[k], 0), p->ncoeffsPerDim);
            double valMeasured = g_points[i].scpos[k];
            double error = valFit - valMeasured;
            for (int j=0; j<p->ncoeffsPerDim; ++j)
                startDf[j] += 2.0 * error * gsl_vector_get(p->B[k], j);
        }
    }

    if (p->weight <= 0.0)
        return;
}
*/

void printTapestats()
{
    int tape_stats[STAT_SIZE];
    tapestats(1, tape_stats);

    fprintf(stdout,"\n    independents            %d\n",tape_stats[NUM_INDEPENDENTS]);
    fprintf(stdout,"    dependents              %d\n",tape_stats[NUM_DEPENDENTS]);
    fprintf(stdout,"    operations              %d\n",tape_stats[NUM_OPERATIONS]);
    fprintf(stdout,"    operations buffer size  %d\n",tape_stats[OP_BUFFER_SIZE]);
    fprintf(stdout,"    locations buffer size   %d\n",tape_stats[LOC_BUFFER_SIZE]);
    fprintf(stdout,"    constants buffer size   %d\n",tape_stats[VAL_BUFFER_SIZE]);
    fprintf(stdout,"    maxlive                 %d\n",tape_stats[NUM_MAX_LIVES]);
    fprintf(stdout,"    valstack size           %d\n\n",tape_stats[TAY_STACK_SIZE]);
}
    
double funcAdolc(const double* coef, void* params)
{
    // First evaluate the splines using the coefficients
    FunctionParams* p = (FunctionParams*)params;
    int N = 3*p->ncoeffsPerDim;

    int startId = p->startPoint;
    int endPoint = p->endPoint;
    double startTime = g_points[startId].time;

    double funcValue = 0.0;

    adouble funcValueA = 0.0;
    adouble fitErrorA = 0.0;
    adouble rangeErrorA = 0.0;
    adouble ptA[3];
    adouble errorA;
    adouble t = 0.0;
    adouble intersectPt[3];
    adouble vec[3];
    adouble dist;
    adouble* coefA = new adouble[N];
    
    trace_on(1);

    for (int i=0; i<N; ++i)
        coefA[i] <<= coef[i];

    for (int i = startId; i < endPoint; ++i)
    {
        struct LidarPoint pt = g_points[i];

        for (int k=0; k<3; ++k)
        {
            const adouble* startCoef = &coefA[k*p->ncoeffsPerDim];
        
            double t = pt.time - startTime;
            gsl_bspline_eval(t, p->B[k], p->bw[k]);

            vdotgAdolc(startCoef, gsl_vector_const_ptr(p->B[k], 0), p->ncoeffsPerDim, &ptA[k]);
            double valMeasured = pt.scpos[k];
            errorA = ptA[k] - valMeasured;
            fitErrorA += errorA*errorA;
        }

        if (p->weight > 0.0)
        {
            // Next evaluate the difference between the measured range and the
            // computed range based on the position computed from the splines.

            double measuredRange = pt.range;
            
            double closestPoint[3];
            double normal[3];
            SpiceBoolean found;

            // Compute intersection with asteroid
            findClosestPointAndNormalDsk(pt.scpos, pt.boredir, closestPoint, normal, &found);

            if (!found)
            {
                // If no intersection penalize by large amount
                rangeErrorA += 2.0 * 2.0;
                continue;
            }
            
            // Form equation of plane along this plate using intersect
            // and normal. Equation of plane is ax + by + cz + d = 0
            // where [a, b, c] is normal vector.
            double a = normal[0];
            double b = normal[1];
            double c = normal[2];
            double d = -a*closestPoint[0] - b*closestPoint[1] - c*closestPoint[2];

            // Now recompute the intersect point using the standard
            // algebraic formula for the intersection of a ray and a
            // plane. By doing it this way, the entire objective
            // function is expressed analytically and it is then easy
            // to use ADOLC to compute a gradient. For the purpose of
            // computing a gradient it is okay to assume that the
            // plate the ray intersects has infinite extent.
            
            t = -(a*ptA[0] + b*ptA[1] + c*ptA[2] + d) /
                (a*pt.boredir[0] + b*pt.boredir[1] + c*pt.boredir[2]);

            intersectPt[0] = ptA[0] + t*pt.boredir[0];
            intersectPt[1] = ptA[1] + t*pt.boredir[1];
            intersectPt[2] = ptA[2] + t*pt.boredir[2];

            // do sanity check to make sure intersectPt is same as closestPoint

            // compute distance between spacecraft position and intersect point.
            vec[0] = intersectPt[0] - ptA[0];
            vec[1] = intersectPt[1] - ptA[1];
            vec[2] = intersectPt[2] - ptA[2];

            dist = vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2];
            dist = sqrt(dist);

            errorA = dist - measuredRange;
            rangeErrorA += errorA*errorA;
        }
    }
    
    funcValueA = (1.0 - p->weight) * fitErrorA + p->weight * rangeErrorA;
    funcValueA >>= funcValue;

    trace_off();
    
    //printTapestats();
    
    delete[] coefA;
    
    return funcValue;
}

void gradAdolc(const double* coef, double* df, void* params)
{
    FunctionParams* p = (FunctionParams*)params;
    int N = 3*p->ncoeffsPerDim;

    funcAdolc(coef, params); // return value not used
    gradient(1,N,coef,df);
}
    
/* do an initial linear least squares fit of a cubic spline to the data */
bool doInitialFit(int startId, int trackSize, FunctionParams* params)
{
    if (trackSize <= 20)
        return false;
    
    int endPoint = startId + trackSize;
    double t0 = 0.0;
    double t1 = g_points[endPoint-1].time - g_points[startId].time;

    /* nbreak = ncoeffs + 2 - k = ncoeffs - 2 since k = 4 */
    const int n = trackSize;
    int ncoeffs = (int)(t1 / 60.0);
    if (ncoeffs < 5)
        ncoeffs = 5;
    if (n < ncoeffs)
        ncoeffs = n;
    cout << "ncoeffs: " << ncoeffs << endl;
    cout << "t0: " << t0 << endl;
    cout << "t1: " << t1 << endl;
    const int nbreak = ncoeffs - 2;
    int i, j, k;
    gsl_bspline_workspace *bw;
    gsl_vector *B;
    gsl_vector *c, *w;
    gsl_vector *x, *y;
    gsl_matrix *X, *cov;
    gsl_multifit_linear_workspace *mw;
    double chisq, Rsq, dof, tss;

    params->startPoint = startId;
    params->endPoint = endPoint;
    params->ncoeffsPerDim = ncoeffs;
    
    for (k=0; k<3; ++k)
    {

        /* allocate a cubic bspline workspace (k = 4) */
        bw = gsl_bspline_alloc(4, nbreak);
        B = gsl_vector_alloc(ncoeffs);

        x = gsl_vector_alloc(n);
        y = gsl_vector_alloc(n);
        X = gsl_matrix_alloc(n, ncoeffs);
        c = gsl_vector_alloc(ncoeffs);
        w = gsl_vector_alloc(n);
        cov = gsl_matrix_alloc(ncoeffs, ncoeffs);
        mw = gsl_multifit_linear_alloc(n, ncoeffs);

        params->bw[k] = bw;
        params->B[k] = B;
        params->c[k] = c;

        printf("Begin initial fit dim %d\n", k);
        /* this is the data to be fitted */
        for (i = startId, j=0; i < endPoint; ++i,++j)
        {
            gsl_vector_set(x, j, g_points[i].time - g_points[startId].time);
            gsl_vector_set(y, j, g_points[i].scpos[k]);
            gsl_vector_set(w, j, 1.0);
        }

        /* use uniform breakpoints on [t0, t1] */
        gsl_bspline_knots_uniform(t0, t1, bw);

        /* construct the fit matrix X */
        for (i = 0; i < n; ++i)
        {
            double xi = gsl_vector_get(x, i);

            /* compute B_j(xi) for all j */
            gsl_bspline_eval(xi, B, bw);

            /* fill in row i of X */
            for (j = 0; j < ncoeffs; ++j)
            {
                double Bj = gsl_vector_get(B, j);
                gsl_matrix_set(X, i, j, Bj);
            }
        }

        /* do the fit */
        gsl_multifit_wlinear(X, w, y, c, cov, &chisq, mw);

        dof = n - ncoeffs;
        tss = gsl_stats_wtss(w->data, 1, y->data, 1, y->size);
        Rsq = 1.0 - chisq / tss;

        fprintf(stderr, "chisq/dof = %e, Rsq = %f\n",
                chisq / dof, Rsq);

        /* output the smoothed curve */
        {
            double xi, yi, yerr, ytrue;

            double meanError = 0.0;
            for (i = startId, j=0; i < endPoint; ++i,++j)
            {
                xi = gsl_vector_get(x, j);
                ytrue = gsl_vector_get(y, j);
                gsl_bspline_eval(xi, B, bw);
                gsl_multifit_linear_est(B, c, cov, &yi, &yerr);

                double error = yi-ytrue;

                if (error < 0.0)
                    error = -error;

                meanError += error;
            }
            printf("mean error: %f\n", meanError / (double)n);
        }
        
        
        //gsl_bspline_free(bw);
        //gsl_vector_free(B);
        gsl_vector_free(x);
        gsl_vector_free(y);
        gsl_matrix_free(X);
        //gsl_vector_free(c);
        gsl_vector_free(w);
        gsl_matrix_free(cov);
        gsl_multifit_linear_free(mw);

    }

    return true;
}

/************************************************************************
* Does the optimization of a single track (Polynomial)
************************************************************************/
void optimizeTrack(int startId, int trackSize, SolverType solverType)
{
    printf("Optimizing track starting at %d with size %d\n\n", startId, trackSize);

    int endPoint = startId + trackSize;
        
    FunctionParams params;
    params.weight = 0.5;
    bool success = doInitialFit(startId, trackSize, &params);
    if (!success)
    {
        for (int i = startId; i < endPoint; ++i)
        {
            g_pointsOptimized[i] = g_points[i];
            g_numberOptimizationsPerPoint[i] = 1;
        }

        return;
    }
    
    // put computed coefficients into single array
    vector<double> coeffs;
    for (int i=0; i<3; ++i)
        for (int j=0; j<params.ncoeffsPerDim; ++j)
            coeffs.push_back(gsl_vector_get(params.c[i],j));
    
    /* Now do full optimization */
//    optimizeGsl(func, grad, &coeffs[0], 3*params.ncoeffsPerDim, &params); 
    optimizeGsl(funcAdolc, gradAdolc, &coeffs[0], 3*params.ncoeffsPerDim, &params); 

    // Now evaluate the splines using the new coefficients
    for (int k=0; k<3; ++k)
    {
        const double* startCoef = &coeffs[k*params.ncoeffsPerDim];
        double startTime = g_points[startId].time;
        
        for (int i = startId; i < endPoint; ++i)
        {
            double t = g_points[i].time - startTime;
            gsl_bspline_eval(t, params.B[k], params.bw[k]);

            double valFit = vdotg(startCoef, gsl_vector_const_ptr(params.B[k], 0), params.ncoeffsPerDim);
            //double valMeasured = g_points[i].scpos[k];
            //double error = valFit - valMeasured;

            g_pointsOptimized[i].scpos[k] = valFit;

            g_numberOptimizationsPerPoint[i] = 1;
        }
    }

    // Free memory
    for (int i=0; i<3; ++i)
    {
        gsl_bspline_free(params.bw[i]);
        gsl_vector_free(params.B[i]);
    }
    
    printf("Finished optimizing track\n\n\n\n");
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
        g_numberOptimizationsPerPoint[i] = 0;
    }
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
        
        //++currentStartPoint;
        currentStartPoint = endPoint;
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

        double targetpos[3];
        targetpos[0] = point.scpos[0] + point.range*point.boredir[0];
        targetpos[1] = point.scpos[1] + point.range*point.boredir[1];
        targetpos[2] = point.scpos[2] + point.range*point.boredir[2];
        
        fprintf(fout, "%d %s %s %8s %.16e %.16e %.16e %.16e %.16e %.16e\n",
                g_numberOptimizationsPerPoint[i],
                point.met,
                point.utc,
                point.rangeStr,
                point.scpos[0],
                point.scpos[1],
                point.scpos[2],
                targetpos[0],
                targetpos[1],
                targetpos[2]);
    }

    fclose ( fout );
}

} // end anonymous namespace

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

    optimizeAllTracks(solverType);
    
    savePointsOptimized(outfile);

    return 0;
}

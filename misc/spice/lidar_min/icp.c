#include <stdio.h>
#include "icp.h"
#include "optimize.h"
#include "SpiceUsr.h"
#include "lbfgs.h"


static void centroid(struct Point points[], int n, double* centroid)
{
    centroid[0] = 0.0;
    centroid[1] = 0.0;
    centroid[2] = 0.0;
    int i;
    for (i=0; i<n; ++i)
    {
        vadd_c(centroid, points[i].p, centroid);
    }

    centroid[0] /= n;
    centroid[1] /= n;
    centroid[2] /= n;
}

static int findClosestPoint(struct Point targets[], int n, struct Point point)
{
    double mindist = 1.0e10;
    int minidx = 0;
    int i;
    for (i=0; i<n; ++i)
    {
        double dist = vdist_c(point.p, targets[i].p);
        if (dist < mindist)
        {
            mindist = dist;
            minidx = i;
        }
    }

    return minidx;
}

static void findAllClosestPoints(struct Point source[], struct Point target[], int n, const double* translation, int corr[])
{
    int i;
    for (i=0; i<n; ++i)
    {
        struct Point s = source[i];
        vadd_c(s.p, translation, s.p);
        corr[i] = findClosestPoint(target, n, s);
    }
}

/* These static variables are used by func since we can't pass them to the function directly */
static struct Point* g_source = 0;
static struct Point* g_target = 0;
static int* correspondences = 0;
static int N;

static double func(const double* translation)
{
    int i;
    double ssd = 0.0;
    for (i=0; i<N; ++i)
    {
        struct Point s = g_source[i];
        struct Point t = g_target[correspondences[i]];

        vadd_c(s.p, translation, s.p);

        double dist = vdist_c(s.p, t.p);
        ssd += dist*dist;
    }

    return ssd;
}

/**
   Perform ICP algorithm on source and target points, both of size
   n. The optimal translation that maps the source points into target
   points is calculated and placed in translation.
 */
void icp(struct Point source[], struct Point target[], int n, double* translation)
{
    /* For the ICP, we do the following:

       First compute an initial transformation between the 2 point sets
       by computing the centroid of each set and translating the source
       by that amount to the target

       Then iterate the following:
       1. For each point in source, find the closest point in target.
       2. Minimize the ssd between these corresponding points
    */

    g_source = source;
    g_target = target;
    N = n;
    int corr[n];
    correspondences = &corr[0];


    translation[0] = translation[1] = translation[2] = 0.0;
    findAllClosestPoints(source, target, n, translation, corr);
    printf("Initial value of objective function with no translation: %f\n",
           func(translation));

    
    struct Point sourceCentroid;
    struct Point targetCentroid;
    centroid(source, n, sourceCentroid.p);
    centroid(target, n, targetCentroid.p);
    translation[0] = targetCentroid.p[0] - sourceCentroid.p[0];
    translation[1] = targetCentroid.p[1] - sourceCentroid.p[1];
    translation[2] = targetCentroid.p[2] - sourceCentroid.p[2];

    /* Now iterate */
    
    int maxIter = 100;
    int i;
    for (i=0;i<maxIter;++i)
    {
        printf("\nStarting iteration %d of ICP\n", i+1);

        findAllClosestPoints(source, target, n, translation, corr);

        double prevssd = func(translation);

        optimizeLbfgs(func, translation, 3);

        double ssd = func(translation);

        if (ssd >= prevssd)
            break;
    }
}

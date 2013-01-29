#include <stdio.h>
#include "icp.h"
#include "optimize.h"
#include "point.h"


/* Return distance squared between points x and y */
static double vdist2(const double x[3], const double y[3])
{
    return ( ( x[0] - y[0] ) * ( x[0] - y[0] )
             + ( x[1] - y[1] ) * ( x[1] - y[1] )
             + ( x[2] - y[2] ) * ( x[2] - y[2] ) );
}

/* Add vectors v1 and v2 and store result in vout */
static void vadd(const double v1[3], const double v2[3], double vout[3])
{
    vout[0] = v1[0] + v2[0];
    vout[1] = v1[1] + v2[1];
    vout[2] = v1[2] + v2[2];
}

static void centroid(struct PointLite points[], int n, double* centroid)
{
    centroid[0] = 0.0;
    centroid[1] = 0.0;
    centroid[2] = 0.0;
    int i;
    for (i=0; i<n; ++i)
    {
        vadd(centroid, points[i].p, centroid);
    }

    centroid[0] /= n;
    centroid[1] /= n;
    centroid[2] /= n;
}

static int findClosestPoint(struct PointLite targets[], int n, struct PointLite point)
{
    double mindist2 = 1.0e20;
    int minidx = 0;
    int i;
    for (i=0; i<n; ++i)
    {
        double dist2 = vdist2(point.p, targets[i].p);
        if (dist2 < mindist2)
        {
            mindist2 = dist2;
            minidx = i;
        }
    }

    return minidx;
}

static void findAllClosestPoints(struct PointLite source[], struct PointLite target[], int n, const double* translation, int corr[])
{
    int i;
    for (i=0; i<n; ++i)
    {
        struct PointLite s = source[i];
        vadd(s.p, translation, s.p);
        corr[i] = findClosestPoint(target, n, s);
    }
}

/* These static variables are used by func since we can't pass them to the function directly */
static struct PointLite* g_source = 0;
static struct PointLite* g_target = 0;
static int* correspondences = 0;
static int N;

static double func(const double* translation, void* notused)
{
    int i;
    double ssd = 0.0;
    for (i=0; i<N; ++i)
    {
        struct PointLite s = g_source[i];
        struct PointLite t = g_target[correspondences[i]];

        vadd(s.p, translation, s.p);

        double dist2 = vdist2(s.p, t.p);
        ssd += dist2;
    }

    return ssd;
}

/**
   Perform ICP algorithm on source and target points, both of size
   n. The optimal translation that maps the source points into target
   points is calculated and placed in translation.
 */
void icp(struct PointLite source[], struct PointLite target[], int n, struct PointLite* additionalPoints)
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

    double translation[3] = {0.0, 0.0, 0.0};
    findAllClosestPoints(source, target, n, translation, corr);
    printf("Initial value of objective function with no translation: %f\n",
           func(translation, NULL));


    struct PointLite sourceCentroid;
    struct PointLite targetCentroid;
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

        double prevssd = func(translation, NULL);

        optimizeLbfgs(func, 0, translation, 3, NULL);

        double ssd = func(translation, NULL);

        if (ssd >= prevssd)
            break;
    }

    for (i=0; i<n; ++i)
    {
        vadd(source[i].p, translation, source[i].p);
        if (additionalPoints != 0)
            vadd(additionalPoints[i].p, translation, additionalPoints[i].p);
    }
}

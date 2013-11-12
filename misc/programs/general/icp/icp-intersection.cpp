#include <stdio.h>
#include <vector>
#include "icp.h"
#include "optimize.h"
#include "optimize-gsl.h"
#include "closest-point-vtk.h"
#include "point.h"


static std::vector<PointLite> g_source;
static std::vector<PointLite> g_target;


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

static void centroid(const std::vector<PointLite>& points, double* centroid)
{
    centroid[0] = 0.0;
    centroid[1] = 0.0;
    centroid[2] = 0.0;
    int n = points.size();
    int i;
    for (i=0; i<n; ++i)
    {
        vadd(centroid, points[i].p, centroid);
    }

    centroid[0] /= n;
    centroid[1] /= n;
    centroid[2] /= n;
}

static void findAllClosestPoints(const double* translation)
{
    g_target.clear();
    int n = g_source.size();
    int found;
    int i;
    for (i=0; i<n; ++i)
    {
        struct PointLite s = g_source[i];
        vadd(s.p, translation, s.p);

        struct PointLite closestPoint;
        findClosestPointVtk(s.p, closestPoint.p, &found);
        g_target.push_back(closestPoint);
    }
}

static double func(const double* translation, void*)
{
    int i;
    int n = g_source.size();
    double ssd = 0.0;
    for (i=0; i<n; ++i)
    {
        struct PointLite s = g_source[i];
        struct PointLite t = g_target[i];

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
void icp2(struct PointLite source[], int n, struct PointLite* additionalPoints, double* translation)
{
    /* For the ICP, we do the following:

       First compute an initial transformation between the 2 point sets
       by computing the centroid of each set and translating the source
       by that amount to the target

       Then iterate the following:
       1. For each point in source, find the closest point in target.
       2. Minimize the ssd between these corresponding points
    */

    g_source.clear();
    g_target.clear();

    // put source into g_source vector
    for (int i=0; i<n; ++i)
        g_source.push_back(source[i]);

    translation[0] = 0.0;
    translation[1] = 0.0;
    translation[2] = 0.0;
    findAllClosestPoints(translation);
    printf("Initial value of objective function with no translation: %f\n",
           func(translation, NULL));


    struct PointLite sourceCentroid;
    struct PointLite targetCentroid;
    centroid(g_source, sourceCentroid.p);
    centroid(g_target, targetCentroid.p);
    translation[0] = targetCentroid.p[0] - sourceCentroid.p[0];
    translation[1] = targetCentroid.p[1] - sourceCentroid.p[1];
    translation[2] = targetCentroid.p[2] - sourceCentroid.p[2];

    /* Now iterate */

    int maxIter = 5000;
    int i;
    for (i=0;i<maxIter;++i)
    {
        printf("\nStarting iteration %d of ICP\n", i+1);

        findAllClosestPoints(translation);

        double prevssd = func(translation, NULL);

        optimizeGsl(func, 0, translation, 3, NULL);
        //optimizeLbfgs(func, 0, translation, 3, NULL);

        double ssd = func(translation, NULL);

        if (ssd >= prevssd)
            break;
    }

    printf("Final value of objective function with optimal translation: %f\n",
           func(translation, NULL));

    for (i=0; i<n; ++i)
    {
        vadd(source[i].p, translation, source[i].p);
        if (additionalPoints != 0)
            vadd(additionalPoints[i].p, translation, additionalPoints[i].p);
    }
}

#ifndef __ICP_INTERSECTION_H__
#define __ICP_INTERSECTION_H__

#include "util.h"
#include "point.h"

/**
 * Run a variant of the Iterative Closest Point algorithm.
 *
 * @param[in,out] source source point for which we want to find closest points on body.
 *                On output, this array contains the translated points
 * @param[in] n number of points in source
 * @param[in,out] additionalPoints once optimal translation is found, translate these additional points using the optimal translation
 * @param[out] translation the optimal translation computed by this function. Assumed to be a 3 element double array.
 * @param[out] closestPoints the point on the surface of the body closest to the translated source points
 */
void icp2(struct PointLite source[], int n, struct PointLite* additionalPoints, double* translation, struct PointLite* closestPoints);


#endif

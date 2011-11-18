#ifndef __CLOSEST_POINT_H__
#define __CLOSEST_POINT_H__

#include "SpiceUsr.h"


void findClosestPointDsk(const double* origin, const double* direction, double* closestPoint, int* found);
void findClosestPointVtk(const double* origin, double* closestPoint, int* found);
void illum_pl02Dsk ( SpiceDouble            et,
                     SpiceDouble            spoint [3],
                     SpiceDouble          * phase,
                     SpiceDouble          * solar,
                     SpiceDouble          * emissn );


#endif

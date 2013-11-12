#ifndef __CLOSEST_POINT_DSK_H__
#define __CLOSEST_POINT_DSK_H__

#include "SpiceUsr.h"


void initializeDsk(const char* dskfile);
void findClosestPointDsk(const double* origin, const double* direction, double* closestPoint, int* found);
void findClosestPointAndNormalDsk(const double* origin, const double* direction, double* closestPoint, double* normal, int* found);
void illum_pl02Dsk ( SpiceDouble            et,
                     SpiceDouble            spoint [3],
                     SpiceDouble          * phase,
                     SpiceDouble          * solar,
                     SpiceDouble          * emissn );


#endif

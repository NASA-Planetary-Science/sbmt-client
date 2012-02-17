#ifndef __CLOSEST_POINT_H__
#define __CLOSEST_POINT_H__

#include "SpiceUsr.h"


#ifdef __cplusplus
extern "C" {
#endif

void initializeDsk(const char* dskfile);
void initializeVtk(const char* dskfile);
void findClosestPointDsk(const double* origin, const double* direction, double* closestPoint, int* found);
void findClosestPointAndNormalDsk(const double* origin, const double* direction, double* closestPoint, double* normal, int* found);
void findClosestPointVtk(const double* origin, double* closestPoint, int* found);
void illum_pl02Dsk ( SpiceDouble            et,
                     SpiceDouble            spoint [3],
                     SpiceDouble          * phase,
                     SpiceDouble          * solar,
                     SpiceDouble          * emissn );


#ifdef __cplusplus
}       // closing brace for extern "C"
#endif


#endif

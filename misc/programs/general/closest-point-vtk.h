#ifndef __CLOSEST_POINT_VTK_H__
#define __CLOSEST_POINT_VTK_H__

#include "SpiceUsr.h"


void initializeVtk(const char* dskfile);
void findClosestPointVtk(const double* origin, double* closestPoint, int* found);
void intersectWithLineVtk(const double* origin, const double* direction, double* closestPoint, int* found);


#endif

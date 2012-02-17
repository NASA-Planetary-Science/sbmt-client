#ifndef GRAVITY2_H
#define GRAVITY2_H

#include <vtkPolyData.h>

vtkPolyData* initializeGravity2(const char* vtkfile);
double getPotential2(const double fieldPoint[3], double* acc);


#endif // GRAVITY2_H

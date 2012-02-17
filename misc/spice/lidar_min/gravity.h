#ifndef GRAVITY_H
#define GRAVITY_H

#include <vtkPolyData.h>

vtkPolyData* initializeGravity(const char* vtkfile);
double getPotential(const double fieldPoint[3], double* acc);


#endif // GRAVITY_H

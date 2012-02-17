#ifndef GRAVITY3_H
#define GRAVITY3_H

#include <vtkPolyData.h>

vtkPolyData* initializeGravity3(const char* vtkfile);
double getPotential3(const double fieldPoint[3], double* acc);


#endif // GRAVITY3_H

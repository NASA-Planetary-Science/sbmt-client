#ifndef GRAVITY_WERNER_H
#define GRAVITY_WERNER_H

#include <vtkPolyData.h>
#include <vtkMath.h>


vtkPolyData* initializeGravityWerner(const char* vtkfile);
double getGravityWerner(const double fieldPoint[3], double* acc);


#endif // GRAVITY_WERNER_H

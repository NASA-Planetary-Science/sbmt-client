#ifndef GRAVITY_H
#define GRAVITY_H

#include <vtkPolyData.h>
#include <vtkMath.h>

vtkPolyData* initializeGravity(const char* vtkfile);
double getGravity(const double fieldPoint[3], double* acc);

inline double getPointGravity(const double fieldPoint[3], double* acc)
{
    double rhat[3] = {fieldPoint[0], fieldPoint[1], fieldPoint[2]};
    double r = vtkMath::Normalize(rhat);

    double r2 = fieldPoint[0]*fieldPoint[0] + fieldPoint[1]*fieldPoint[1] + fieldPoint[2]*fieldPoint[2];

    double potential = 1.0 / r;
    acc[0] = -rhat[0] / r2;
    acc[1] = -rhat[1] / r2;
    acc[2] = -rhat[2] / r2;

    return potential;
}


#endif // GRAVITY_H

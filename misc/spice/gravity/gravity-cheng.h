#ifndef GRAVITY_CHENG_H
#define GRAVITY_CHENG_H

#include "platemodel.h"

Platemodel* initializeGravityCheng(const char* vtkfile);
double getGravityCheng(const double fieldPoint[3], double* acc);


#endif // GRAVITY_CHENG_H

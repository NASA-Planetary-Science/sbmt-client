#ifndef GRAVITY_WERNER_H
#define GRAVITY_WERNER_H

#include "util.h"
#include "platemodel.h"

Platemodel* initializeGravityWerner(const char* vtkfile);
double getGravityWerner(const double fieldPoint[3], double* acc);



#endif // GRAVITY_H

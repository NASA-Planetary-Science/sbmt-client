#ifndef __OPTIMIZE_H__
#define __OPTIMIZE_H__

void optimizeLbfgs(double (*func)(const double* x), double* minimizer, int numVar);

#endif

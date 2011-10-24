#ifndef __OPTIMIZE_H__
#define __OPTIMIZE_H__

/* minimizer is both the initial guess and the final returned value */
void optimizeLbfgs(double (*func)(const double* x), double* minimizer, int numVar);

#endif

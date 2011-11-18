#ifndef __OPTIMIZE_H__
#define __OPTIMIZE_H__

#ifdef __cplusplus
extern "C" {
#endif

/* minimizer is both the initial guess and the final returned value */
void optimizeLbfgs(double (*func)(const double* x), double* minimizer, int numVar);

#ifdef __cplusplus
}       // closing brace for extern "C"
#endif
    
#endif

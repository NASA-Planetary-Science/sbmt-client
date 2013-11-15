#include <stdio.h>
#include "lbfgs.h"


/*-----------------------------------------------------------------------
-------------------------------------------------------------------------
 This file contains LBFGS solver related functions and variables
-------------------------------------------------------------------------
-----------------------------------------------------------------------*/


#define DX 0.00001
static double (*function)(const double* x, void *instance) = 0;
static void (*gradient)(const double* x, double* g, void *instance) = 0;
static int N;


static void printCurrentValue(double fx, const double* x)
{
    printf("  fx = %f", fx);
    int i;
    for (i=0; i<N; ++i)
    {
        printf(", x[%d] = %f", i, x[i]);
    }
    printf("\n");
}


/************************************************************************
* This function numerically computes the gradient of func using finite differences.
* On output, the gradient is stored in g.
************************************************************************/
static void gradientNumeric(const double* coef, double* g, void *instance)
{
    double f = function(coef, instance);

    int i;
    int j;
    for (i=0; i<N; ++i)
    {
        double coef2[N];
        for (j=0; j<N; ++j)
            coef2[j] = coef[j];

        coef2[i] += DX;

        double f2 = function(coef2, instance);

        g[i] = (f2 - f) / DX;
    }
}


static lbfgsfloatval_t evaluate(
    void *instance,
    const lbfgsfloatval_t *x,
    lbfgsfloatval_t *g,
    const int n,
    const lbfgsfloatval_t step
    )
{
    lbfgsfloatval_t fx = function(x, instance);
    if (gradient != 0)
        gradient(x, g, instance);
    else
        gradientNumeric(x, g, instance);

    return fx;
}


static int progress(
    void *instance,
    const lbfgsfloatval_t *x,
    const lbfgsfloatval_t *g,
    const lbfgsfloatval_t fx,
    const lbfgsfloatval_t xnorm,
    const lbfgsfloatval_t gnorm,
    const lbfgsfloatval_t step,
    int n,
    int k,
    int ls
    )
{
    printf("Iteration %d:\n", k);
    printCurrentValue(fx, x);
    printf("  xnorm = %f, gnorm = %f, step = %f\n", xnorm, gnorm, step);
    printf("\n");
    return 0;
}


/** Public function */
void optimizeLbfgs(double (*func)(const double* x, void *externalParams),
                   void (*grad)(const double* x, double* g, void *instance),
                   double* minimizer,
                   int numVar,
                   void *instance)
{
    static lbfgs_parameter_t param;
    static int initialized = 0;
    if (!initialized)
    {
        lbfgs_parameter_init(&param);
        initialized = 1;
    }

    function = func;
    gradient = grad;
    N = numVar;

    lbfgsfloatval_t fx;
    lbfgsfloatval_t *x = lbfgs_malloc(N);

    /* Initialize the variables. */
    int i;
    for (i = 0;i < N;++i)
        x[i] = minimizer[i];

    double value = func(minimizer, instance);
    printf("Initial value of objective function: \n");
    printCurrentValue(value, x);
    printf("\n");

    int ret = lbfgs(N, x, &fx, evaluate, progress, instance, &param);

    /* Report the result. */
    printf("L-BFGS optimization terminated with status code = %d\n", ret);
    printCurrentValue(fx, x);
    printf("\n");

    /* return the minimizer to the calling function */
    for (i = 0;i < N;++i)
        minimizer[i] = x[i];

    lbfgs_free(x);
}

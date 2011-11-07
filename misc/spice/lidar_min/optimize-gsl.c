#include <stdio.h>
#include <gsl/gsl_multimin.h>


/*-----------------------------------------------------------------------
-------------------------------------------------------------------------
 This file contains GSL solver related functions
-------------------------------------------------------------------------
-----------------------------------------------------------------------*/


#define DX 0.00001


struct FuncParam
{
    double (*function)(const double*);
    int N;
};


static void printCurrentValue(int iter, double fx, const gsl_vector* x, int N)
{
    printf("Iter: %d,  fx = %f", iter, fx);
    int i;
    for (i=0; i<N; ++i)
    {
        printf(", x[%d] = %f", i, gsl_vector_get(x, i));
    }
    printf("\n");
}


static double func(const gsl_vector *v, void *params)
{
    struct FuncParam* p = (struct FuncParam*)params;
    return p->function(gsl_vector_const_ptr(v, 0));
}


/************************************************************************
* This function numerically computes the gradient of func using finite differences
************************************************************************/
static void grad(const gsl_vector *v,
                 void *params,
                 gsl_vector *df)
{
    double f = func(v, params);
    struct FuncParam* p = (struct FuncParam*)params;
    int N = p->N;
    
    int i;
    int j;
    for (i=0; i<N; ++i)
    {
        double coef2[N];
        for (j=0; j<N; ++j)
            coef2[j] = gsl_vector_get(v,j);
        
        coef2[i] += DX;

        double f2 = p->function(coef2);

        gsl_vector_set(df,i, (f2 - f) / DX);
    }
}


static void fdf(const gsl_vector *x,
                void *params,
                double *f,
                gsl_vector *df)
{
    *f = func(x, params);
    grad(x, params, df);
}


/*
  Public function
  
  Perform an optimization on provided function with N independent variables.
  minimizer contains the initial guess and on output contains the optimal
  values of the independent function that minimizes the function.
 */
void optimizeGsl(double (*function)(const double*), double* minimizer, int N)
{
    struct FuncParam param;
    param.function = function;
    param.N = N;

    size_t iter = 0;
    int status;

    const gsl_multimin_fdfminimizer_type *T;
    gsl_multimin_fdfminimizer *s;

    gsl_vector *x;
    gsl_multimin_function_fdf my_func;

    my_func.n = N;
    my_func.f = func;
    my_func.df = grad;
    my_func.fdf = fdf;
    my_func.params = &param;

    /* Starting point, x =  */
    x = gsl_vector_alloc (N);
    int i;
    for (i=0;i<N;++i)
        gsl_vector_set (x, i, minimizer[i]);

    T = gsl_multimin_fdfminimizer_conjugate_fr;
    s = gsl_multimin_fdfminimizer_alloc (T, N);

    gsl_multimin_fdfminimizer_set (s, &my_func, x, 0.01, 1e-4);

    printf("Initial value of objective function: \n");
    printCurrentValue(0, function(minimizer), x, N);
    printf("\n");

    do
    {
        iter++;
        status = gsl_multimin_fdfminimizer_iterate (s);

        if (status)
            break;

        status = gsl_multimin_test_gradient (s->gradient, 1e-3);

        if (status == GSL_SUCCESS)
            printf ("Minimum found at:\n");

        printCurrentValue(iter, s->f, s->x, N);
    }
    while (status == GSL_CONTINUE && iter < 100);

    for (i=0;i<N;++i)
        minimizer[i] = gsl_vector_get (s->x, i);

    printf("GSL optimization terminated with status code = %d\n", status);
    printCurrentValue(iter, function(minimizer), s->x, N);
    printf("\n");
    
    gsl_multimin_fdfminimizer_free (s);
    gsl_vector_free (x);
}

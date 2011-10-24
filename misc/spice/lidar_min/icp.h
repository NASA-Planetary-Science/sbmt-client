#ifndef __ICP_H__
#define __ICP_H__

struct Point
{
    double p[3];
};

void icp(struct Point source[], struct Point target[], int n, double* translation);

#endif

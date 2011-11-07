#ifndef __ICP_H__
#define __ICP_H__

#ifdef __cplusplus
extern "C" {
#endif

struct Point
{
    double p[3];
};

void icp(struct Point source[], struct Point target[], int n, struct Point* additionalPoints);

void icpVtk(struct Point source[], struct Point target[], int n, struct Point* additionalPoints);

#ifdef __cplusplus
}       // closing brace for extern "C"
#endif

#endif

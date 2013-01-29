#ifndef __ICP_H__
#define __ICP_H__

#include "point.h"


void icp(struct PointLite source[], struct PointLite target[], int n, struct PointLite* additionalPoints);


#endif

#include <stdio.h>
#include <string.h>
#include "near_ddr.h"
#include "SpiceUsr.h"

/*
   This function computes the position of the sun in the Eros body frame.
   
   Input:
     et:         Ephemeris time
   
   Output:
     sunpos:     The position of the sun in body cooridinates
*/
void getSunPosition(double et, double sunpos[3])
{
	double lt;
	const char* target = "SUN";
	const char* ref = "IAU_EROS";
	const char* abcorr = "LT+S";
	const char* obs = "EROS";

	spkpos_c(target, et, ref, abcorr, obs, sunpos, &lt);

//	printf("Sun position: %f ",sunpos[0]);
//	printf("%f ",sunpos[1]);
//	printf("%f \n",sunpos[2]);
}

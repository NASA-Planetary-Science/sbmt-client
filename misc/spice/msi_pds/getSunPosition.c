#include <stdio.h>
#include <string.h>
#include "near_ddr.h"
#include "spiceFortran.h"


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
	char* target = "SUN";
	char* ref = "IAU_EROS";
	char* abcorr = "LT+S";
	char* obs = "EROS";
	
	spkpos_(target, &et, ref, abcorr, obs, sunpos, &lt,
			strlen(target), strlen(ref), strlen(abcorr), strlen(obs));

	printf("Sun position: %f ",sunpos[0]);
	printf("%f ",sunpos[1]);
	printf("%f \n",sunpos[2]);
}

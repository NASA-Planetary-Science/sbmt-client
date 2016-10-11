#include <stdio.h>
#include <string.h>
#include "near_ddr.h"
#include "SpiceUsr.h"

/*
   This function computes the position of the earth in the Eros body frame.
   
   Input:
     et:         Ephemeris time
   
   Output:
     sunpos:     The position of the sun in body cooridinates
*/
void getEarthPosition(double et, double earthpos[3])
{
	double lt;
	const char* target = "EARTH";
	const char* ref = "IAU_EROS";
	const char* abcorr = "LT+S";
	const char* obs = "EROS";
	
	spkpos_c(target, et, ref, abcorr, obs, earthpos, &lt);
}

#include <stdio.h>
#include <string.h>
#include "near_ddr.h"
#include "SpiceUsr.h"

extern "C"
{
#include "SpiceUsr.h"
}
using namespace std;

/*
   This function computes the position of the spacecraft in the Eros body frame.

   Input:
     et:         Ephemeris time

   Output:
     sunpos:     The position of the sun in body cooridinates
*/
void getScPosition(double et, double scposb[3])
{
	double lt;
	const char* target = "NEAR";
	const char* ref = "IAU_EROS";
	const char* abcorr = "NONE";
	const char* obs = "EROS";
	
	spkpos_c(target, et, ref, abcorr, obs, scposb, &lt);
}

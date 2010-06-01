#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include "near_ddr.h"
#include "spiceFortran.h"



static const int np = NUM_LINE_SAMPLES;
static const int nf = NUM_LINES;
static const int nl = NUM_LAYER;

/*
   This function
   
   Input:
     et:         Ephemeris time
   
   Output:
     scposb:
	 boredir:
	 updir:
   
*/
void getScOrientation(double et, double scposb[3], double boredir[3], double updir[3],
					  double frustum[12])
{
	double lt;
	double i2bmat[3][3];
	double vpxi[3];
	double ci[3];
	double dz,dy,zo,yo;
	int i,j;
	char* target = "NEAR";
	char* ref = "IAU_EROS";
	char* abcorr = "NONE";
	char* obs = "EROS";
	char* frame = "NEAR_MSI";
	
	spkpos_(target, &et, ref, abcorr, obs, scposb, &lt,
			strlen(target), strlen(ref), strlen(abcorr), strlen(obs));

	printf("%f ",scposb[0]);
	printf("%f ",scposb[1]);
	printf("%f \n",scposb[2]);

	pxform_(frame, ref, &et, i2bmat, strlen(frame), strlen(ref));

	dz=2.*0.025753661240/(double)np;
	zo=-0.025753661240;
	dy=2.*0.019744857140/(double)nf;
	yo=-0.019744857140;


	/* First compute the direction of the center pixel */
	vpxi[0] = 1.0;
	vpxi[1] = 0.0;
	vpxi[2] = 0.0;
	vpack_(&vpxi[0], &vpxi[1], &vpxi[2], ci);
	mxv_(i2bmat, ci, boredir);

	/* Then compute the up direction */
	vpxi[0] = 0.0;
	vpxi[1] = 0.0;
	vpxi[2] = 1.0;
	vpack_(&vpxi[0], &vpxi[1], &vpxi[2], ci);
	mxv_(i2bmat, ci, updir);

	/* Now compute the frustum */
	j = 0;
	i = 0;
	vpxi[0] = 1.0;
	vpxi[1] = yo+dy*j;
	vpxi[2] = zo+dz*i;
	vpack_(&vpxi[0], &vpxi[1], &vpxi[2], ci);
	mxv_(i2bmat, ci, &frustum[0]);

	j = 0;
	i = np-1;
	vpxi[0] = 1.0;
	vpxi[1] = yo+dy*j;
	vpxi[2] = zo+dz*i;
	vpack_(&vpxi[0], &vpxi[1], &vpxi[2], ci);
	mxv_(i2bmat, ci, &frustum[3]);

	j = nf-1;
	i = 0;
	vpxi[0] = 1.0;
	vpxi[1] = yo+dy*j;
	vpxi[2] = zo+dz*i;
	vpack_(&vpxi[0], &vpxi[1], &vpxi[2], ci);
	mxv_(i2bmat, ci, &frustum[6]);

	j = nf-1;
	i = np-1;
	vpxi[0] = 1.0;
	vpxi[1] = yo+dy*j;
	vpxi[2] = zo+dz*i;
	vpack_(&vpxi[0], &vpxi[1], &vpxi[2], ci);
	mxv_(i2bmat, ci, &frustum[9]);
}

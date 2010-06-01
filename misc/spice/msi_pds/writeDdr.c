#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include "near_ddr.h"
#include "spiceFortran.h"



#define IDX(np, nf, i, j, k)   ((k * nf + j) * np + i)

static const int np = NUM_LINE_SAMPLES;
static const int nf = NUM_LINES;
static const int nl = NUM_LAYER;

/*
   This function saves a 3D volume to disk consisting of 14 planes or
   layers. These layers are:
   0. Latitude, deg
   1. Longitude, deg
   2. Phase angle, measured against the plate model, deg
   3. Emission angle, measured against the plate model, deg
   4. Incidence angle, measured against the plate model, deg
   5. Spare
   6. Spare
   7. x coordinate of center of pixel, body fixed coordinate system, km
   8. y coordinate of center of pixel, body fixed coordinate system, km
   9. z coordinate of center of pixel, body fixed coordinate system, km
   10. Spare
   11. Spare
   12. Spare
   13. Spare
 
   Input:
     et:         Ephemeris time
     ddrfile:    DDR filename
   
   Output data in data[XX][YY][ZZ]
     XX: 412 / bin           (i)  np: number of pixels
     YY: number of frames    (j)  nf: number of frames
     ZZ: 14                  (k)  nl: number of layers
   
   Output in bin sequential order
     (slowest) ZZ(k) -> YY(j) -> XX(i) (fast)
     idx = (k * nf + j) * np + i;
*/
void writeDdr(double et, char *ddrfile)
{
	FILE *fd;
    int num;
    float *data;              /* Output data buffer */
	double scposb[3];
	double lt;
	double i2bmat[3][3];
	double vpxi[3];
	double ci[3];
	double co[3];
	int i,j,k;
	int plid;
	int body;
	double xyzhit[3];
	int found; /*logical found;*/
	double r;
	double lon;
	double lat;
	double dz,dy,zo,yo;
	double solar;
	double emissn;
	double phase;
	double visamt;
	double litamt;
	int targ;
	int ob;
	char* target = "NEAR";
	char* ref = "IAU_EROS";
	char* abcorr = "NONE";
	char* obs = "EROS";
	char* frame = "NEAR_MSI";
	
	
    /* Dynamic allocate data spaces */
    data = (float *) malloc(sizeof(float) * np * nf * nl);
    if (data == NULL) {
        fprintf(stderr, "ERROR: Failed to allocate data space for %s\n",
                ddrfile);
        exit(1);
    }

    if (NULL == (fd = fopen(ddrfile, "w"))) {
        fprintf(stderr, "ERROR: Failed to open outfile %s.\n", ddrfile);
        exit(1);
    }

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

	for (j=0; j<nf; ++j)
	{
		printf("Row %d\n", j);fflush(0);
		for (i=0; i<np; ++i)
		{
			vpxi[0] = 1.0;
			vpxi[1] = yo+dy*j;
			vpxi[2] = zo+dz*i;
			vpack_(&vpxi[0], &vpxi[1], &vpxi[2], ci);
			mxv_(i2bmat, ci, co);
			plbore_(scposb, co, &plid, &body, xyzhit, &found);
			/*printf("plid, body = %d %d\n", plid, body);*/

			if (found)
			{
				/* Compute LOT/LAT of the intersection point. */
				reclat_(xyzhit, &r, &lon, &lat);
				data[IDX(np, nf, i, j, 0)] = dpr_() * lat;
				data[IDX(np, nf, i, j, 1)] = dpr_() * lon;
				data[IDX(np, nf, i, j, 7)] = xyzhit[0];
				data[IDX(np, nf, i, j, 8)] = xyzhit[1];
				data[IDX(np, nf, i, j, 9)] = xyzhit[2];


				/* Compute solar incidence angle at the intersection point. */
				targ = 10;
				pltang_(&plid, &body,  &targ, &et, "ALL", &solar, 3);
				data[IDX(np, nf, i, j, 4)] = dpr_() * solar;


				/* Compute emission angle at the intersection point. */
				targ = -93;
				pltang_(&plid, &body, &targ, &et, "ALL", &emissn, 3);
				data[IDX(np, nf, i, j, 3)] = dpr_() * emissn;


				/* Compute phase angle at the intersection point. */
				ob = -93;
				plsang_(&plid, &body, &ob, &et, "ALL", &phase, &visamt, &litamt, 3);
				data[IDX(np, nf, i, j, 2)] = dpr_() * phase;

				data[IDX(np, nf, i, j, 5)] = PDS_UNK;
				data[IDX(np, nf, i, j, 6)] = PDS_UNK;
				data[IDX(np, nf, i, j, 10)] = PDS_UNK;
				data[IDX(np, nf, i, j, 11)] = PDS_UNK;
				data[IDX(np, nf, i, j, 12)] = PDS_UNK;
				data[IDX(np, nf, i, j, 13)] = PDS_UNK;
			}
			else
			{
				/* center pointing not found */
				/*fprintf(stderr,
						"WARNING: No pointing data at (%d %d)\n",
						i, j);*/
				for (k = 0; k < nl; k++) {
					data[IDX(np, nf, i, j, k)] = PDS_NA;
				}
			}
		}
	}

	
    /* Write data to output */
    num = fwrite(data, sizeof(float), np * nf * nl, fd);
    if (num != np * nf * nl) {
        fprintf(stderr, "ERROR: fwrite failed (%s: %d).\n", ddrfile, num);
    }
    free(data);

    fclose(fd);
}

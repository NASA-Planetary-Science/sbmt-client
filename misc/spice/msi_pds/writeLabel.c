#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <libgen.h>
#include <time.h>
#include "spiceFortran.h"
#include "near_ddr.h"
#define MAXLEN  128

static const int np = NUM_LINE_SAMPLES;
static const int nf = NUM_LINES;
static const int nl = NUM_LAYER;

void convertString(char* str, int size);


/*
  This function creates a LBL file associated with a given ddrFile
  using the values passed to this function.

  Input:

    ddrFile - the IMG ddr file for which LBL file is being created.
	pname - the name of the program generating this file
         	(near_ddr). This name is recorded in the LBL file.
	pversion - the version of the program generating this file. This
	           version is recorded in the LBL file.
	startEt - the ephemeris start time loaded from the original lblfile
	stopEt - the ephemeris stop time loaded from the original lblfile
	startSC - the spacecraft clock start time loaded from the original lblfile
	stopSC - the spacecraft clock stop time loaded from the original lblfile
	scposb - the spacecraft position
	boredir - the spacecraft boresight direction
	updir - the spacecraft up direction
*/
void writeLabel(char *ddrFile, char *pname, char *pversion,
				char* startEt, char* stopEt,
				char* startSC, char* stopSC,
				double scposb[3], double boredir[3], double updir[3], double frustum[12],
				double sunpos[3])
{
    char lblFile[MAXSTR], productId[40];
    int len;
    FILE *fd;
    char *ptr;
    /*char progName[20], progVersion[10];*/
    time_t timer;
    struct tm *res;
    char prodTime[40]/*, timeStr[40]*/;
    int i;
/*     SpiceInt count;
     SpiceChar kernel[MAXLEN];
     SpiceChar filtyp[MAXLEN];
     SpiceChar source[MAXLEN];
     SpiceInt handle;
     SpiceBoolean found;*/

    int count;
    char kernel[MAXLEN];
	char filtyp[MAXLEN];
    char source[MAXLEN];
    int handle;
    int found;

    /*char utc[40];*/
    /*double state[6], ltime;
	  double zeros[] = { 0.0, 0.0, 0.0 };*/
    /*char *scanModeId;*/
    /*double longitudeRadians, solarLongitude;*/
	/*int prec = 3;*/

	/*
	char* target = "NEAR";
	char* ref = "IAU_EROS";
	char* abcorr = "NONE";
	char* obs = "EROS";
	*/
	
    /* Production Time */
    time(&timer);
    res = gmtime(&timer);
    strftime(prodTime, 20, "%Y-%m-%dT%H:%M:%S", res);

    /* Label file name */
    strcpy(lblFile, ddrFile);
    len = strlen(ddrFile);
    lblFile[len - 3] = 'L';
    lblFile[len - 2] = 'B';
    lblFile[len - 1] = 'L';

    if (NULL == (fd = fopen(lblFile, "w"))) {
	    fprintf(stderr, "ERROR: Failed to open label file %s.\n", lblFile);
	    exit(1);
    }

    /* Product Id */
    strcpy(productId, basename(ddrFile));
    if (NULL == (ptr = strrchr(productId, '.'))) {
	    fprintf(stderr, "ERROR: Invalid Product Id %s\n", productId);
	    exit(1);
    }
    *ptr = '\0';
    fprintf(fd, "PDS_VERSION_ID               = PDS3\r\n");
    /*fprintf(fd,
	    "LABEL_REVISION_NOTE          = \"2004-11-22, S. Slavney (GEO);\r\n");
    fprintf(fd,
	"                                2006-04-05, S. Murchie (JHU/APL);\"\r\n");*/
    fprintf(fd, "\r\n");
    fprintf(fd, "/* DDR Identification */\r\n");
    fprintf(fd, "\r\n");
    fprintf(fd,
	    "INSTRUMENT_HOST_NAME         = \"NEAR EARTH ASTEROID RENDEZVOUS\"\r\n");
    fprintf(fd, "SPACECRAFT_ID                = NEAR\r\n");
    fprintf(fd,
	    "INSTRUMENT_NAME              = \"MULTI-SPECTRAL IMAGER\"\r\n");
    fprintf(fd, "INSTRUMENT_ID                = MSI\r\n");
    fprintf(fd, "TARGET_NAME                  = EROS\r\n");
    fprintf(fd, "PRODUCT_TYPE                 = DDR\r\n");
    fprintf(fd, "PRODUCT_CREATION_TIME        = %s\r\n", prodTime);

    /*et2utc_(&startEt, "ISOC", &prec, utc, 4, 40);
    fprintf(fd, "START_TIME                   = %s\r\n", utc);
    et2utc_(&stopEt, "ISOC", &prec, utc, 4, 40);
    fprintf(fd, "STOP_TIME                    = %s\r\n", utc);*/
    fprintf(fd, "START_TIME                   = %s\r\n", startEt);
    fprintf(fd, "STOP_TIME                    = %s\r\n", stopEt);
    fprintf(fd, "SPACECRAFT_CLOCK_START_COUNT = \"%s\"\r\n", startSC);
    fprintf(fd, "SPACECRAFT_CLOCK_STOP_COUNT  = \"%s\"\r\n", stopSC);
    fprintf(fd, "\r\n");
    /*fprintf(fd, "ORBIT_NUMBER                 = 0\r\n");*/
    fprintf(fd, "SOURCE_PRODUCT_ID            = {\r\n");

    /* Get loaded kernel information */
    ktotal_("ALL", &count, 3);
    for (i = 0; i < count; i++) {
	    kdata_(&i, "ALL", kernel, filtyp, source,
			   &handle, &found,
			   3, MAXLEN, MAXLEN, MAXLEN);

		/* String returned from Fortran seem to be padded with spaces
		   at the end. Therefore replace the first occurance of a
		   space with a null byte so it works with C. */
	    convertString(kernel, MAXLEN);
		
		/*printf("%s",kernel);
		  printf(" %d\n",(int)strlen(kernel));*/

	    if (found && strcmp(filtyp, "META") != 0) {
	        fprintf(fd, "      \"%s\"", basename(kernel));
	        if (i != count - 1) {
		        fprintf(fd, ",\r\n");
	        } else {
		        fprintf(fd, "\r\n");
		        fprintf(fd, "}\r\n");
	        }
	    }
    }

    fprintf(fd, "\r\n");
    fprintf(fd,
	    "PRODUCER_INSTITUTION_NAME    = \"APPLIED PHYSICS LABORATORY\"\r\n");
    fprintf(fd, "SOFTWARE_NAME                = \"%s\"\r\n", pname);
    fprintf(fd, "SOFTWARE_VERSION_ID          = \"%s\"\r\n", pversion);
    fprintf(fd, "\r\n");
    fprintf(fd, "/* DDR Instrument and Observation Parameters */\r\n");
    fprintf(fd, "\r\n");
/*
#if 0
    spkezr_("mars", startEt, "J2000", "NONE", "MRO", state, &ltime);
#endif
	spkezr_(target, &startEt, ref, abcorr, obs, state, &ltime,
			strlen(target), strlen(ref), strlen(abcorr), strlen(obs));
    fprintf(fd, "TARGET_CENTER_DISTANCE       = %f <KM>\r\n",
	    vdist_(state, zeros));
*/
    fprintf(fd, "PIXEL_AVERAGING_WIDTH        = %d\r\n", TOTAL_PIXEL / np);

    fprintf(fd, "\r\n");

	fprintf(fd, "SPACECRAFT_POSITION            = ( %.16f , %.16f , %.16f )\r\n", scposb[0],scposb[1],scposb[2]);
	fprintf(fd, "MSI_BORESIGHT_DIRECTION = ( %.16f , %.16f , %.16f )\r\n", boredir[0],boredir[1],boredir[2]);
	fprintf(fd, "MSI_UP_DIRECTION        = ( %.16f , %.16f , %.16f )\r\n", updir[0],updir[1],updir[2]);
	fprintf(fd, "MSI_FRUSTUM1        = ( %.16f , %.16f , %.16f )\r\n", frustum[0],frustum[1],frustum[2]);
	fprintf(fd, "MSI_FRUSTUM2        = ( %.16f , %.16f , %.16f )\r\n", frustum[3],frustum[4],frustum[5]);
	fprintf(fd, "MSI_FRUSTUM3        = ( %.16f , %.16f , %.16f )\r\n", frustum[6],frustum[7],frustum[8]);
	fprintf(fd, "MSI_FRUSTUM4        = ( %.16f , %.16f , %.16f )\r\n", frustum[9],frustum[10],frustum[11]);

    fprintf(fd, "\r\n");

	fprintf(fd, "SUN_POSITION_LT        = ( %.16f , %.16f , %.16f )\r\n", sunpos[0],sunpos[1],sunpos[2]);

    fprintf(fd, "\r\n");
    fprintf(fd, "\r\n");
    fprintf(fd,
	    "/* This DDR label describes one data file:                               */\r\n");
    fprintf(fd,
	    "/* 1. A multiple-band backplane image file with wavelength-independent,  */\r\n");
    fprintf(fd,
	    "/* spatial pixel-dependent geometric and timing information.             */\r\n");
    fprintf(fd, "\r\n");
    fprintf(fd,
	    "/* See the NEAR Data Products SIS for more detailed description.        */\r\n");
    fprintf(fd, "\r\n");
    fprintf(fd, "OBJECT                       = FILE\r\n");
    fprintf(fd, "  ^IMAGE                     = \"%s.IMG\"\r\n",
	    productId);
    fprintf(fd, "  RECORD_TYPE                = FIXED_LENGTH\r\n");
    fprintf(fd, "  RECORD_BYTES               = %d\r\n", np * 4);
    fprintf(fd, "  FILE_RECORDS               = %d\r\n", nf * nl);
    fprintf(fd, "\r\n");
    fprintf(fd, "  OBJECT                     = IMAGE\r\n");
    fprintf(fd, "    LINES                    = %d\r\n", nf);
    fprintf(fd, "    LINE_SAMPLES             = %d\r\n", np);
    fprintf(fd, "    SAMPLE_TYPE              = PC_REAL\r\n");
    fprintf(fd, "    SAMPLE_BITS              = 32\r\n");
    fprintf(fd, "    BANDS                    = %d\r\n", nl);
    fprintf(fd, "    BAND_STORAGE_TYPE        = BAND_SEQUENTIAL\r\n");
    fprintf(fd,
	    "    BAND_NAME                = (\"Latitude, deg\",\r\n");
    fprintf(fd,
	    "                                \"Longitude, deg\",\r\n");
    fprintf(fd,
	    "                                \"Phase angle, measured against the plate model, deg\",\r\n");
    fprintf(fd,
	    "                                \"Emission angle, measured against the plate model, deg\",\r\n");
    fprintf(fd,
	    "                                \"Incidence angle, measured against the plate model, deg\",\r\n");
    fprintf(fd,
	    "                                \"Approximate ground sample distance of pixel, meters\",\r\n");
    fprintf(fd,
	    "                                \"Shadowed flag, 1=shadowed 0=non-shadowed, computed from plate model\",\r\n");
    fprintf(fd,
	    "                                \"x coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
    fprintf(fd,
	    "                                \"y coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
    fprintf(fd,
	    "                                \"z coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
    fprintf(fd,
	    "                                \"Local solar time of pixel\",\r\n");
    fprintf(fd,
	    "                                \"Slope, relative to equipotential surface\",\r\n");
    fprintf(fd,
	    "                                \"Local G, relative to equipotential surface\",\r\n");
    fprintf(fd, "                                \"Spare\")\r\n");
    fprintf(fd, "\r\n");
    fprintf(fd, "  END_OBJECT                 = IMAGE\r\n");
    fprintf(fd, "\r\n");
    fprintf(fd, "END_OBJECT                   = FILE\r\n");
    fprintf(fd, "\r\n");
    fprintf(fd, "END\r\n");

    fclose(fd);
}

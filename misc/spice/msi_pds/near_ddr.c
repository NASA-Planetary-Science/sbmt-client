#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <libgen.h>
#include "spiceFortran.h"
#include <sys/stat.h> 


double getEt(char *lblfile,
			 char *startEt, char *stopEt,
			 char *startSC, char *stopSC);
void getScOrientation(double et, double scposb[3], double boredir[3], double updir[3], double frustum[12]);
void getSunPosition(double et, double sunpos[3]);
void writeDdr(double et, char *ddrfile);
void writeLabel(char *ddrFile, char *pname, char *pversion,
				char* startEt, char* stopEt,
				char* startSC, char* stopSC,
				double scposb[3], double boredir[3], double updir[3], double frustum[12],
				double sunpos[3]);

/*
  This program generates an IMG volume for each NEAR MSI image. See
  writeDdr.c for a description of the planes in this volume. The IMG
  file is compressed using gzip to save space.
 */
int main(int argc, char** argv)
{
	char* kernels[] = {
		"../../kernels/LSK/NAIF0007.TLS",
/*		"../../kernels/EK/EROS_N2000129_V01.BPE",*/
/*		"../../kernels/EK/EROS_N2000129X_V01.BPE",*/
		"../../kernels/PCK/PCK00007.TPC",
		"../../kernels/PCK/EROSATT_1998329_2001157_V01.BPC",
		"../../kernels/PCK/EROSATT_1999304_2001151.BPC",
		"../../kernels/SCLK/NEAR_171.TSC",
		"../../kernels/IK/GRS12.TI",
		"../../kernels/IK/MSI15.TI",
		"../../kernels/IK/NIS14.TI",
/*		"../../kernels/IK/NLR04.TI",*/
		"../../kernels/IK/XRS12.TI",
		"../../kernels/SPK/NEAR_CRUISE_NAV_V1.BSP",
		"../../kernels/SPK/NEAR_EROSORBIT_NAV_V1.BSP",
		"../../kernels/SPK/NEAR_EROSORBIT_RS_V1.BSP",
		"../../kernels/SPK/NEAR_EROSORBIT_NLR_V1.BSP",
		"../../kernels/SPK/NEAR_EROSLANDED_NAV_V1.BSP",
		"../../kernels/SPK/EROSEPHEM_1999004_2002181.BSP",
		"../../kernels/SPK/EROS80.BSP",
		"../../kernels/SPK/MATH9749.BSP",
		"../../kernels/SPK/DE403S.BSP",
		"../../kernels/SPK/STATIONS.BSP"
	};
	char* platemodel = "../../eros-data/Eros_Dec2006_0.bpe";

	int ii;
	int len;
	double et;
	char ddrFile[256];
	char lblFile[256];
	char startEt[256];
	char stopEt[256];
	char startSC[256];
	char stopSC[256];
	char* progName;
	char* progVersion = "1.0";
	char ckKernel[256];
	char compressCommand[512];
	char compressedFilename[512];
	struct stat stFileInfo;
	double scposb[3];
	double boredir[3];
	double updir[3];
	double frustum[12];
	double sunpos[3];
	int onlyWriteLabel = 1;
	
	if (argc < 3) 
	{
	    fprintf(stderr, "usage: near_ddr CK_KERNEL LBL_FILE [onlyWriteLabel]\n");
	    exit(1);
	}

	if (argc > 3)
		onlyWriteLabel = 1;
	
	progName = basename(argv[0]);
	strncpy(ckKernel, argv[1], 256);

	/* replace the extension with _DDR.IMG*/

	strncpy(lblFile, argv[2], 256);
	
	strncpy(ddrFile, lblFile, 256);
	len = strlen(ddrFile);
	ddrFile[len - 4] = '_';
	ddrFile[len - 3] = 'D';
	ddrFile[len - 2] = 'D';
	ddrFile[len - 1] = 'R';
	ddrFile[len - 0] = '.';
	ddrFile[len + 1] = 'I';
	ddrFile[len + 2] = 'M';
	ddrFile[len + 3] = 'G';
	ddrFile[len + 4] = '\0';

	printf("%s\n",lblFile);
	printf("%s\n",ddrFile);
	printf("%f\n",et);

	/* If the gzipped file already exists, do not overwrite it. */
	strcpy(compressedFilename, ddrFile);
	strcat(compressedFilename, ".gz");

	if (onlyWriteLabel == 0)
	{

		if (stat(compressedFilename, &stFileInfo) == 0)
		{
			printf("%s already exists. Not overwriting\n", compressedFilename);
			exit(0);
		}

	}
	else
	{
		if (stat(compressedFilename, &stFileInfo) != 0)
		{
			printf("%s does not exist. Not creating new label file\n", compressedFilename);
			exit(0);
		}
	}
	
	/*--------------------------------------*/
	/* Load kernels                         */
	/*--------------------------------------*/
	
	for (ii=0; ii < sizeof(kernels)/sizeof(kernels[0]); ++ii)
	{
		printf("%s\n", kernels[ii]);
		furnsh_(kernels[ii], strlen(kernels[ii]));
	}

	/* Load in the ck kernel */
	printf("%s\n", ckKernel);
	furnsh_(ckKernel, strlen(ckKernel));
	
	rdplat_(platemodel, strlen(platemodel));

	
	/*--------------------------------------*/
	/* Get Ephemeris time from LBL file   */
	/*--------------------------------------*/

	et = getEt(lblFile, startEt, stopEt, startSC, stopSC);
	    
	/*--------------------------------------*/
	/* Create IMG file                      */
	/*--------------------------------------*/

	if (onlyWriteLabel == 0)
	{
		writeDdr(et, ddrFile);
	}
	
	/*--------------------------------------*/
	/* Create LBL file                      */
	/*--------------------------------------*/

	getScOrientation(et, scposb, boredir, updir, frustum);
	getSunPosition(et, sunpos);
	
	writeLabel(ddrFile, progName, progVersion,
			   startEt, stopEt,
			   startSC, stopSC,
			   scposb, boredir, updir, frustum,
			   sunpos);

	/*--------------------------------------*/
	/* Compress IMG file to save space      */
	/*--------------------------------------*/

	strcpy(compressCommand, "gzip -f ");
	strcat(compressCommand, ddrFile);
	printf("%s\n",compressCommand);

	if (onlyWriteLabel == 0)
	{
		system(compressCommand);
	}
	
	return 0;
}

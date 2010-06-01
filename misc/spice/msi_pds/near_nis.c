#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <libgen.h>
#include "spiceFortran.h"
#include <sys/stat.h> 
#include <math.h>

const int num_spectra = 292608;
const int num_values_per_row = 292;

float fmodf(float x, float y);
void convertString(char* str, int size);

/* copied from http://www.koders.com/cpp/fid6D2F276098149870E554C056CA804166D026BA0A.aspx?s=tree */
float byteSwapFloat(float l)
{
	union
	{
		unsigned char b[4];
		float f;
	} in, out;

	in.f = l;
	out.b[0] = in.b[3];
	out.b[1] = in.b[2];
	out.b[2] = in.b[1];
	out.b[3] = in.b[0];

	return out.f;
}

/*
  This function converts the times given in the nixdb.fit file to the
  time of the middle of the exposure.

  Inputs:
    met_hi  - this is the first column in the nixdb.fit file
	met_low - this is the second column in the nixdb.fit file
	met_offset_to_middle - this is the fifth column in the nixdb.fit file

  Outputs:
    utc - utc time of middle of the exposure as a string
	dayOfYear - the day of year of the middle of the exposure
	met - the met of the middle of the exposure
*/
void convertMetToUtc(float met_hi,
					 float met_low,
					 float met_offset_to_middle,
					 char utc[256],
					 char dayOfYear[4],
					 double* met)
{
	double et;
	char sclkch[32];
	char* formatC = "ISOC";
	char* formatD = "D";
	int prec = 3;
	int sc = -93;
	char utcDayOfYear[256];
	
	*met = 65536.0*met_hi + fmodf((met_low+65536.0) , 65536.0) + met_offset_to_middle/1000.0;

	if (*met < 122000000.0)
	{
		*met = -999.0;
		return;
	}
	
	sprintf(sclkch, "6/0%d000", (int)(*met));
	
	scs2e_(&sc, sclkch, &et, strlen(sclkch));
	et2utc_(&et, formatC, &prec, utc, strlen(formatC), 256);
	
	convertString(utc, 256);


	
	/* get the day of year */
	et2utc_(&et, formatD, &prec, utcDayOfYear, strlen(formatD), 256);
	
	convertString(utcDayOfYear, 256);

	dayOfYear[0] = utcDayOfYear[5];
	dayOfYear[1] = utcDayOfYear[6];
	dayOfYear[2] = utcDayOfYear[7];
	dayOfYear[3] = '\0';

	/*fprintf(stderr,"%f %f %s ",*met,et,utcDayOfYear);*/

	/*fprintf(stderr," %d\n",*dayOfYear);*/
}


/*
  This function saves a single line of the nixdb.fit file to a single
  text file. In addition the same text is appended to a file
  containing ALL the spectra. Note also that the middle time of the
  exposure is prepended to the column in both utc and met format. Thus
  although the original spectrum has 292 values, this new text file
  has 294 values.

  Inputs:
    fout_all - the file handle of the file containing ALL the spectra as text.
	values - the values of the spectrum
	met - met of the spectrum as computed in the convertMetToUtc function
	utc - utc of the spectrum as computed in the convertMetToUtc function
	dayOfYear - dayOfYear of the spectrum as computed in the convertMetToUtc function
*/
void saveSpectrum(FILE* fout_all, float values[292], double met, char* utc, char* dayOfYear)
{
	FILE *fout_single;
	char output_file[256];
	char mkdir_command[256];
	int j;

	sprintf(mkdir_command, "mkdir -p 2000/%s", dayOfYear);
	sprintf(output_file, "2000/%s/N0%d.NIS", dayOfYear, (int)met);

	system(mkdir_command);

	if(!(fout_single = fopen(output_file, "w")))
		return;

	fprintf(fout_single, "%s %d ", utc, (int)met);
	fprintf(fout_all,    "%s %d ", utc, (int)met);
	
	for (j=0; j<num_values_per_row; ++j)
	{
		fprintf(fout_single, "%.8g", values[j]);
		fprintf(fout_all,    "%.8g", values[j]);
		
		if (j < num_values_per_row-1)
		{
			fprintf(fout_single, " ");
			fprintf(fout_all,    " ");
		}
		else
		{
			fprintf(fout_single, "\n");
			fprintf(fout_all,    "\n");
		}
	}

	fclose(fout_single);
}

/*
  This program reads in the large 300 MB NIS data (nixdb.fit) and
  converts it to text. The middle time of exposure of each spectrum in
  utc and met is prepended to the columns. Thus although each original
  spectrum has 292 values, the new text files have 294 values.
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

	int ii,i,j;
	FILE *fd;
	FILE *fout;
	char fitfile[256];
	char header[14400];
	float value;
	float met_hi;
	float met_low;
	float met_offset_to_middle;
	double met;
	char utc[256];
	int num_spectra_to_skip = 65992;
	float values_per_row[292];
	char dayOfYear[4];

	
	if (argc < 2) 
	{
	    fprintf(stderr, "usage: near_nis path_to_nixdb.fit\n");
	    exit(1);
	}

	/*--------------------------------------*/
	/* Load kernels                         */
	/*--------------------------------------*/
	
	for (ii=0; ii < sizeof(kernels)/sizeof(kernels[0]); ++ii)
	{
		printf("%s\n", kernels[ii]);
		furnsh_(kernels[ii], strlen(kernels[ii]));
	}

	strncpy(fitfile, argv[1], 256);

	if(!(fd = fopen(fitfile, "r")))
		return 1;
	if(!(fout = fopen("nixdb.txt", "w")))
		return 1;

	/* Discard the header (14400 bytes) */
	fread(header, 1, 14400, fd);

	/* Discard the first 65992 spectra since they are from before NEAR reached Eros */
	for (i=0; i<num_spectra_to_skip; ++i)
	{
		for (j=0; j<num_values_per_row; ++j)
			fread((void*)&value, 4, 1, fd);
	}
	
	for (i=num_spectra_to_skip; i<num_spectra; ++i)
	{
		for (j=0; j<num_values_per_row; ++j)
		{
			fread((void*)&value, 4, 1, fd);
			value = byteSwapFloat(value);

			values_per_row[j] = value;
			
			if (j == 0)
			{
				met_hi = value;
			}
			else if (j == 1)
			{
				met_low = value;
			}
			else if (j == 4)
			{
				met_offset_to_middle = value;
				
				convertMetToUtc(met_hi, met_low, met_offset_to_middle, utc, dayOfYear, &met);

				fprintf(stderr,"%d %s\n", (int)met, utc);

				if (met == -999.0)
				{
					fprintf(stderr, "There was a problem reading the NIS data.\n");
					abort();
				}
			}
		}
		
		/* Save this spectrum out to a separate file and append it to
		 * the file containing all spectra */
		 saveSpectrum(fout, values_per_row, met, utc, dayOfYear);
		
	}

	fclose(fd);
	fclose(fout);
	
	return 0;
}

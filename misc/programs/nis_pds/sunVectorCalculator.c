#include <stdlib.h>
#include <stdio.h>
#include <string>
#include <sys/stat.h>
#include "SpiceUsr.h"

using namespace std;
#define MAX_LINE_LEN 1000

void getTargetBodyPosition(double et, const char* observerBody, const char* targetBody, double targetpos[3]);

/*
  This program calculates the body to Sun vector at the UTC times in the input file.
  The input file format is two columns, space-delimited, where the first column is
  ignored and second column contains the UTC time string. E.g.

	011/N0122960855.NIS 2000-01-11T00:30:30.085
	011/N0122960873.NIS 2000-01-11T00:30:48.085
	011/N0122960891.NIS 2000-01-11T00:31:06.085
	...
 */
int main(int nargs, char** argv)
{
    char utc[25];
    SpiceDouble sunpos[3];
    SpiceDouble et;
    FILE *fin;
    FILE *fout;
    char junk[ MAX_LINE_LEN ];
    char time[ MAX_LINE_LEN ];

	if (nargs < 4)
	{
	    fprintf(stderr, "\nThis program outputs the sun position at the times read in from file.\n");
	    fprintf(stderr, "\nThe input times file format is space-delimited, first column is \n");
	    fprintf(stderr, "\nignored, second column is UTC time string.\n");
	    fprintf(stderr, "usage: sunVectorCalculator <body> <spacecraft> <metaKernel> <timesFile>\n");
	    fprintf(stderr, "       body       - IAU name of body in uppercase\n");
	    fprintf(stderr, "       metaKernel - file name of SPICE metakernel\n");
	    fprintf(stderr, "       timesFile  - input times file name\n");
	    fprintf(stderr, "example:\n");
	    fprintf(stderr, "       sunVectorCalculator EROS kernelsEros.tm nisTimes.txt\n");
	    exit(1);
	}

	const char* body = argv[1];
    const char* metakernel = argv[2];
    const char* inputFile = argv[3];
    string fname = body + string("_sunVectors.txt");

	/*--------------------------------------*/
	/* Load kernels                         */
	/*--------------------------------------*/

	furnsh_c(metakernel);

	/*--------------------------------------*/
	/* Prepare input and output files.      */
	/*--------------------------------------*/

    if((fin = fopen(inputFile, "r"))==NULL)
    {
        printf("ERROR: Failed to open file %s.\n", inputFile);
        return 1;
    }

	if (NULL == (fout = fopen(fname.c_str(), "wb"))) {
		printf("ERROR: Failed to open output file %s.\n", fname.c_str());
        exit(1);
	}
    fprintf(fout, "#UTC, Sun x, Sun y, Sun z\n");

	/*--------------------------------------*/
	/* For each time, calculate sun vector. */
	/*--------------------------------------*/

	while (fscanf(fin, "%s %s", junk, time) != EOF)
	{
		str2et_c (time, &et);
		getTargetBodyPosition(et, body, "SUN", sunpos);
	    et2utc_c(et, "ISOC", 3, 25, utc);

		fprintf(fout, "%s, %2.10e, %2.10e, %2.10e\n", utc, sunpos[0], sunpos[1], sunpos[2]);
	}

    fclose(fin);
    fclose(fout);

	printf("Output written to %s\n\n", fname.c_str());
	return 0;
}

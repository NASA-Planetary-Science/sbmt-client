#include <stdlib.h>
#include <stdio.h>
#include "SpiceUsr.h"


/************************************************************************
* Constants
************************************************************************/
#define LINE_SIZE 1024


/************************************************************************
* This program takes raw lidar data (i.e. a file only containing times
* in UTC and range) as well as a SPICE metakernel file and computes a
* new file which contains the actual target point of the lidar in body
* fixed coordinates.
*
* In particular, this program assumes an input file
* with the following format:
* 1. MET
* 2. UTC
* 3. range
* 4. remainder of line is ignored
*
* The output file will contain the following fields:
* 1. MET
* 2. UTC
* 3. range
* 4. x target position in body fixed coordinates
* 5. y target position in body fixed coordinates
* 6. z target position in body fixed coordinates
* 7. x spacecraft position in body fixed coordinates
* 8. y spacecraft position in body fixed coordinates
* 9. z spacecraft position in body fixed coordinates
************************************************************************/
int main(int argc, char** argv)
{
    const char* kernelfiles;
    const char* inputfile;
    const char* outputfile;
    FILE* fin;
    FILE* fout;
    const char* scname = "HAYABUSA";
    const char* ref = "IAU_ITOKAWA";
    const char* abcorr = "NONE";
    const char* bodyname = "ITOKAWA";
    const char* instrumentframe = "HAYABUSA_AMICA";
    char line[LINE_SIZE];
    char met[LINE_SIZE];
    char utc[LINE_SIZE];
    double range;
    char restOfLine[LINE_SIZE];
    double et;
    double scposb[3];
    double boredir[3];
    double lt;
    double i2bmat[3][3];
    double vpxi[3] = {0.0, 0.0, 1.0};
    double ci[3];
    double targetpos[3];

    if (argc < 4)
    {
        printf("Usage: lidar-save-min-icp <kernelfiles> <inputfile> <outputfile>\n");;
        return 1;
    }

    kernelfiles = argv[1];
    inputfile = argv[2];
    outputfile = argv[3];

    /* Load in SPICE metakernel */
    furnsh_c(kernelfiles);

    /* Open the input file */
    fin = fopen(inputfile, "r");
    if (fin == NULL)
    {
        printf("Could not open %s", inputfile);
        exit(1);
    }

    /* Open the output file */
    fout = fopen(outputfile, "w");
    if (fout == NULL)
    {
        printf("Could not open %s", outputfile);
        exit(1);
    }

    while ( fgets ( line, LINE_SIZE, fin ) != NULL ) /* read a line */
    {
        sscanf(line, "%s %s %lf %s",
               met,
               utc,
               &range,
               restOfLine);

        /* Convert UTC to ephemeris time */
        utc2et_c(utc, &et);

        /* Get spacecraft position */
        spkpos_c(scname, et, ref, abcorr, bodyname, scposb, &lt);
        if (failed_c())
            return 1;

        /* Compute boresite direction */
        pxform_c(instrumentframe, ref, et, i2bmat);
        if (failed_c())
            return 1;
        vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
        mxv_c(i2bmat, ci, boredir);

        /* compute the target position */
        targetpos[0] = scposb[0] + range*boredir[0];
        targetpos[1] = scposb[1] + range*boredir[1];
        targetpos[2] = scposb[2] + range*boredir[2];

        /* Write out the line to the output file */
        fprintf(fout, "%s %s %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f\r\n",
                met,
                utc,
                range,
                scposb[0], scposb[1], scposb[2],
                targetpos[0], targetpos[1], targetpos[2]
            );
    }

    /* Close open files */
    fclose (fin);
    fclose (fout);

    return 0;
}

#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include "ola-common.h"


/************************************************************************
* This program takes OLA Level 1 science data and also SPICE data and
* outputs a file which can be used as input to the NAIF mksopck program for
* generation of a CK kernel file. Refer to the relevant ICD document
* for descriptions of these formats and the SPICE documentation of the
* msopck program.
************************************************************************/


/************************************************************************
* Constants
************************************************************************/

#define MET_SIZE_BYTES 18 /* does not include null terminating character */
#define UTC_SIZE_BYTES 24 /* does not include null terminating character */

int main(int argc, char** argv)
{
    const char* inputfile;
    const char* outputfile;
    FILE* fin;
    FILE* fout;
    struct Level1Record level1Record;
    int status;

    if (argc < 3)
    {
        printf("Usage: ./ola-test-level1 <inputfile> <outputfile>\n");
        return 1;
    }

    inputfile = argv[1];
    outputfile = argv[2];

    /* Open the input file */
    fin = fopen(inputfile, "r");
    if (fin == NULL)
    {
        printf("Could not open %s\n", inputfile);
        exit(1);
    }

    /* Open the output file */
    fout = fopen(outputfile, "w");
    if (fout == NULL)
    {
        printf("Could not open %s\n", outputfile);
        exit(1);
    }

    for ( ;; ) /* loop until we break out */
    {
        /* Read in level 2 record */
        status = readLevel1Record(fin, &level1Record);
        if (status != 0)
            break;

        /* save out next lidar position in km*/
        status = fprintf(fout, "%s %d %d %d %g %g %g %g %g\n",
                         level1Record.met,
                         level1Record.laser_selection,
                         level1Record.scan_mode,
                         level1Record.flag_status,
                         level1Record.range,
                         level1Record.azimuth,
                         level1Record.elevation,
                         level1Record.intensity_t0,
                         level1Record.intensity_trr);

        if (status < 0)
            break;
    }

    /* Close open files */
    fclose (fin);
    fclose (fout);

    return 0;
}

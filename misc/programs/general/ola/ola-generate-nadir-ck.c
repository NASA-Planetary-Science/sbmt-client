#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include "SpiceUsr.h"
#include "SpicePln.h"


/************************************************************************
* This program takes OLA Level 1 science data (for timing info only) and also SPICE data and
* outputs attitude data of spacecraft in a file which can be used as input to msopck
* to generate ck kernel of spacecraft. This program is not meant to be delivered to SPOC
* but is to be used for testing so we have a CK file to work with.
************************************************************************/


/************************************************************************
* Constants
************************************************************************/

#define MET_SIZE_BYTES 18 /* does not include null terminating character */

struct Level1Record
{
    char met[MET_SIZE_BYTES+1]; /* add 1 to include null terminating character */
    uint16_t laser_selection;
    uint16_t scan_mode;
    uint16_t flag_status;
    double range;
    double azimuth;
    double elevation;
    double intensity_t0;
    double intensity_trr;
};


/**
 * This function parses the rawdata and puts it into a Level1Record structure.
 *
 * @param fin (input) level 1 file stream pointer
 * @param level1Record (output) structure filled in by this function
 *                     containing values parsed from the level 1 file
 * @return 0 if parsed successfully, 1 otherwise
 */
int parseLevel1Record(FILE* fin, struct Level1Record* level1Record)
{
    if (fread ( &level1Record->met, MET_SIZE_BYTES, 1, fin ) != 1)
        return 1;
    if (fread ( &level1Record->laser_selection, 2, 1, fin ) != 1)
        return 1;
    if (fread ( &level1Record->scan_mode, 2, 1, fin ) != 1)
        return 1;
    if (fread ( &level1Record->flag_status, 2, 1, fin ) != 1)
        return 1;
    if (fread ( &level1Record->range, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level1Record->azimuth, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level1Record->elevation, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level1Record->intensity_t0, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level1Record->intensity_trr, 8, 1, fin ) != 1)
        return 1;

    /* Set null terminating character of met string */
    level1Record->met[MET_SIZE_BYTES] = 0;

    return 0;
}

/**
 * This function uses the time found in the level 1 data to compute an orientation matrix of the spacecraft.
 *
 * @param level1Record (input) The level 1 data
 * @param matrix (output) The orientation matrix
 * @return 0 if converted successfully, 1 otherwise
 */
int convertLevel1ToMatrix(const struct Level1Record* level1Record, double m[3][3])
{
    const char* scname = "ORX";
    const char* ref = "J2000";
    const char* abcorr = "NONE";
    const char* bodyname = "BENNU";
    double starg[6];
    double velocityPoint[3];
    double z[3];
    double y[3];
    double x[3];
    double lt;
    int found;
    int id;
    double et;
    struct _SpicePlane plane;

    /* Convert met to ephemeris time */
    bodn2c_c(scname, &id, &found);
    if (found == 0)
        return 1;
    scs2e_c(id, level1Record->met, &et);

    /* Get spacecraft position */
    spkezr_c(scname, et, ref, abcorr, bodyname, starg, &lt);
    if (failed_c())
        return 1;

    /* z axis points from spacecraft to center of asteroid */
    z[0] = -starg[0];
    z[1] = -starg[1];
    z[2] = -starg[2];
    vhat_c(z, z);

    /* y axis is projection of velocity vector into plane normal to z axis */

    /* first create a point that lies along the velocity vector */
    velocityPoint[0] = starg[0] + starg[3];
    velocityPoint[1] = starg[1] + starg[4];
    velocityPoint[2] = starg[2] + starg[5];

    /* then project it into plane normal to z axis */
    nvp2pl_c(z, starg, &plane);
    vprjp_c(velocityPoint, &plane, y);
    y[0] = y[0] - starg[0];
    y[1] = y[1] - starg[1];
    y[2] = y[2] - starg[2];
    /* normalize it */
    vhat_c(y, y);

    /* find x as cross product of y and z */
    ucrss_c(y, z, x);

    m[0][0] = x[0];
    m[0][1] = x[1];
    m[0][2] = x[2];
    m[1][0] = y[0];
    m[1][1] = y[1];
    m[1][2] = y[2];
    m[2][0] = z[0];
    m[2][1] = z[1];
    m[2][2] = z[2];

    return 0;
}


int main(int argc, char** argv)
{
    const char* kernelfiles;
    const char* inputfile;
    const char* outputfile;
    const char* labelfile;
    FILE* fin;
    FILE* fout;
    struct Level1Record level1Record;
    int status;
    double matrix[3][3];

    if (argc < 4)
    {
        printf("Usage: ./ola-generate-ck <kernelfiles> <inputfile> <outputfile> <labelfile>\n");;
        return 1;
    }

    kernelfiles = argv[1];
    inputfile = argv[2];
    outputfile = argv[3];
    labelfile = argv[4];

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

    for ( ;; ) /* loop until we break out */
    {
        /* Read in level 1 record */
        status = parseLevel1Record(fin, &level1Record);
        if (status != 0)
            break;

        /* Convert level 1 record to level 2 */
        status = convertLevel1ToMatrix(&level1Record, matrix);
        if (status != 0)
            break;

        /* save out next orientation to output file */
        status = fprintf(
                    fout, "%s %.16e %.16e %.16e %.16e %.16e %.16e %.16e %.16e %.16e\n",
                    level1Record.met,
                    matrix[0][0],
                matrix[0][1],
                matrix[0][2],
                matrix[1][0],
                matrix[1][1],
                matrix[1][2],
                matrix[2][0],
                matrix[2][1],
                matrix[2][2]);
        if (status < 0)
            break;
    }

    /* Close open files */
    fclose (fin);
    fclose (fout);

    return 0;
}

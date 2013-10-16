#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include "SpiceUsr.h"


/************************************************************************
* This program takes OLA Level 1 science data and also SPICE data and
* outputs OLA Level 2 science data. Refer to the relevant ICD document
* for descriptions of these formats.
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

struct Level2Record
{
    char met[MET_SIZE_BYTES+1]; /* add 1 to include null terminating character */
    char utc[25];
    double et;
    double x;
    double y;
    double z;
    double elongitude;
    double latitude;
    double radius;
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
 * The level 1 science data is stored as follows where each character represents one byte:
 *
 * MMMMMMMMMMMMMMMMMMLLCCFFRRRRRRRRAAAAAAAAEEEEEEEEIIIIIIIIiiiiiiii
 *
 * where:
 *
 * MMMMMMMMMMMMMMMMMM - met (18 byte string)
 * LL - laser selection (2 byte integer)
 * CC - scan mode (2 byte integer)
 * FF - flag status (2 byte integer)
 * RRRRRRRR - range (8 byte double)
 * AAAAAAAA - azimuth (8 byte double)
 * EEEEEEEE - elevation (8 byte double)
 * IIIIIIII - intensity T0 (8 byte double)
 * iiiiiiii - intensity TRr (8 byte double)
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
 * This function converts a Level1Record to a Level2Record using the SPICE kernel
 * files.
 *
 * * The level 2 science data is stored as follows where each character represents one byte:
 *
 * MMMMMMMMMMMMMMMMMMXXXXXXXXYYYYYYYYZZZZZZZZNNNNNNNNTTTTTTTTDDDDDDDDLLCCFFRRRRRRRRAAAAAAAAEEEEEEEEIIIIIIIIiiiiiiii
 *
 * where:
 *
 * MMMMMMMMMMMMMMMMMM - met (18 byte string)
 * XXXXXXXX - x (8 byte double)
 * YYYYYYYY - y (8 byte double)
 * ZZZZZZZZ - z (8 byte double)
 * NNNNNNNN - E. longitude (8 byte double)
 * TTTTTTTT - latitude (8 byte double)
 * DDDDDDDD - radius (8 byte double)
 * LL - laser selection (2 byte integer)
 * CC - scan mode (2 byte integer)
 * FF - flag status (2 byte integer)
 * RRRRRRRR - range (8 byte double)
 * AAAAAAAA - azimuth (8 byte double)
 * EEEEEEEE - elevation (8 byte double)
 * IIIIIIII - intensity T0 (8 byte double)
 * iiiiiiii - intensity TRr (8 byte double)

 *
 * @param level1Record (input) The level 1 data
 * @param level2Record (output) The level 2 data
 * @return 0 if converted successfully, 1 otherwise
 */
int convertLevel1ToLevel2(const struct Level1Record* level1Record, struct Level2Record* level2Record)
{
    const char* scname = "ORX";
    const char* ref = "IAU_BENNU";
    const char* abcorr = "NONE";
    const char* bodyname = "BENNU";
    const char* instrumentframe = "ORX_NADIR";
    double scposb[3];
    double boredir[3];
    double lt;
    double i2bmat[3][3];
    double vpxi[3] = {0.0, 0.0, 1.0};
    double ci[3];
    double targetpos[3];
    int found;
    int id;

    /* Convert met to ephemeris time */
    strncpy(level2Record->met, level1Record->met, MET_SIZE_BYTES+1);
    bodn2c_c(scname, &id, &found);
    if (found == 0)
        return 1;
    scs2e_c(id, level2Record->met, &level2Record->et);

    /* convert ephemeris time to UTC */
    et2utc_c(level2Record->et, "ISOC", 4, 25, level2Record->utc);

    /* Get spacecraft position */
    spkpos_c(scname, level2Record->et, ref, abcorr, bodyname, scposb, &lt);
    if (failed_c())
        return 1;
    /* convert from km to meters */
    scposb[0] *= 1000.0;
    scposb[1] *= 1000.0;
    scposb[2] *= 1000.0;

    /* Compute boresite direction */
    pxform_c(instrumentframe, ref, level2Record->et, i2bmat);
    if (failed_c())
        return 1;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, boredir);

    /* compute the target position */
    level2Record->range = level1Record->range;
    targetpos[0] = scposb[0] + 1.0e-3*level2Record->range*boredir[0];
    targetpos[1] = scposb[1] + 1.0e-3*level2Record->range*boredir[1];
    targetpos[2] = scposb[2] + 1.0e-3*level2Record->range*boredir[2];
    level2Record->x = targetpos[0];
    level2Record->y = targetpos[1];
    level2Record->z = targetpos[2];

    /* convert to latitudinal coordinates */
    reclat_c(targetpos, &level2Record->radius, &level2Record->elongitude, &level2Record->latitude);
    /* convert to degrees */
    level2Record->elongitude *= dpr_c();
    /* convert to degrees and east longitude*/
    level2Record->latitude *= dpr_c();
    level2Record->latitude += 180.0;
    /* convert radius from meters to km */
    level2Record->radius *= 1.0e-3;

    level2Record->laser_selection = level1Record->laser_selection;
    level2Record->scan_mode       = level1Record->scan_mode;
    level2Record->flag_status     = level1Record->flag_status;
    level2Record->azimuth         = level1Record->azimuth;
    level2Record->elevation       = level1Record->elevation;
    level2Record->intensity_t0    = level1Record->intensity_t0;
    level2Record->intensity_trr   = level1Record->intensity_trr;

    return 0;
}

int main(int argc, char** argv)
{
    const char* kernelfiles;
    const char* inputfile;
    const char* outputfile;
    FILE* fin;
    FILE* fout;
    struct Level1Record level1Record;
    struct Level2Record level2Record;
    int status;

    if (argc < 4)
    {
        printf("Usage: ./ola-level1-to-level2 <kernelfiles> <inputfile> <outputfile>\n");;
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

    for ( ;; ) /* loop until we break out */
    {
        /* Read in level 1 record */
        status = parseLevel1Record(fin, &level1Record);
        if (status != 0)
            break;

        /* Convert level 1 record to level 2 */
        status = convertLevel1ToLevel2(&level1Record, &level2Record);
        if (status != 0)
            break;

        /* save out the level 2 record */
        status = fwrite(&level2Record, sizeof(level2Record), 1, fout);
        if (status != 1)
            break;
    }

    /* Close open files */
    fclose (fin);
    fclose (fout);

    return 0;
}

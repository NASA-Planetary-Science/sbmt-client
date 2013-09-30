#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include "SpiceUsr.h"


/************************************************************************
* This program takes OLA Level 1 science data and also SPICE data and
* outputs OLA Level 2 science data. Refer to the relevant ICD document
* for descriptions of this format.
************************************************************************/


/************************************************************************
* Constants
************************************************************************/

#define LEVEL1_RECORD_SIZE_BYTES 16

#define L1_MET_OFFSET 0
#define L1_MET_LENGTH 40
#define L1_LASER_SELECTION_OFFSET (L1_MET_OFFSET+L1_MET_LENGTH)
#define L1_LASER_SELECTION_LENGTH 1
#define L1_SCAN_MODE_OFFSET (L1_LASER_SELECTION_OFFSET+L1_LASER_SELECTION_LENGTH)
#define L1_SCAN_MODE_LENGTH 4
#define L1_FLAG_STATUS_OFFSET (L1_SCAN_MODE_OFFSET+L1_SCAN_MODE_LENGTH)
#define L1_FLAG_STATUS_LENGTH 2
#define L1_RANGE_OFFSET (L1_FLAG_STATUS_OFFSET+L1_FLAG_STATUS_LENGTH)
#define L1_RANGE_LENGTH 24
#define L1_AZIMUTH_OFFSET (L1_RANGE_OFFSET+L1_RANGE_LENGTH)
#define L1_AZIMUTH_LENGTH 14
#define L1_ELEVATION_OFFSET (L1_AZIMUTH_OFFSET+L1_AZIMUTH_LENGTH)
#define L1_ELEVATION_LENGTH 14
#define L1_INTENSITY_T0_OFFSET (L1_ELEVATION_OFFSET+L1_ELEVATION_LENGTH)
#define L1_INTENSITY_T0_LENGTH 14
#define L1_INTENSITY_TRR_OFFSET (L1_INTENSITY_T0_OFFSET+L1_INTENSITY_T0_LENGTH)
#define L1_INTENSITY_TRR_LENGTH 14

struct Level1Record
{
    uint64_t met;
    uint8_t laser_selection;
    uint8_t scan_mode;
    uint8_t flag_status;
    uint32_t range;
    uint16_t azimuth;
    uint16_t elevation;
    uint16_t intensity_t0;
    uint16_t intensity_trr;
};

struct Level2Record
{
    uint64_t met;
    char utc[25];
    double et;
    double x;
    double y;
    double z;
    double elongitude;
    double latitude;
    double radius;
    uint8_t laser_selection;
    uint8_t scan_mode;
    uint8_t flag_status;
    double range;
    float azimuth;
    float elevation;
    uint16_t intensity_t0;
    uint16_t intensity_trr;
};


/**
 * This function parses the rawdata and puts it into a Level1Record structure.
 *
 * The level 1 science data is stored as follows:
 *
 * MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMLCCCCFFRRRRRRRRRRRRRRRRRRRRRRRRAAAAAAAAAAAAAAEEEEEEEEEEEEEEIIIIIIIIIIIIIIiiiiiiiiiiiiii
 *
 * where:
 *
 * MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM - met (40 bits)
 * L - laser selection (1 bit)
 * CCCC - scan mode (4 bits)
 * FF - flag status (2 bits)
 * RRRRRRRRRRRRRRRRRRRRRRRR - range (24 bits)
 * AAAAAAAAAAAAAA - azimuth (14 bits)
 * EEEEEEEEEEEEEE - elevation (14 bits)
 * IIIIIIIIIIIIII - intensity T0 (14 bits)
 * iiiiiiiiiiiiii - intensity TRr (14 bits)
 *
 * @param rawdata (input) The raw data loaded from the file
 * @param Level1Record (output) structure filled in by this function
 *                     containing values parsed from the input
 */
void parseLevel1Record(const char* rawdata, struct Level1Record* level1Record)
{
    int recordoffset = 0;
    int fieldoffset = 0;
    int bitvalue = 0;
    int i, j;

    level1Record->met = 0;
    level1Record->laser_selection = 0;
    level1Record->scan_mode = 0;
    level1Record->flag_status = 0;
    level1Record->range = 0;
    level1Record->azimuth = 0;
    level1Record->elevation = 0;
    level1Record->intensity_t0 = 0;
    level1Record->intensity_trr = 0;

    for (i=0; i<LEVEL1_RECORD_SIZE_BYTES; ++i)
    {
        for (j=0; j<8; ++j)
        {
            bitvalue = 1 & (rawdata[i] >> j);
            if (bitvalue == 1)
            {
                if (recordoffset < L1_MET_OFFSET+L1_MET_LENGTH)
                {
                    fieldoffset = recordoffset - L1_MET_OFFSET;
                    level1Record->met |= 1 << fieldoffset;
                }
                else if (recordoffset < L1_LASER_SELECTION_OFFSET+L1_LASER_SELECTION_LENGTH)
                {
                    fieldoffset = recordoffset - L1_LASER_SELECTION_OFFSET;
                    level1Record->laser_selection |= 1 << fieldoffset;
                }
                else if (recordoffset < L1_SCAN_MODE_OFFSET+L1_SCAN_MODE_LENGTH)
                {
                    fieldoffset = recordoffset - L1_SCAN_MODE_OFFSET;
                    level1Record->scan_mode |= 1 << fieldoffset;
                }
                else if (recordoffset < L1_FLAG_STATUS_OFFSET+L1_FLAG_STATUS_LENGTH)
                {
                    fieldoffset = recordoffset - L1_FLAG_STATUS_OFFSET;
                    level1Record->flag_status |= 1 << fieldoffset;
                }
                else if (recordoffset < L1_RANGE_OFFSET+L1_RANGE_LENGTH)
                {
                    fieldoffset = recordoffset - L1_RANGE_OFFSET;
                    level1Record->range |= 1 << fieldoffset;
                }
                else if (recordoffset < L1_AZIMUTH_OFFSET+L1_AZIMUTH_LENGTH)
                {
                    fieldoffset = recordoffset - L1_AZIMUTH_OFFSET;
                    level1Record->azimuth |= 1 << fieldoffset;
                }
                else if (recordoffset < L1_ELEVATION_OFFSET+L1_ELEVATION_LENGTH)
                {
                    fieldoffset = recordoffset - L1_ELEVATION_OFFSET;
                    level1Record->elevation |= 1 << fieldoffset;
                }
                else if (recordoffset < L1_INTENSITY_T0_OFFSET+L1_INTENSITY_T0_LENGTH)
                {
                    fieldoffset = recordoffset - L1_INTENSITY_T0_OFFSET;
                    level1Record->intensity_t0 |= 1 << fieldoffset;
                }
                else if (recordoffset < L1_INTENSITY_TRR_OFFSET+L1_INTENSITY_TRR_LENGTH)
                {
                    fieldoffset = recordoffset - L1_INTENSITY_TRR_OFFSET;
                    level1Record->intensity_trr |= 1 << fieldoffset;
                }
            }
            ++recordoffset;
        }
    }
}

/**
 * This function converts a Level1Record to a Level2Record using the SPICE kernel
 * files.
 *
 * @param level1Record (input) The level 1 data
 * @param level2Record (output) The level 2 data
 * @return non-zero value if successfully converted, zero otherwise
 */
int convertLevel1ToLevel2(const struct Level1Record* level1Record, struct Level2Record* level2Record)
{
    const char* scname = "ORX";
    const char* ref = "IAU_BENNU";
    const char* abcorr = "NONE";
    const char* bodyname = "BENNU";
    const char* instrumentframe = "ORX_NADIR";
    double etStartOfMission = 0.0;
    double scposb[3];
    double boredir[3];
    double lt;
    double i2bmat[3][3];
    double vpxi[3] = {0.0, 0.0, 1.0};
    double ci[3];
    double targetpos[3];

    /* Convert met to ephemeris time */
    level2Record->et = etStartOfMission + 1.0e-4 * level1Record->met;

    level2Record->met = level1Record->met;
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
    char rawdata[LEVEL1_RECORD_SIZE_BYTES];
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

    while ( fread ( &level1Record, LEVEL1_RECORD_SIZE_BYTES, 1, fin ) != 1 ) /* read a record */
    {
        parseLevel1Record(rawdata, &level1Record);

        status = convertLevel1ToLevel2(&level1Record, &level2Record);

        /* save out the level 2 record */
        if (status == 0)
            fwrite(&level2Record, sizeof(level2Record), 1, fout);
    }

    /* Close open files */
    fclose (fin);
    fclose (fout);

    return 0;
}

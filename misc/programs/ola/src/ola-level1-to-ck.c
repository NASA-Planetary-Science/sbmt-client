#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include "SpiceUsr.h"


/************************************************************************
 * The msopck program distributed by NAIF (http://naif.jpl.nasa.gov/naif/utilities.html)
 * is used by this pipeline to generate CK kernels for the OLA scanning platform.
 * The msopck program requires 2 input files: a setup file and a data file containing
 * orientation data for each time instant. This program generates both of these input files
 * needed by the msopck program.
 *
 * This program should be called in the following manner:
 *
 * ./ola-level1-to-ck <level1-input-data-file> <lsk-kernel-filename> <sclk-kernel-filename> <frames-kernel-filename> <msopck-data-file> <msopck-setup-file>
 *
 * The program takes 6 command line arguments, all of them required, as follows:
 *
 * 1. <level1-input-data-file> the binary level 1 data file which was generated in a
 *                             previous stage of the pipeline
 * 2. <lsk-kernel-filename> the leapsecond kernel file to use by msopck
 * 3. <sclk-kernel-filename> the spacecraft clock kernel file to use by msopck
 * 4. <frames-kernel-filename> the frames kernel file to use by msopck. This file must
 *                             define the ORX_OLA_BASE frame which is used as the reference
 *                             frame by msopck.
 * 5. <level2-output-data-file> name of the msopck data file to generate This file
 *                              contains the orientation of the OLA scan platform in
 *                              the form of X-Y-Z Euler angles where the negative azimuth
 *                              in radians is the X angle, the negative elevation
 *                              in radians is the Y angle and Z angle is set to zero.
 * 6. <msopck-setup-file> name of the msopck setup file to generate
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
 * This function reads the level 1 binary data and puts it into a Level1Record structure.
 *
 * @param[in] fin level 1 file stream pointer to open level 1 data file
 * @param[out] level1Record structure filled in by this function
 *                     containing values read from the level 1 file
 * @return 0 if read successfully, 1 otherwise
 */
int readLevel1Record(FILE* fin, struct Level1Record* level1Record)
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
 * write the msopck setup file
 *
 * @param[in] filename
 * @param[in] lsk_kernel
 * @param[in] sclk_kernel
 * @param[in] frames_kernel
 */
void writeMsopckSetupFile(
        const char* filename,
        const char* lsk_kernel,
        const char* sclk_kernel,
        const char* frames_kernel)
{
    FILE* fout;
    /* Open the output file */
    fout = fopen(filename, "w");
    if (fout == NULL)
    {
        printf("Could not open %s\n", filename);
        exit(1);
    }

    fprintf(fout, "\\begindata\n");
    fprintf(fout, "LSK_FILE_NAME          = '%s'\n", lsk_kernel);
    fprintf(fout, "SCLK_FILE_NAME         = '%s'\n", sclk_kernel);
    fprintf(fout, "FRAMES_FILE_NAME       = '%s'\n", frames_kernel);
    fprintf(fout, "CK_TYPE                = 3\n");
    fprintf(fout, "INSTRUMENT_ID          = -64401\n");
    fprintf(fout, "REFERENCE_FRAME_NAME   = 'ORX_OLA_BASE'\n");
    fprintf(fout, "ANGULAR_RATE_PRESENT   = 'MAKE UP/NO AVERAGING'\n");
    fprintf(fout, "INPUT_TIME_TYPE        = 'SCLK'\n");
    fprintf(fout, "INPUT_DATA_TYPE        = 'EULER ANGLES'\n");
    fprintf(fout, "EULER_ANGLE_UNITS      = 'RADIANS'\n");
    fprintf(fout, "EULER_ROTATIONS_ORDER  = ( 'X' 'Y' 'Z' )\n");
    fprintf(fout, "PRODUCER_ID            = 'JHUAPL'\n");
    fprintf(fout, "\\begintext\n");

    fclose (fout);
}

int main(int argc, char** argv)
{
    const char* level1_input_data_file;
    const char* lsk_kernel;
    const char* sclk_kernel;
    const char* frames_kernel;
    const char* msopck_data_file;
    const char* msopck_setup_file;
    FILE* fin;
    FILE* fout;
    struct Level1Record level1Record;
    int status;

    if (argc < 4)
    {
        printf("Usage: ./ola-level1-to-ck <level1-input-data-file> <lsk-kernel-filename> <sclk-kernel-filename> <frames-kernel-filename> <msopck-data-file> <msopck-setup-file>\n");
        return 1;
    }

    level1_input_data_file = argv[1];
    lsk_kernel = argv[2];
    sclk_kernel = argv[3];
    frames_kernel = argv[4];
    msopck_data_file = argv[5];
    msopck_setup_file = argv[6];

    /* Open the input file */
    fin = fopen(level1_input_data_file, "r");
    if (fin == NULL)
    {
        printf("Could not open %s\n", level1_input_data_file);
        exit(1);
    }

    /* Open the output file */
    fout = fopen(msopck_data_file, "w");
    if (fout == NULL)
    {
        printf("Could not open %s\n", msopck_data_file);
        exit(1);
    }

    for ( ;; ) /* loop until we break out */
    {
        /* Read in level 1 record */
        status = readLevel1Record(fin, &level1Record);
        if (status != 0)
            break;

        /* save out next orientation to output file */
        status = fprintf(fout, "%s %.16e %.16e 0.0\n",
                         level1Record.met,
                         -level1Record.azimuth/1000.0,
                         -level1Record.elevation/1000.0);
        if (status < 0)
            break;
    }

    /* Close open files */
    fclose (fin);
    fclose (fout);

    writeMsopckSetupFile(msopck_setup_file, lsk_kernel, sclk_kernel, frames_kernel);

    return 0;
}

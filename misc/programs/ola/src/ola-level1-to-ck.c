#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <strings.h>
#include "SpiceUsr.h"
#include "ola-common.h"


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

#define  FILLEN   1024
#define  TYPLEN   32
#define  SRCLEN   1024

/**
 * write the msopck setup file
 *
 * @param[in] filename
 * @param[in] lsk_kernel
 * @param[in] sclk_kernel
 * @param[in] frames_kernel
 */
void writeMsopckSetupFile(const char* filename)
{
    FILE* fout;
    SpiceInt which;
    SpiceInt handle;
    SpiceInt count;
    SpiceChar file[FILLEN];
    SpiceChar file_copy[FILLEN];
    SpiceChar filtyp[TYPLEN];
    SpiceChar source[SRCLEN];
    SpiceBoolean found;
    char* base_name; /* basename of kernel, e.g. 'naif0010.tls' */
    const char* dir_name; /* folder of kernel, e.g. 'LSK' */
    char lsk_kernel[FILLEN];
    char sclk_kernel[FILLEN];
    char frames_kernel[FILLEN];

    /* Open the output file */
    fout = fopen(filename, "w");
    if (fout == NULL)
    {
        printf("Could not open %s\n", filename);
        exit(1);
    }

    /* Look at the loaded kernel files and look for the leap second, sclk, and frames kernel files
       which are needed in the msopck setup file */
    ktotal_c ( "text", &count );

    if ( count == 0 )
    {
        printf ( "No kernel files loaded at this time.\n" );
        exit(1);
    }

    for ( which = 0;  which < count;  which++ )
    {
        kdata_c ( which,  "text",    FILLEN,   TYPLEN, SRCLEN,
                  file,   filtyp,  source, &handle,  &found );

        strncpy(file_copy, file, FILLEN);

        /* the following extracts out the basename and final folder of the kernel path.
         * E.g. if the path is SPICE/Kernels/FK/orx_ola_v000.tf, then it extract out
         * 'orx_ola_v000.tf' as the basename and 'FK' as the folder.
         * Unfortunately, this is tedious in C */
        base_name = strrchr(file_copy, '/');
        if (base_name != NULL)
            *base_name = '\0';
        else
        {
            printf("kernel filename not in expected format\n");
            exit(1);
        }
        base_name = base_name + 1;

        dir_name = strrchr(file_copy, '/') + 1;
        if (dir_name == NULL)
        {
            printf("kernel filename not in expected format\n");
            exit(1);
        }

        if (strncasecmp(dir_name, "LSK", 3) == 0 && strncasecmp(base_name, "naif", 4) == 0)
            strncpy(lsk_kernel, file, FILLEN);
        else if (strncasecmp(dir_name, "SCLK", 4) == 0 && strncasecmp(base_name, "ORX_SCLKSCET", 12) == 0)
            strncpy(sclk_kernel, file, FILLEN);
        else if (strncasecmp(dir_name, "FK", 2) == 0 && strncasecmp(base_name, "orx_ola_", 8) == 0)
            strncpy(frames_kernel, file, FILLEN);
    }

    if (lsk_kernel == NULL || sclk_kernel == NULL || frames_kernel == NULL)
    {
        printf("Error: could not determine leap second, spacecraft clock or frames kernel file needed for msopck.\n");
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
    const char* meta_kernel_file;
    const char* msopck_data_file;
    const char* msopck_setup_file;
    FILE* fin;
    FILE* fout;
    struct Level1Record level1Record;
    int status;

    if (argc < 5)
    {
        printf("Usage: ./ola-level1-to-ck <level1-input-data-file> <meta-kernel-filename> <msopck-data-file> <msopck-setup-file>\n");
        return 1;
    }

    level1_input_data_file = argv[1];
    meta_kernel_file = argv[2];
    msopck_data_file = argv[3];
    msopck_setup_file = argv[4];

    /* Load in SPICE kernel files */
    furnsh_c(meta_kernel_file);

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

    writeMsopckSetupFile(msopck_setup_file);

    return 0;
}

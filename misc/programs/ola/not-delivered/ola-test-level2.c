#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>


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

struct Level2Record
{
    char met[MET_SIZE_BYTES+1]; /* add 1 to include null terminating character */
    char utc[UTC_SIZE_BYTES+1]; /* add 1 to include null terminating character */
    double et;
    uint16_t laser_selection;
    uint16_t scan_mode;
    uint16_t flag_status;
    double range;
    double azimuth;
    double elevation;
    double intensity_t0;
    double intensity_trr;
    double x;
    double y;
    double z;
    double elongitude;
    double latitude;
    double radius;
};

int readLevel2Record(FILE* fin, struct Level2Record* level2Record)
{
    if (fread ( &level2Record->met, MET_SIZE_BYTES, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->utc, UTC_SIZE_BYTES, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->et, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->laser_selection, 2, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->scan_mode, 2, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->flag_status, 2, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->range, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->azimuth, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->elevation, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->intensity_t0, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->intensity_trr, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->x, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->y, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->z, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->elongitude, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->latitude, 8, 1, fin ) != 1)
        return 1;
    if (fread ( &level2Record->radius, 8, 1, fin ) != 1)
        return 1;

    /* Set null terminating character of met string */
    level2Record->met[MET_SIZE_BYTES] = 0;
    level2Record->utc[UTC_SIZE_BYTES] = 0;

    return 0;
}

int main(int argc, char** argv)
{
    const char* inputfile;
    const char* outputfile;
    FILE* fin;
    FILE* fout;
    struct Level2Record level2Record;
    int status;

    if (argc < 3)
    {
        printf("Usage: ./ola-level1-to-ck <inputfile> <outputfile>\n");
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
        status = readLevel2Record(fin, &level2Record);
        if (status != 0)
            break;

        /* save out next lidar position in km. Save it out in the order needed by SBMT
           so it can load in the track file */
        status = fprintf(fout, "%s %g %g %g %g %g %g %s %f %d %d %d %g %g %g %g %g\n",
                         level2Record.utc,
                         0.001*level2Record.x,
                         0.001*level2Record.y,
                         0.001*level2Record.z,
                         level2Record.elongitude,
                         level2Record.latitude,
                         level2Record.radius,
                         level2Record.met,
                         level2Record.et,
                         level2Record.laser_selection,
                         level2Record.scan_mode,
                         level2Record.flag_status,
                         level2Record.range,
                         level2Record.azimuth,
                         level2Record.elevation,
                         level2Record.intensity_t0,
                         level2Record.intensity_trr);

        if (status < 0)
            break;
    }

    /* Close open files */
    fclose (fin);
    fclose (fout);

    return 0;
}

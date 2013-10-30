#ifndef __OLA_COMMON_H__
#define __OLA_COMMON_H__

#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <time.h>


#define MET_SIZE_BYTES 18 /* does not include null terminating character */
#define LEVEL1_RECORD_SIZE_BYTES 64


/**
 * The Level1Record struct stores the values of a single Level 1 record. See
 * the ICD for an explanantion of each field.
 */
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
 * Get current date/time, format is YYYY-MM-DDTHH:mm:ss
 * @param[out] buf character buffer in which to place time. Caller must allocate enough memory for it.
 */
void getCurrentDateTime(char* buf)
{
    struct tm  tstruct;
    time_t     now = time(0);
    tstruct = *gmtime(&now);
    strftime(buf, 64, "%Y-%m-%dT%XZ", &tstruct);
}


#endif

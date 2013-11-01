#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include "SpiceUsr.h"
#include "ola-common.h"


/************************************************************************
 * OLA Level 1 to Level 2 Converter
 *
 * This program takes OLA Level 1 science data and also SPICE data and
 * outputs OLA Level 2 science data as well as a PDS4 label file.
 * Refer to the relevant ICD document for descriptions of the level 1 and 2
 * formats.
 *
 * This program should be called in the following manner:
 *
 * ./ola-level1-to-level2 <meta-kernel-file> <ck-kernel-file> <level1-input-data-file> <level2-output-data-file> <level2-output-label-file>
 *
 * The program takes 5 command line arguments, all of them required, as follows:
 *
 * 1. <meta-kernel-file> a SPICE kernel meta file listing all the spice kernel to load
 * 2. <ck-kernel-file> the OLA CK kernel file which was generated in a previos stage
 *             of the pipeline. Because it was generated as part of the pipeline
 *             it is not listed in the SPICE metakernel file of the previous argument
 *             so it is therefore required as a separate command line argument
 * 3. <level1-input-data-file> the binary level 1 data file which was generated in a
 *                             previous stage of the pipeline
 * 4. <level2-output-data-file> name of the binary level 2 data file which this program
 *                              should generates. The file contains a binary table of the data
 * 5. <level2-output-label-file> name of the level 2 PDS label file which this program
 *                               should generate. This file a PDS4 label in XML format
************************************************************************/


/************************************************************************
* Constants
************************************************************************/

#define UTC_SIZE_BYTES 24 /* does not include null terminating character */
#define LEVEL2_RECORD_SIZE_BYTES 144

/**
 * The Level2Record struct stores the values of a single Level 2 record. See
 * the ICD for an explanantion of each field.
 */
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


/**
 * This function converts a Level1Record to a Level2Record using the SPICE kernel
 * files.
 *
 * @param[in] level1Record The level 1 data is read from here
 * @param[out] level2Record The level 2 data is written here. Caller must make sure pointer is valid.
 * @return 0 if converted successfully, 1 otherwise
 */
int convertLevel1ToLevel2(const struct Level1Record* level1Record, struct Level2Record* level2Record)
{
    const char* scname = "ORX_OLA";
    int32_t scid = -64; /* ORX spacecraft ID */
    const char* ref = "IAU_BENNU";
    const char* abcorr = "NONE";
    const char* bodyname = "BENNU";
    const char* ola_hi_frame = "ORX_OLA_HIGH";
    const char* ola_low_frame = "ORX_OLA_LOW";
    const char* ola_frame;
    double scposb[3];
    double boredir[3];
    double lt;
    double i2bmat[3][3];
    double vpxi[3] = {0.0, 0.0, 1.0};
    double ci[3];
    double targetpos[3];

    /* Convert met to ephemeris time */
    strncpy(level2Record->met, level1Record->met, MET_SIZE_BYTES+1);
    scs2e_c(scid, level2Record->met, &level2Record->et);

    /* convert ephemeris time to UTC */
    et2utc_c(level2Record->et, "ISOD", 6, UTC_SIZE_BYTES+1, level2Record->utc);

    /* Get spacecraft position */
    spkpos_c(scname, level2Record->et, ref, abcorr, bodyname, scposb, &lt);
    if (failed_c())
        return 1;
    /* convert from km to meters */
    scposb[0] *= 1000.0;
    scposb[1] *= 1000.0;
    scposb[2] *= 1000.0;

    /* Compute boresite direction */
    if (level1Record->laser_selection == 0)
        ola_frame = ola_hi_frame;
    else
        ola_frame = ola_low_frame;
    pxform_c(ola_frame, ref, level2Record->et, i2bmat);
    if (failed_c())
        return 1;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, boredir);

    /* compute the target position */
    level2Record->range = level1Record->range;
    /* note range is in millimeters and needs to be converted to meters. Hence we multiple by 0.001 */
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
    /* convert to degrees */
    level2Record->latitude *= dpr_c();
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


/**
 * Write out a level 2 record to a file using the fout FILE pointer.
 *
 * @param[in] fout
 * @param[in] level2Record
 * @return 0 if written successfully, 1 otherwise
 */
int writeLevel2Record(FILE* fout, const struct Level2Record* level2Record)
{
    if (fwrite(level2Record->met, MET_SIZE_BYTES, 1, fout) != 1)
        return 1;
    if (fwrite(level2Record->utc, UTC_SIZE_BYTES, 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->et, sizeof(level2Record->et), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->laser_selection, sizeof(level2Record->laser_selection), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->scan_mode, sizeof(level2Record->scan_mode), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->flag_status, sizeof(level2Record->flag_status), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->range, sizeof(level2Record->range), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->azimuth, sizeof(level2Record->azimuth), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->elevation, sizeof(level2Record->elevation), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->intensity_t0, sizeof(level2Record->intensity_t0), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->intensity_trr, sizeof(level2Record->intensity_trr), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->x, sizeof(level2Record->x), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->y, sizeof(level2Record->y), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->z, sizeof(level2Record->z), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->elongitude, sizeof(level2Record->elongitude), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->latitude, sizeof(level2Record->latitude), 1, fout) != 1)
        return 1;
    if (fwrite(&level2Record->radius, sizeof(level2Record->radius), 1, fout) != 1)
        return 1;

    return 0;
}

/**
 * Write out a PDS4 complient label (which is in XML format).
 *
 * @param[in] labelfile name of label file to use
 * @param[in] datafilename
 * @param[in] starttime
 * @param[in] stoptime
 * @param[in] numRecords
 */
void writeLabel(
        const char* labelfile,
        const char* datafilename,
        const char* metstarttime,
        const char* metstoptime,
        uint32_t numRecords)
{
    FILE* fout;
    char currentDateTime[256];
    int32_t offset = 1;
    int32_t scid = -64; /* ORX spacecraft ID */
    double et;
    char utcstarttime[UTC_SIZE_BYTES+1];
    char utcstoptime[UTC_SIZE_BYTES+1];
    const char* base_name;
    char base_name_underscore[1024];
    char* pointer_to_period;

    /* Open the output file */
    fout = fopen(labelfile, "w");
    if (fout == NULL)
    {
        printf("Could not open %s\n", labelfile);
        exit(1);
    }

    /* the following extracts out the filename without the leading directory part */
    base_name = strrchr(datafilename, '/') + 1;
    if (base_name == NULL)
        base_name = datafilename;

    /* The next 4 lines replace the final period in basename with an underscore */
    strncpy(base_name_underscore, base_name, sizeof(base_name_underscore));
    pointer_to_period = strrchr(base_name_underscore, '.');
    if (pointer_to_period != NULL)
        *pointer_to_period = '_';

    getCurrentDateTime(currentDateTime);

    /* convert met start and stop times to UTC for display in the label files */
    scs2e_c(scid, metstarttime, &et);
    et2utc_c(et, "ISOC", 0, UTC_SIZE_BYTES+1, utcstarttime);
    scs2e_c(scid, metstoptime, &et);
    et2utc_c(et, "ISOC", 0, UTC_SIZE_BYTES+1, utcstoptime);

    fprintf(fout, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
    fprintf(fout, "<Product_Observational xmlns=\"http://pds.nasa.gov/schema/pds4/pds/v07\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n");
    fprintf(fout, "    <Identification_Area>\r\n");
    fprintf(fout, "        <logical_identifier>OLA_LEVEL2_%s</logical_identifier>\r\n", base_name_underscore);
    fprintf(fout, "        <version_id>1.0</version_id>\r\n");
    fprintf(fout, "        <title>OLA LEVEL 2 LIDAR DATA</title>\r\n");
    fprintf(fout, "        <information_model_version>1.0</information_model_version>\r\n");
    fprintf(fout, "        <product_class>Product_Observational</product_class>\r\n");
    fprintf(fout, "    </Identification_Area>\r\n");
    fprintf(fout, "    <Observation_Area>\r\n");
    fprintf(fout, "        <Time_Coordinates>\r\n");
    fprintf(fout, "            <start_date_time>%s</start_date_time>\r\n", utcstarttime);
    fprintf(fout, "            <stop_date_time>%s</stop_date_time>\r\n", utcstoptime);
    fprintf(fout, "        </Time_Coordinates>\r\n");
    fprintf(fout, "        <Investigation_Area>\r\n");
    fprintf(fout, "            <name>OSIRIS-REX with OLA</name>\r\n");
    fprintf(fout, "            <type>Mission</type>\r\n");
    fprintf(fout, "            <Internal_Reference>\r\n");
    fprintf(fout, "                <lid_reference>urn:nasa:pds:osiris-rex_ola:ola_level2:%s</lid_reference>\r\n", base_name_underscore);
    fprintf(fout, "                <reference_type>has_investigation</reference_type>\r\n");
    fprintf(fout, "            </Internal_Reference>\r\n");
    fprintf(fout, "        </Investigation_Area>\r\n");
    fprintf(fout, "        <Observing_System>\r\n");
    fprintf(fout, "            <Observing_System_Component>\r\n");
    fprintf(fout, "                <name>OLA</name>\r\n");
    fprintf(fout, "                <observing_system_component_type>Instrument</observing_system_component_type>\r\n");
    fprintf(fout, "                <description>OSIRIS-REx Laser Altimeter</description>\r\n");
    fprintf(fout, "            </Observing_System_Component>\r\n");
    fprintf(fout, "            <Observing_System_Component>\r\n");
    fprintf(fout, "                <name>OSIRIS-REX</name>\r\n");
    fprintf(fout, "                <observing_system_component_type>Spacecraft</observing_system_component_type>\r\n");
    fprintf(fout, "                <description>OSIRIS-REX</description>\r\n");
    fprintf(fout, "            </Observing_System_Component>\r\n");
    fprintf(fout, "        </Observing_System>\r\n");
    fprintf(fout, "        <Target_Identification>\r\n");
    fprintf(fout, "            <name>BENNU</name>\r\n");
    fprintf(fout, "            <type>ASTEROID</type>\r\n");
    fprintf(fout, "        </Target_Identification>\r\n");
    fprintf(fout, "    </Observation_Area>\r\n");
    fprintf(fout, "    <File_Area_Observational>\r\n");
    fprintf(fout, "        <File>\r\n");
    fprintf(fout, "            <file_name>%s</file_name>\r\n", base_name);
    fprintf(fout, "            <local_identifier>OLA_LEVEL2_%s</local_identifier>\r\n", base_name_underscore);
    fprintf(fout, "            <creation_date_time>%s</creation_date_time>\r\n", currentDateTime);
    fprintf(fout, "            <file_size unit=\"byte\">%.0f</file_size>\r\n", (double)numRecords * LEVEL2_RECORD_SIZE_BYTES);
    fprintf(fout, "            <records>%u</records>\r\n", numRecords);
    fprintf(fout, "        </File>\r\n");
    fprintf(fout, "        <Table_Binary>\r\n");
    fprintf(fout, "            <local_identifier>OLA-LEVEL2-DIR_TABLE_BINARY</local_identifier>\r\n");
    fprintf(fout, "            <offset unit=\"byte\">0</offset>\r\n");
    fprintf(fout, "            <records>%u</records>\r\n", numRecords);
    fprintf(fout, "            <Record_Binary>\r\n");
    fprintf(fout, "                <fields>17</fields>\r\n");
    fprintf(fout, "                <groups>0</groups>\r\n");
    fprintf(fout, "                <record_length unit=\"byte\">%d</record_length>\r\n", LEVEL2_RECORD_SIZE_BYTES);
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>MET</name>\r\n");
    fprintf(fout, "                    <field_number>1</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=18;
    fprintf(fout, "                    <data_type>ASCII_Time</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">18</field_length>\r\n");
    fprintf(fout, "                    <description>spacecraft clock time</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>UTC</name>\r\n");
    fprintf(fout, "                    <field_number>2</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=24;
    fprintf(fout, "                    <data_type>ASCII_Time</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">24</field_length>\r\n");
    fprintf(fout, "                    <description>UTC Time (yyyy-doyThh:mm:ss.ssssss)</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>ET</name>\r\n");
    fprintf(fout, "                    <field_number>3</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <description>ephemeris Time</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>LASER SELECTION</name>\r\n");
    fprintf(fout, "                    <field_number>4</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=2;
    fprintf(fout, "                    <data_type>UnsignedLSB2</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">2</field_length>\r\n");
    fprintf(fout, "                    <description>laser selection (O: HELT, 1: LELT)</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>SCAN MODE</name>\r\n");
    fprintf(fout, "                    <field_number>5</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=2;
    fprintf(fout, "                    <data_type>UnsignedLSB2</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">2</field_length>\r\n");
    fprintf(fout, "                    <description>scan mode (0: raster, 1: linear azim,\r\n");
    fprintf(fout, "                             2: linear elev, 3: fixed, 4: reserved)</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>FLAG STATUS</name>\r\n");
    fprintf(fout, "                    <field_number>6</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=2;
    fprintf(fout, "                    <data_type>UnsignedLSB2</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">2</field_length>\r\n");
    fprintf(fout, "                    <description>flag status (0: valid return, 1: valid return\r\n");
    fprintf(fout, "                             with overflow, 2: no return, 3: missing sample)</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>RANGE</name>\r\n");
    fprintf(fout, "                    <field_number>7</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>millimeters</unit>\r\n");
    fprintf(fout, "                    <description>lidar range calibrated</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>AZIMUTH</name>\r\n");
    fprintf(fout, "                    <field_number>8</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>Mrad</unit>\r\n");
    fprintf(fout, "                    <description>azimuth calibrated</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>ELEVATION</name>\r\n");
    fprintf(fout, "                    <field_number>9</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>Mrad</unit>\r\n");
    fprintf(fout, "                    <description>elevation calibrated</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>INTENSITY T0</name>\r\n");
    fprintf(fout, "                    <field_number>10</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>TBD</unit>\r\n");
    fprintf(fout, "                    <description>intensity of outgoing pulse calibrated</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>INTENSITY TRR</name>\r\n");
    fprintf(fout, "                    <field_number>11</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>TBD</unit>\r\n");
    fprintf(fout, "                    <description>intensity of incoming pulse calibrated</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>X</name>\r\n");
    fprintf(fout, "                    <field_number>12</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>meters</unit>\r\n");
    fprintf(fout, "                    <description>X position of lidar point</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>Y</name>\r\n");
    fprintf(fout, "                    <field_number>13</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>meters</unit>\r\n");
    fprintf(fout, "                    <description>Y position of lidar point</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>Z</name>\r\n");
    fprintf(fout, "                    <field_number>14</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>meters</unit>\r\n");
    fprintf(fout, "                    <description>Z position of lidar point</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>ELONGITUDE</name>\r\n");
    fprintf(fout, "                    <field_number>15</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>deg</unit>\r\n");
    fprintf(fout, "                    <description>east longitude of lidar point</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>LATITUDE</name>\r\n");
    fprintf(fout, "                    <field_number>16</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>deg</unit>\r\n");
    fprintf(fout, "                    <description>latitude of lidar point</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>RADIUS</name>\r\n");
    fprintf(fout, "                    <field_number>17</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>km</unit>\r\n");
    fprintf(fout, "                    <description>radius of lidar point</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "            </Record_Binary>\r\n");
    fprintf(fout, "        </Table_Binary>\r\n");
    fprintf(fout, "    </File_Area_Observational>\r\n");
    fprintf(fout, "</Product_Observational>\r\n");

    fclose(fout);
}

int main(int argc, char** argv)
{
    const char* meta_kernel_file;
    const char* ck_kernel_file;
    const char* level1_input_data_file;
    const char* level2_output_data_file;
    const char* level2_output_label_file;
    FILE* fin;
    FILE* fout;
    struct Level1Record level1Record;
    struct Level2Record level2Record;
    int32_t status;
    uint32_t numberRecords = 0;
    char metstarttime[MET_SIZE_BYTES+1];
    char metstoptime[MET_SIZE_BYTES+1];

    if (argc < 6)
    {
        printf("Usage: ./ola-level1-to-level2 <meta-kernel-file> <ck-kernel-file> <level1-input-data-file> <level2-output-data-file> <level2-output-label-file>\n");
        return 1;
    }

    meta_kernel_file = argv[1];
    ck_kernel_file = argv[2];
    level1_input_data_file = argv[3];
    level2_output_data_file = argv[4];
    level2_output_label_file = argv[5];

    /* Load in SPICE kernel files */
    furnsh_c(meta_kernel_file);
    furnsh_c(ck_kernel_file);

    /* Open the input file */
    fin = fopen(level1_input_data_file, "r");
    if (fin == NULL)
    {
        printf("Could not open %s\n", level1_input_data_file);
        exit(1);
    }

    /* Open the output file */
    fout = fopen(level2_output_data_file, "w");
    if (fout == NULL)
    {
        printf("Could not open %s\n", level2_output_data_file);
        exit(1);
    }

    for ( ;; ) /* loop until no more data */
    {
        /* Read in level 1 record */
        status = readLevel1Record(fin, &level1Record);
        if (status != 0)
            break;

        /* Convert level 1 record to level 2 */
        status = convertLevel1ToLevel2(&level1Record, &level2Record);
        if (status != 0)
            break;

        /* save out the level 2 record */
        status = writeLevel2Record(fout, &level2Record);
        if (status != 0)
            break;

        /* Hold on to the start time as it is needed in the label file */
        if (numberRecords == 0)
            strncpy(metstarttime, level2Record.met, MET_SIZE_BYTES+1);

        ++numberRecords;
    }

    /* Hold on to the stop time as it is needed in the label file */
    strncpy(metstoptime, level2Record.met, MET_SIZE_BYTES+1);

    /* Close open files */
    fclose(fin);
    fclose(fout);

    writeLabel(level2_output_label_file, level2_output_data_file, metstarttime, metstoptime, numberRecords);

    return 0;
}

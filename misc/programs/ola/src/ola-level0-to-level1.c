#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <math.h>
#include <libgen.h>
#include "SpiceUsr.h"
#include "ola-common.h"


/************************************************************************
 * OLA Level 0 to Level 1 Converter
 *
 * This program takes OLA Level 0 science data and also SPICE data and
 * outputs OLA Level 1 science data as well as a PDS4 label file.
 * Refer to the relevant ICD document for descriptions of the level 0 and 1
 * formats.
 *
 * This program should be called in the following manner:
 *
 * ./ola-level0-to-level1 <meta-kernel-file> <level0-input-data-file> <level1-output-data-file> <level1-output-label-file>
 *
 * The program takes 4 command line arguments, all of them required, as follows:
 *
 * 1. <meta-kernel-file> a SPICE kernel meta file listing all the spice kernel to load
 * 2. <level0-input-data-file> the binary level 0 data file which was generated in a
 *                             previous stage of the pipeline
 * 3. <level1-output-data-file> name of the binary level 1 data file which this program
 *                              should generates. The file contains a binary table of the data
 * 4. <level1-output-label-file> name of the level 1 PDS label file which this program
 *                               should generate. This file a PDS4 label in XML format
************************************************************************/


/************************************************************************
* Constants
************************************************************************/
#define UTC_SIZE_BYTES 24 /* does not include null terminating character */


/************************************************************************
* Global variables (prefixed with g_)
************************************************************************/
static int32_t g_scan_point_id = 0;
static int32_t g_block_number = -1;

struct Level0Record
{
    uint16_t laser_selection;
    char notused1[10];
    uint16_t block_number;
    char notused2[8];
    uint16_t scan_mode;
    char notused3[6];
    uint16_t sc_clock_partition;
    uint32_t ola_time_seconds;
    uint16_t ola_time_sub_seconds;
    char notused4[6];
    int16_t time_delta_to_mrtu;
    double range;
    double azimuth;
    double elevation;
    double intensity_t0;
    double intensity_trr;
    uint16_t flag_status;
    int16_t scan_point_alignment_difference;
};

/**
 * This function reads the level 0 binary data and puts it into a Level0Record structure.
 *
 * The level 0 science data is stored as follows:
 * @param[in] fin level 1 file stream pointer to open level 1 data file
 * @param[out] Level0Record structure filled in by this function
 *                     containing values read from the file
 */
int readLevel0Record(FILE* fin, struct Level0Record* level0Record)
{
    if (fread ( &level0Record->laser_selection, sizeof(level0Record->laser_selection), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->notused1, sizeof(level0Record->notused1), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->block_number, sizeof(level0Record->block_number), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->notused2, sizeof(level0Record->notused2), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->scan_mode, sizeof(level0Record->scan_mode), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->notused3, sizeof(level0Record->notused3), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->sc_clock_partition, sizeof(level0Record->sc_clock_partition), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->ola_time_seconds, sizeof(level0Record->ola_time_seconds), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->ola_time_sub_seconds, sizeof(level0Record->ola_time_sub_seconds), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->notused4, sizeof(level0Record->notused4), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->time_delta_to_mrtu, sizeof(level0Record->time_delta_to_mrtu), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->range, sizeof(level0Record->range), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->azimuth, sizeof(level0Record->azimuth), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->elevation, sizeof(level0Record->elevation), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->intensity_t0, sizeof(level0Record->intensity_t0), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->intensity_trr, sizeof(level0Record->intensity_trr), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->flag_status, sizeof(level0Record->flag_status), 1, fin ) != 1)
        return 1;
    if (fread ( &level0Record->scan_point_alignment_difference, sizeof(level0Record->scan_point_alignment_difference), 1, fin ) != 1)
        return 1;

    return 0;
}

/**
 * This function calibrates the range value by converting the uncalibrated
 * range value using a polynomial function. Currently the calibration formula
 * is unknown so this function just returns the original value. When the
 * calibration is known, this function will need to be modified.
 *
 * @param[in] range uncalibrated range
 * @return calibrated range
 */
double calibrateRange(double range)
{
    return range;
}

/**
 * This function calibrates the azimuth value by converting the uncalibrated
 * azimuth value using a polynomial function. Currently the calibration formula
 * is unknown so this function just returns the original value. When the
 * calibration is known, this function will need to be modified.
 *
 * @param[in] azimuth uncalibrated azimuth
 * @return calibrated azimuth
 */
double calibrateAzimuth(double azimuth)
{
    return azimuth;
}

/**
 * This function calibrates the elevation value by converting the uncalibrated
 * elevation value using a polynomial function. Currently the calibration formula
 * is unknown so this function just returns the original value. When the
 * calibration is known, this function will need to be modified.
 *
 * @param[in] elevation uncalibrated elevation
 * @return calibrated elevation
 */
double calibrateElevation(double elevation)
{
    return elevation;
}

/**
 * This function calibrates the intensity value by converting the uncalibrated
 * intensity value using a polynomial function. Currently the calibration formula
 * is unknown so this function just returns the original value. When the
 * calibration is known, this function will need to be modified.
 *
 * @param[in] intensity uncalibrated intensity T0
 * @return calibrated intensity T0
 */
double calibrateInensityT0(double intensity)
{
    return intensity;
}

/**
 * This function calibrates the intensity value by converting the uncalibrated
 * intensity value using a polynomial function. Currently the calibration formula
 * is unknown so this function just returns the original value. When the
 * calibration is known, this function will need to be modified.
 *
 * @param[in] intensity uncalibrated intensity TRr
 * @return calibrated intensity TRr
 */
double calibrateIntensityTrr(double intensity)
{
    return intensity;
}

/**
 * This function computes the spacecraft clock time using the various time quantities
 * in the level 0 binary data.
 *
 * @param[in] sc_clock_partition spacecraft clock partition number (e.g. is sc clock string is 1/0647854072.00097
 *            then partition number is 1)
 * @param[in] seconds
 * @param[in] subseconds
 * @param[in] time_delta_to_mrtu
 * @param[in] scan_point_alignment_difference
 * @param[in] firing_rate
 * @param[out] met spacecraft clock as a string calculated by this function
 */
void computeTime(uint16_t sc_clock_partition,
                 double seconds,
                 double subseconds,
                 double time_delta_to_mrtu,
                 double scan_point_alignment_difference,
                 double firing_rate,
                 uint32_t scan_point_id,
                 char met[MET_SIZE_BYTES+1])
{
    double t;
    t = seconds + (subseconds / 65536.0) - (time_delta_to_mrtu / 1000000.0);
    t += (((double)scan_point_id - 1.0) / firing_rate) + (scan_point_alignment_difference / 1000000.0);

    /* Now break t up into a seconds part and a subseconds part */
    seconds = floor(t);
    subseconds = t - seconds;

    /* transform subseconds part to within range of 0 to 2^16-1 */
    subseconds = subseconds * 65536.0;

    /* round subseconds to nearest integer */
    subseconds = round(subseconds);

    /* if subseconds is 65536, then increment seconds and set subseconds to 0 */
    if (subseconds == 65536.0)
    {
        subseconds = 0.0;
        seconds = seconds + 1.0;
    }

    snprintf(met, MET_SIZE_BYTES+1, "%u/%010u.%05u", sc_clock_partition, (uint32_t)seconds, (uint32_t)subseconds);
}

/**
 * This function converts a Level0Record to a Level1Record.
 *
 * @param[in] level0Record The level 0 data
 * @param[out] level1Record The level 1 data
 * @return 0 if converted successfully, 1 otherwise
 */
int convertLevel0ToLevel1(const struct Level0Record* level0Record, struct Level1Record* level1Record)
{
    uint32_t firing_rate = 10000;

    level1Record->laser_selection = level0Record->laser_selection;
    level1Record->scan_mode = level0Record->scan_mode;
    level1Record->flag_status = level0Record->flag_status;
    level1Record->range = calibrateRange(level0Record->range);
    level1Record->azimuth = calibrateAzimuth(level0Record->azimuth);
    level1Record->elevation = calibrateElevation(level0Record->elevation);
    level1Record->intensity_t0 = calibrateInensityT0(level0Record->intensity_t0);
    level1Record->intensity_trr = calibrateIntensityTrr(level0Record->intensity_trr);

    /* firing rate is number of shots per second which is 10000 when in LELT
       and 100 when in HELT */
    if (level1Record->laser_selection == 0)
        firing_rate = 100;

    ++g_scan_point_id;
    if (level0Record->block_number != g_block_number)
    {
        g_block_number = level0Record->block_number;
        g_scan_point_id = 1;
    }

    computeTime(level0Record->sc_clock_partition,
                level0Record->ola_time_seconds,
                level0Record->ola_time_sub_seconds,
                level0Record->time_delta_to_mrtu,
                level0Record->scan_point_alignment_difference,
                firing_rate,
                g_scan_point_id,
                level1Record->met);

    return 0;
}

/**
 * Write out a level 1 record to a file using the fout FILE pointer.
 *
 * @param[in] fout file pointer
 * @param[in] level1Record record to write out
 * @return 0 if written successfully, 1 otherwise
 */
int writeLevel1Record(FILE* fout, const struct Level1Record* level1Record)
{
    if (fwrite(level1Record->met, MET_SIZE_BYTES, 1, fout) != 1)
        return 1;
    if (fwrite(&level1Record->laser_selection, sizeof(level1Record->laser_selection), 1, fout) != 1)
        return 1;
    if (fwrite(&level1Record->scan_mode, sizeof(level1Record->scan_mode), 1, fout) != 1)
        return 1;
    if (fwrite(&level1Record->flag_status, sizeof(level1Record->flag_status), 1, fout) != 1)
        return 1;
    if (fwrite(&level1Record->range, sizeof(level1Record->range), 1, fout) != 1)
        return 1;
    if (fwrite(&level1Record->azimuth, sizeof(level1Record->azimuth), 1, fout) != 1)
        return 1;
    if (fwrite(&level1Record->elevation, sizeof(level1Record->elevation), 1, fout) != 1)
        return 1;
    if (fwrite(&level1Record->intensity_t0, sizeof(level1Record->intensity_t0), 1, fout) != 1)
        return 1;
    if (fwrite(&level1Record->intensity_trr, sizeof(level1Record->intensity_trr), 1, fout) != 1)
        return 1;

    return 0;
}

/*
 * Write out a PDS4 complient label (which is in XML format).
 *
 * @param[in] labelfile name of label file to use
 * @param[in] datafilename name of output data file
 * @param[in] metstarttime spacecraft clock time of first record
 * @param[in] metstoptime spacecraft clock time of last record
 * @param[in] numRecords number of records in the file
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
        *pointer_to_period  = '_';

    getCurrentDateTime(currentDateTime);

    /* convert met start and stop times to UTC for display in the label files */
    scs2e_c(scid, metstarttime, &et);
    et2utc_c(et, "ISOC", 0, UTC_SIZE_BYTES+1, utcstarttime);
    scs2e_c(scid, metstoptime, &et);
    et2utc_c(et, "ISOC", 0, UTC_SIZE_BYTES+1, utcstoptime);


    fprintf(fout, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
    fprintf(fout, "<Product_Observational xmlns=\"http://pds.nasa.gov/schema/pds4/pds/v07\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n");
    fprintf(fout, "    <Identification_Area>\r\n");
    fprintf(fout, "        <logical_identifier>OLA_LEVEL1_%s</logical_identifier>\r\n", base_name_underscore);
    fprintf(fout, "        <version_id>1.0</version_id>\r\n");
    fprintf(fout, "        <title>OLA LEVEL 1 LIDAR DATA</title>\r\n");
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
    fprintf(fout, "                <lid_reference>urn:nasa:pds:osiris-rex_ola:ola_level1:%s</lid_reference>\r\n", base_name_underscore);
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
    fprintf(fout, "            <local_identifier>OLA_LEVEL1_%s</local_identifier>\r\n", base_name_underscore);
    fprintf(fout, "            <creation_date_time>%s</creation_date_time>\r\n", currentDateTime);
    fprintf(fout, "            <file_size unit=\"byte\">%.0f</file_size>\r\n", (double)numRecords * LEVEL1_RECORD_SIZE_BYTES);
    fprintf(fout, "            <records>%u</records>\r\n", numRecords);
    fprintf(fout, "        </File>\r\n");
    fprintf(fout, "        <Table_Binary>\r\n");
    fprintf(fout, "            <local_identifier>OLA-LEVEL1-DIR_TABLE_BINARY</local_identifier>\r\n");
    fprintf(fout, "            <offset unit=\"byte\">0</offset>\r\n");
    fprintf(fout, "            <records>%u</records>\r\n", numRecords);
    fprintf(fout, "            <Record_Binary>\r\n");
    fprintf(fout, "                <fields>9</fields>\r\n");
    fprintf(fout, "                <groups>0</groups>\r\n");
    fprintf(fout, "                <record_length unit=\"byte\">%d</record_length>\r\n", LEVEL1_RECORD_SIZE_BYTES);
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>MET</name>\r\n");
    fprintf(fout, "                    <field_number>1</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=18;
    fprintf(fout, "                    <data_type>ASCII_Time</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">18</field_length>\r\n");
    fprintf(fout, "                    <description>spacecraft clock time</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>LASER SELECTION</name>\r\n");
    fprintf(fout, "                    <field_number>2</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=2;
    fprintf(fout, "                    <data_type>UnsignedLSB2</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">2</field_length>\r\n");
    fprintf(fout, "                    <description>laser selection (O: HELT, 1: LELT)</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>SCAN MODE</name>\r\n");
    fprintf(fout, "                    <field_number>3</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=2;
    fprintf(fout, "                    <data_type>UnsignedLSB2</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">2</field_length>\r\n");
    fprintf(fout, "                    <description>scan mode (0: raster, 1: linear azim,\r\n");
    fprintf(fout, "                             2: linear elev, 3: fixed, 4: reserved)</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>FLAG STATUS</name>\r\n");
    fprintf(fout, "                    <field_number>4</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=2;
    fprintf(fout, "                    <data_type>UnsignedLSB2</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">2</field_length>\r\n");
    fprintf(fout, "                    <description>flag status (0: valid return, 1: valid return\r\n");
    fprintf(fout, "                             with overflow, 2: no return, 3: missing sample)</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>RANGE</name>\r\n");
    fprintf(fout, "                    <field_number>5</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>millimeters</unit>\r\n");
    fprintf(fout, "                    <description>lidar range calibrated</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>AZIMUTH</name>\r\n");
    fprintf(fout, "                    <field_number>6</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>Mrad</unit>\r\n");
    fprintf(fout, "                    <description>azimuth calibrated</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>ELEVATION</name>\r\n");
    fprintf(fout, "                    <field_number>7</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>Mrad</unit>\r\n");
    fprintf(fout, "                    <description>elevation calibrated</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>INTENSITY T0</name>\r\n");
    fprintf(fout, "                    <field_number>8</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>TBD</unit>\r\n");
    fprintf(fout, "                    <description>intensity of outgoing pulse calibrated</description>\r\n");
    fprintf(fout, "                </Field_Binary>\r\n");
    fprintf(fout, "                <Field_Binary>\r\n");
    fprintf(fout, "                    <name>INTENSITY TRR</name>\r\n");
    fprintf(fout, "                    <field_number>9</field_number>\r\n");
    fprintf(fout, "                    <field_location unit=\"byte\">%d</field_location>\r\n", offset); offset+=8;
    fprintf(fout, "                    <data_type>IEEE754LSBDouble</data_type>\r\n");
    fprintf(fout, "                    <field_length unit=\"byte\">8</field_length>\r\n");
    fprintf(fout, "                    <unit>TBD</unit>\r\n");
    fprintf(fout, "                    <description>intensity of incoming pulse calibrated</description>\r\n");
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
    const char* level0_input_data_file;
    const char* level1_output_data_file;
    const char* level1_output_label_file;
    FILE* fin;
    FILE* fout;
    struct Level0Record level0Record;
    struct Level1Record level1Record;
    int status;
    char metstarttime[MET_SIZE_BYTES+1];
    char metstoptime[MET_SIZE_BYTES+1];
    uint32_t numberRecords = 0;

    if (argc < 5)
    {
        printf("Usage: ./ola-level1-to-level2 <meta-kernel-file> <level0-input-data-file> <level1-output-data-file> <level1-output-label-file>\n");
        return 1;
    }

    meta_kernel_file = argv[1];
    level0_input_data_file = argv[2];
    level1_output_data_file = argv[3];
    level1_output_label_file = argv[4];

    /* Load in SPICE kernel file */
    furnsh_c(meta_kernel_file);

    /* Open the input file */
    fin = fopen(level0_input_data_file, "r");
    if (fin == NULL)
    {
        printf("Could not open %s", level0_input_data_file);
        exit(1);
    }

    /* Open the output file */
    fout = fopen(level1_output_data_file, "w");
    if (fout == NULL)
    {
        printf("Could not open %s", level1_output_data_file);
        exit(1);
    }

    g_scan_point_id = 0;
    g_block_number = -1;

    for ( ;; ) /* loop until no more data */
    {
        /* Read in level 0 record */
        status = readLevel0Record(fin, &level0Record);
        if (status != 0)
            break;

        /* Convert level 0 record to level 1 */
        status = convertLevel0ToLevel1(&level0Record, &level1Record);
        if (status != 0)
            break;

        /* save out the level 1 record */
        status = writeLevel1Record(fout, &level1Record);
        if (status != 0)
            break;

        /* Hold on to the start time as it is needed in the label file */
        if (numberRecords == 0)
            strncpy(metstarttime, level1Record.met, MET_SIZE_BYTES+1);

        ++numberRecords;
    }

    /* Hold on to the stop time as it is needed in the label file */
    strncpy(metstoptime, level1Record.met, MET_SIZE_BYTES+1);

    /* Close open files */
    fclose(fin);
    fclose(fout);

    writeLabel(level1_output_label_file, level1_output_data_file, metstarttime, metstoptime, numberRecords);

    return 0;
}

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "spiceFortran.h"


#define MAXLEN      2048
#define START_TIME "START_TIME"
#define STOP_TIME "STOP_TIME"
#define SPACECRAFT_CLOCK_START_COUNT "SPACECRAFT_CLOCK_START_COUNT"
#define SPACECRAFT_CLOCK_STOP_COUNT "SPACECRAFT_CLOCK_STOP_COUNT"


/*
  This function extracts the ephemeris start and stop times and the
  spacecraft clock start and stop times from a given LBL
  file. In particular it extracts the values as text of these 4
  variables:
    START_TIME
    STOP_TIME
    SPACECRAFT_CLOCK_START_COUNT
    SPACECRAFT_CLOCK_STOP_COUNT

  It returns the midpoint between the start and stop ephemeris times.
  
  Input:

  lblfile - the LBL file to scan for the times

  Output:

  startEt - the ephemeris start time recorded in lblfile
  stopEt - the ephemeris stop time recorded in lblfile
  startSC - the spacecraft clock start time recorded in lblfile
  stopSC - the spacecraft clock stop time recorded in lblfile

  Returns:

    the midpoint between the start and stop ephemeris times.
	
 */
double getEt(const char *lblfile,
			 char *startEt, char *stopEt,
			 char *startSC, char *stopSC)
{
	FILE *fd;
	char str[MAXLEN];

	double startTime;
	double stopTime;
	
	if (NULL == (fd = fopen(lblfile, "r"))) {
        fprintf(stderr, "ERROR: Failed to open file %s.\n", lblfile);
        exit(1);
	}

	/* Read through the input file until we reach the keys START_TIME
	   and STOP_TIME. Convert these times to ET and return the
	   midpoint between them */

	
	while (1)
	{
		fscanf(fd, "%2048s", str);

		if (feof(fd))
		{
			fprintf(stderr, "ERROR: Failed to find the START_TIME and STOP_TIME values in %s\n", lblfile);
			exit(1);
		}

		if (!strcmp(str, START_TIME))
		{
			fscanf(fd, "%2048s", str);
			fscanf(fd, "%2048s", str);
			strcpy(startEt, str);
			utc2et_(str, &startTime, strlen(str));
		}
		if (!strcmp(str, STOP_TIME))
		{
			fscanf(fd, "%2048s", str);
			fscanf(fd, "%2048s", str);
			strcpy(stopEt, str);
			utc2et_(str, &stopTime, strlen(str));
		}
		if (!strcmp(str, SPACECRAFT_CLOCK_START_COUNT))
		{
			fscanf(fd, "%2048s", str);
			fscanf(fd, "%2048s", str);
			strcpy(startSC, str);
		}
		if (!strcmp(str, SPACECRAFT_CLOCK_STOP_COUNT))
		{
			fscanf(fd, "%2048s", str);
			fscanf(fd, "%2048s", str);
			strcpy(stopSC, str);
			break;
		}
	}

	return (stopTime + startTime) / 2.0;
}

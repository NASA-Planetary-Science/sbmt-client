#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "SpiceUsr.h"


/************************************************************************
* Constants
************************************************************************/
#define PATH_SIZE 256
#define LINE_SIZE 1024
#define WORD_SIZE 128
#define NUMBER_FILES 3
const char Tabfiles[NUMBER_FILES][PATH_SIZE] =
{
    "/project/nearsdc/data/ITOKAWA/LIDAR/edr/edr20050911_20050929.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/edr/edr20050930_20051028.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/edr/edr20051029_20051125.tab"
};
const char Outfiles[NUMBER_FILES][PATH_SIZE] =
{
    "/project/nearsdc/data/ITOKAWA/LIDAR/edr/edr_uf_20050911_20050930.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/edr/edr_uf_20051001_20051031.tab",
    "/project/nearsdc/data/ITOKAWA/LIDAR/edr/edr_uf_20051101_20051118.tab"
};
const char* const kernelfiles = "/project/nearsdc/spice-kernels/hayabusa/kernels.txt";


/************************************************************************
* This program tries to find better positions of the lidar points than
* is published on the PDS.
************************************************************************/
int main(int argc, char** argv)
{
    furnsh_c(kernelfiles);
    erract_c("SET", 1, (char*)"RETURN");

    int i;
    int count = 0;

    const char* target = "HAYABUSA";
    const char* ref = "IAU_ITOKAWA";
    const char* abcorr = "NONE";
    const char* obs = "ITOKAWA";
    const char* frame = "HAYABUSA_LIDAR";
 
    for (i=0; i<NUMBER_FILES; ++i)
    {
        const char* tabfilename = Tabfiles[i];
        FILE *fin = fopen(tabfilename, "r");
        if (fin == NULL)
        {
            printf("Could not open %s\n", tabfilename);
            exit(1);
        }

        const char* outfilename = Outfiles[i];
        FILE *fout = fopen(outfilename, "w");
        if (fout == NULL)
        {
            printf("Could not open %s\n", outfilename);
            exit(1);
        }
        
        char line[LINE_SIZE];
        char met[WORD_SIZE];
        char utc[WORD_SIZE];
        char rangeChar[WORD_SIZE];
        double range;
        double et;
        double lt;
        double scpos[3];
        double i2bmat[3][3];
        double vpxi[3];
        double ci[3];
        double boredir[3];
        double lidarpoint[3];
        
        while ( fgets ( line, sizeof line, fin ) != NULL ) /* read a line */
        {
            reset_c();
            
            sscanf(line, "%s %s %s", met, utc, rangeChar);
            range = atof(rangeChar);
            
            printf("%s\n", utc);

            utc2et_c(utc, &et);

            char utcstr[WORD_SIZE];
            et2utc_c(et, "C", 3, sizeof(utcstr), utcstr);
            printf("%s\n", utcstr);
            
            spkpos_c(target, et, ref, abcorr, obs, scpos, &lt);
            if (failed_c())
                continue;

            pxform_c(frame, ref, et, i2bmat);
            if (failed_c())
                continue;

            /* Compute the direction of the boresight vector */
            vpxi[0] = 0.0;
            vpxi[1] = 0.0;
            vpxi[2] = 1.0;
            vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
            mxv_c(i2bmat, ci, boredir);

            /* Normalize the vector */
            vhat_c(boredir, boredir);

            /* add vector of length range to spacecraft position to get lidar position */
            vscl_c(range, boredir, boredir);
            vadd_c(scpos, boredir, lidarpoint);

            double sx = scpos[0];
            double sy = scpos[1];
            double sz = scpos[2];
            double x = lidarpoint[0];
            double y = lidarpoint[1];
            double z = lidarpoint[2];

            fprintf(fout, "%s %s %s %.8e %.8e %.8e %.8e %.8e %.8e\n", met, utc, rangeChar, sx, sy, sz, x, y, z);
            
            ++count;
        }

        printf("points written %d\n", count);
        fflush(NULL);
        fclose ( fin );
        fclose ( fout );
    }

    return 0;
}

#include <stdio.h>
#include "SpiceUsr.h"
#include "SpiceDLA.h"
#include "dsk_proto.h"

/*

  This program is based on the example found in the documentation to
  the dskp02_c spice function and converts a binary dsk file to a the
  pds vertex format used to distribute various shape models.

  This program takes the following arguments:

  1. input bds file
  2. output file name

*/
int main(int argc, char** argv)
{   
    /*
      Constants 
    */
#define PBUFSIZ         10000

    /*
      Local variables 
    */
    SpiceBoolean            found;

    SpiceDLADescr           dladsc;

    SpiceDouble             verts  [PBUFSIZ][3];

    SpiceInt                handle;
    SpiceInt                i;
    SpiceInt                n;
    SpiceInt                np;
    SpiceInt                nread;
    SpiceInt                nv;
    SpiceInt                plates[PBUFSIZ][3];
    SpiceInt                plix;
    SpiceInt                remain;
    SpiceInt                start;

    char* inputfile = argv[1];
    char* outputfile = argv[2];
    FILE *fp;
    
    if (argc < 3)
    {
        printf("Usage: bds2vertex <inputfile> <outputfile>\n");
        return 1;
    }

    dasopr_c ( inputfile,    &handle );

    dlabfs_c ( handle, &dladsc, &found );

    if ( !found )
    { 
        setmsg_c ( "No segment found in file #." );
        errch_c  ( "#",  inputfile               );
        sigerr_c ( "SPICE(NOSEGMENT)"            );
    }

    /*
      Get segment vertex and plate counts.
    */
    dskz02_c ( handle, &dladsc, &nv, &np );

    printf ( "Number of vertices:  %d\n"
             "Number of plates:    %d\n",
             nv,
             np                           );

    if((fp=fopen(outputfile, "wb"))==NULL)
    {
        printf("Cannot open file.\n");
        return 1;
    }

    fprintf(fp, "%d %d\n", nv, np);
    
    /*
      Display the vertices of each plate.
    */
    remain = nv;
    start  = 1;

    while ( remain > 0 )
    { 
        /*
          `nread' is the number of plates we'll read on this
          loop pass.
        */
        nread  = mini_c ( 2, PBUFSIZ, remain );

        dskv02_c ( handle, &dladsc, start, nread, &n, verts );

        for ( i = 0; i < nread; i++ )
        {
            plix = start + i;

            fprintf(fp, "%d %.8e %.8e %.8e\n", plix, verts[i][0], verts[i][1], verts[i][2]);
        }

        start  = start  + nread;
        remain = remain - nread;
    }


    remain = np;
    start  = 1;

    while ( remain > 0 )
    { 
        /*
          `nread' is the number of plates we'll read on this
          loop pass.
        */
        nread  = mini_c ( 2, PBUFSIZ, remain );

        dskp02_c ( handle, &dladsc, start, nread, &n, plates );

        for ( i = 0; i < nread; i++ )
        {
            plix = start + i;

            fprintf(fp, "%d %d %d %d\n", plix, plates[i][0], plates[i][1], plates[i][2]);
        }

        start  = start  + nread;
        remain = remain - nread;
    }

    /*
      Close the kernel.  This isn't necessary in a stand-
      alone program, but it's good practice in subroutines
      because it frees program and system resources.
    */
    dascls_c ( handle );

    fclose(fp);
    
    return ( 0 );
}

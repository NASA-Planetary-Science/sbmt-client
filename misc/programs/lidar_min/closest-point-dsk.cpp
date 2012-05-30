#include <stdlib.h>
#include "SpiceUsr.h"
extern "C"
{
#include "SpiceDLA.h"
#include "SpiceDSK.h"
#include "pl02.h"
}

/* Used to perform ray intersection with shape model */
static SpiceDLADescr g_dladsc;

/* Used to perform ray intersection with shape model */
static SpiceInt g_handle;


/************************************************************************
* Initialize the dsk shape model
************************************************************************/
void initializeDsk(const char* const dskfile)
{
    SpiceBoolean found;
    dasopr_c ( dskfile, &g_handle );
    dlabfs_c ( g_handle, &g_dladsc, &found );
    if ( !found  )
    {
        setmsg_c ( "No segments found in DSK file #.");
        errch_c  ( "#",  dskfile                     );
        sigerr_c ( "SPICE(NODATA)"                   );
        exit(1);
    }
}

/** Public function */
void findClosestPointDsk(const double* origin, const double* direction, double* closestPoint, int* found)
{
    int plid;
    dskx02_c ( g_handle, &g_dladsc, origin, direction,
               &plid,  closestPoint, found );
}

void findClosestPointAndNormalDsk(const double* origin, const double* direction, double* closestPoint, double* normal, int* found)
{
    int plid;
    dskx02_c ( g_handle, &g_dladsc, origin, direction,
               &plid,  closestPoint, found );

    if (*found)
        dskn02_c ( g_handle, &g_dladsc, plid, normal );
}

/* Computes illumination angles. For Itokawa only. */
void illum_pl02Dsk ( SpiceDouble            et,
                     SpiceDouble            spoint [3],
                     SpiceDouble          * phase,
                     SpiceDouble          * solar,
                     SpiceDouble          * emissn )
{
    const char* obsrvr = "HAYABUSA";
    const char* target = "ITOKAWA";
    const char* abcorr = "LT+S";

    illum_pl02 ( g_handle, &g_dladsc, target, et,
                 abcorr, obsrvr, spoint,
                 phase, solar, emissn );
}

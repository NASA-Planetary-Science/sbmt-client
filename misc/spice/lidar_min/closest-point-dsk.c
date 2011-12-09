#include <stdlib.h>
#include "SpiceUsr.h"
#include "SpiceDLA.h"
#include "SpiceDSK.h"
#include "pl02.h"


static const char* const dskfile = "/project/nearsdc/data/ITOKAWA/quad512q.bds";

/* Used to perform ray intersection with shape model */
static SpiceDLADescr g_dladsc;

/* Used to perform ray intersection with shape model */
static SpiceInt g_handle;

static int initialized = 0;


/************************************************************************
* Loads the dsk shape model
************************************************************************/
static void loadDsk()
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
    if (!initialized)
    {
        loadDsk();
        initialized = 1;
    }

    int plid;
    dskx02_c ( g_handle, &g_dladsc, origin, direction,
               &plid,  closestPoint, found );
}

void findClosestPointAndNormalDsk(const double* origin, const double* direction, double* closestPoint, double* normal, int* found)
{
    if (!initialized)
    {
        loadDsk();
        initialized = 1;
    }

    int plid;
    dskx02_c ( g_handle, &g_dladsc, origin, direction,
               &plid,  closestPoint, found );

    if (*found)
        dskn02_c ( g_handle, &g_dladsc, plid, normal );
}

void illum_pl02Dsk ( SpiceDouble            et,
                     SpiceDouble            spoint [3],
                     SpiceDouble          * phase,
                     SpiceDouble          * solar,
                     SpiceDouble          * emissn )
{
    if (!initialized)
    {
        loadDsk();
        initialized = 1;
    }

    const char* obsrvr = "HAYABUSA";
    const char* target = "ITOKAWA";
    const char* abcorr = "LT+S";

    illum_pl02 ( g_handle, &g_dladsc, target, et,
                 abcorr, obsrvr, spoint,
                 phase, solar, emissn );
}

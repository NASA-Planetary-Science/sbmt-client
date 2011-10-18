#include <stdlib.h>
#include "SpiceUsr.h"
#include "SpiceDLA.h"
#include "SpiceDSK.h"


static const char* const dskfile = "/project/nearsdc/data/ITOKAWA/quad512q.bds";

/* Used to perform ray intersection with shape model */
static SpiceDLADescr g_dladsc;

/* Used to perform ray intersection with shape model */
static SpiceInt g_handle;


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
    static int initialized = 0;
    if (!initialized)
    {
        loadDsk();
        initialized = 1;
    }

    int plid;
    dskx02_c ( g_handle, &g_dladsc, origin, direction,
               &plid,  closestPoint, found );
}

#include <stdio.h>
#include <iostream>
#include <string>
#include "SpiceUsr.h"

using namespace std;

/*
   This function computes the position of the target in the observer body frame.
   
   Input:
     et:          Ephemeris time
     observerBody Name of observer body (e.g. EROS)
     targetBody   Name of target body (e.g. SUN)
   
   Output:
     targetPos:   The position of the target in observer body-fixed coordinates
*/
void getTargetBodyPosition(double et, const char* observerBody, const char* targetBody, double bodyToTarget[3])
{
    double lt, inert2bf[3][3], targetToBodyInertial[3], bodyToTargetInertial[3];
    const char *inertframe = "J2000";
    const char* abcorr = "LT+S";
    string ref = string("IAU_") + observerBody;

    /*
     *  Compute the apparent position of the center of the observer body
     *  as seen from the target at the epoch of observation (et),
     *  and the one-way light time from the observer to the target.
     */
    spkpos_c(observerBody, et, inertframe, abcorr, targetBody, targetToBodyInertial, &lt);
    if (failed_c()) {
        cerr << "Failed spkpos" << endl;
        return;
    }

    /*
     *  Get the position of the target.  This is just the negative of the
     *  target to observer vector using vminus().  Note that this is _NOT_
     *  the same as the apparent position of the target as seen from
     *  the observer!
     */
    vminus_c(targetToBodyInertial, bodyToTargetInertial);    // still in j2000 coordinates

    /*
     *  Get the coordinate transformation from inertial to
     *  body-fixed coordinates at ET minus one light time ago.
     */
    pxform_c(inertframe, ref.c_str(), et - lt, inert2bf);
    if (failed_c()) {
        cerr << "Failed pxform" << endl;
        return;
    }

    /*
     *  transform target position vector from inertial to body-fixed frame
     */
    mxv_c(inert2bf, bodyToTargetInertial, bodyToTarget);

//    printf("Target position LT: %f %f %f \n",targetpos[0], targetpos[1], targetpos[2]);
}

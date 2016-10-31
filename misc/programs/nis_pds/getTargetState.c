#include <stdio.h>
#include <iostream>
#include <string>
#include "SpiceUsr.h"

using namespace std;

#define  TIMFMT         "YYYY-MON-DD HR:MN:SC.###::UTC (UTC)"
#define  TIMLEN         41

/*
   This function computes the position of the target in the observer body frame, at the time the spacecraft observed the body.

   Input:
     et:           Ephemeris time when an image of the body was taken
     spacecraft:   Name of the spacecraft that took the image
     observerBody: Name of observer body (e.g. EROS, PLUTO, PHOEBE)
     targetBody:   Name of target body (e.g. SUN, EARTH). SPICE names can be found at
                   https://naif.jpl.nasa.gov/pub/naif/toolkit_docs/FORTRAN/req/naif_ids.html

   Output:
     bodyToSc:     The position of the target in observer body-fixed coordinates corrected for light time
     velocity:     The velocity of the target in observer body-fixed coordinates corrected for light time
*/
void getTargetState(double et, const char* spacecraft, const char* observerBody, const char* targetBody, double bodyToTarget[3], double velocity[3])
{
    double lt, notUsed[6], bodyToTargetState[6];
    const char* abcorr = "LT+S";
    string bodyFrame = string("IAU_") + observerBody;

    /*
     *  Compute the apparent state of the center of the observer body
     *  as seen from the spacecraft at the epoch of observation (et),
     *  and the one-way light time from the observer to the spacecraft.
     *  Only the returned light time will be used from this call, as
     *  such, the reference frame does not matter here. Use the body
     *  fixed frame.
     */
    spkezr_c(observerBody, et, bodyFrame.c_str(), abcorr, spacecraft, notUsed, &lt);
    if (failed_c()) {
        cerr << "Failed spkezr" << endl;
        return;
    }

    /*
     *  Back up the time at the observer body by the light time to the
     *  spacecraft. This is the time that light from the target body was
     *  received at the observer body when the spacecraft took the image.
     *  It is the time at the observer body. Now simply get the position
     *  of the target at this time, as seen from the observer body, in
     *  the observer body frame.
     */
    spkezr_c(targetBody, et - lt, bodyFrame.c_str(), abcorr, observerBody, bodyToTargetState, &lt);
    if (failed_c()) {
        cerr << "Failed spkezr" << endl;
        return;
    }

    /*
     *  Assign the output variables.
     */
    bodyToTarget[0] = bodyToTargetState[0];
    bodyToTarget[1] = bodyToTargetState[1];
    bodyToTarget[2] = bodyToTargetState[2];
    velocity[0] = bodyToTargetState[3];
    velocity[1] = bodyToTargetState[4];
    velocity[2] = bodyToTargetState[5];

//    SpiceChar timstr  [ TIMLEN ];
//    timout_c ( et, TIMFMT, TIMLEN, timstr );
//    printf("\nTime: %s \n",timstr);
//    printf("%s position 2: %f %f %f \n",targetBody, bodyToTarget[0], bodyToTarget[1], bodyToTarget[2]); //this is identical to above method with inertial frame
//    printf("%s velocity 2: %f %f %f \n",targetBody, velocity[0], velocity[1], velocity[2]);

}

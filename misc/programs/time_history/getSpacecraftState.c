#include <stdio.h>
#include <stdexcept>
#include <limits>
#include <iostream>
#include <string>
#include "SpiceUsr.h"

using namespace std;

#define  TIMFMT         "YYYY-MON-DD HR:MN:SC.###::UTC (UTC)"
#define  TIMLEN         41

/*
   This function computes the position of the spacecraft in the observer body frame, at the time the spacecraft observed the body.
   
   Input:
     et:           Ephemeris time when the image was taken
     observerBody: Name of observer body (e.g. EROS, PLUTO)
     spacecraft:   NAIF SPICE name of spacecraft (e.g. NEAR, NH). These can be found at
                   https://naif.jpl.nasa.gov/pub/naif/toolkit_docs/FORTRAN/req/naif_ids.html
   
   Output:
     bodyToSc:     The position of the spacecraft in observer body-fixed coordinates corrected for light time
     velocity:     The velocity of the spacecraft in observer body-fixed coordinates corrected for light time
*/
void getSpacecraftState(double et, const char* observerBody, const char* spacecraft, double bodyToSc[3], double velocity[3])
{
    double lt, scToBodyState[6];
    const char* abcorr = "LT+S";
    string bodyFrame = string("IAU_") + observerBody;

    /*
     *  Compute the apparent state of the body as seen from the
     *  spacecraft at the epoch of observation, in the body-fixed
     *  frame, corrected for stellar aberration and light time.
     *  Note that the time entered is the time at the spacecraft,
     *  who is the observer.
     */
    spkezr_c(observerBody, et, bodyFrame.c_str(), abcorr, spacecraft, scToBodyState, &lt);
    if (failed_c()) {
        cerr << "Failed spkezr" << endl;
        return;
    }

    /*
     *  The state of the spacecraft (apparent position and velocity)
     *  relative to the body is just the negative of this state. Note
     *  that this is not the same as the apparent position and velocity
     *  of the spacecraft as seen from the body at time et, because et
     *  is the time at the spacecraft not the body.
     */
    bodyToSc[0] = -scToBodyState[0];
    bodyToSc[1] = -scToBodyState[1];
    bodyToSc[2] = -scToBodyState[2];
    velocity[0] = -scToBodyState[3];
    velocity[1] = -scToBodyState[4];
    velocity[2] = -scToBodyState[5];

//    /*
//     *  Get the coordinate transformation from inertial to
//     *  body-fixed coordinates at ET minus one light time ago.
//     */
//    sxform_c(inertframe, bodyFrame.c_str(), et - lt, inert2bf);
//    if (failed_c()) {
//        cerr << "Failed pxform" << endl;
//        return;
//    }
//
//    /*
//     *  transform target position vector from inertial to body-fixed frame
//     */
//    mxvg_c(inert2bf, bodyToTargetStateInertial, 6, 6, bodyToTargetState);
//
//    bodyToSc[0] = bodyToTargetState[0];
//    bodyToSc[1] = bodyToTargetState[1];
//    bodyToSc[2] = bodyToTargetState[2];
//    velocity[0] = bodyToTargetState[3];
//    velocity[1] = bodyToTargetState[4];
//    velocity[2] = bodyToTargetState[5];
//
//
//    //This is correct
//    SpiceChar timstr  [ TIMLEN ];
//    timout_c ( et, TIMFMT, TIMLEN, timstr );
//    printf("\nTime: %s \n",timstr);
//    printf("SC position 1: %f %f %f \n",bodyToSc[0], bodyToSc[1], bodyToSc[2]);
//    printf("SC velocity 1: %f %f %f \n",velocity[0], velocity[1], velocity[2]);
//
//    spkezr_c(observerBody, et, bodyFrame.c_str(), abcorr, targetBody, bodyToTargetState, &lt);
//    bodyToSc[0] = -bodyToTargetState[0];
//    bodyToSc[1] = -bodyToTargetState[1];
//    bodyToSc[2] = -bodyToTargetState[2];
//    velocity[0] = -bodyToTargetState[3];
//    velocity[1] = -bodyToTargetState[4];
//    velocity[2] = -bodyToTargetState[5];

    SpiceChar timstr  [ TIMLEN ];
    timout_c ( et, TIMFMT, TIMLEN, timstr );
    printf("\nTime: %s \n",timstr);
    printf("%s position 2: %f %f %f \n",spacecraft,bodyToSc[0], bodyToSc[1], bodyToSc[2]); //this is identical to above method with inertial frame
    printf("%s velocity 2: %f %f %f \n",spacecraft,velocity[0], velocity[1], velocity[2]);
}

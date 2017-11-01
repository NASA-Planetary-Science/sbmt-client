#include <stdio.h>
#include <iostream>
#include <string>
#include "SpiceUsr.h"

#include <cstdlib>
#include <fstream>
#include <iostream>
#include <string>
#include <vector>

extern "C"
{
#include "SpiceUsr.h"
}
#define  FILSIZ         256
            #define  LNSIZE         81
            #define  MAXCOV         100000
            #define  WINSIZ         ( 2 * MAXCOV )
            #define  TIMLEN         51


#define MAXBND 4
#define WDSIZE 32
using namespace std;

/*
  This function computes the instrument boresight and frustum vectors in the observer body frame
  at the time the spacecraft imaged the body.

   Input:
     et:           Ephemeris time when an image of the body was taken
     observerBody: Name of observer body (e.g. EROS, PLUTO, PHOEBE)
     spacecraft:   Name of the spacecraft that took the image
     instrFrame:   SPICE frame ID of instrument on the observing spacecraft

   Output:
     boredir:    Boresight direction in bodyframe coordinates
     updir:
     frustum:    Field of view boundary corner vectors in bodyframe coordinates

*/
void getFov(double et, const char* spacecraft, const char* observerBody, const char* instrFrame, double boredir[3], double updir[3], double frustum[12])
{
	double lt, notUsed[6];
//    double targpos[3];  // sc to target vector in j2000
//    double scpos[3];    // target to sc vector in j2000
    double inst2inert[3][3], inert2bf[3][3], inst2bf[3][3];
    char shape[32];
    char frame[32];
    double bsight [3];
    int n;
    double bounds [MAXBND][3];
    double boundssbmt [MAXBND][3];
    //  The celestial body is the target when dealing with light time
//    const char* target = observerBody.c_str();
    string bodyFrame = string("IAU_") + observerBody;
    const char* abcorr = "LT+S";
    const char* inertframe = "J2000";
    SpiceInt instid;
    double tmpvec[3];

    /*
     *  Compute the apparent position of the center of the observer body
     *  as seen from the spacecraft at the epoch of observation (et),
     *  and the one-way light time from the observer to the spacecraft.
     *  Only the returned light time will be used from this call, as
     *  such, the reference frame does not matter here. Use J2000.
     */
//    spkpos_c(target, et, inertframe, abcorr, obs, targpos, &lt);
    spkpos_c(observerBody, et, inertframe, abcorr, spacecraft, notUsed, &lt);
    if (failed_c()) {
        cerr << "Failed getFov call to spkpos" << endl;
        return;
    }



    /*
    *  Get field of view boresight and boundary corners
    */
    namfrm_c(instrFrame, &instid);
    if (failed_c()) {
        cout << "Failed namfrm" << endl;
        return;
    }
    getfov_c(instid, MAXBND, WDSIZE, WDSIZE, shape, frame, bsight, &n, bounds);
    if (failed_c()) {
        cout << "Failed getfov" << endl;
        return;
    }


    //Tested with POLYCAM. The correct values are returned.
//	cout<< "bs " << bsight[0] << ", " << bsight[1] << ", " << bsight[2] << endl;
//	cout<< "bdy1 " << bounds[0][1] << ", " << bounds[0][1] << ", " << bounds[0][2] << endl;
//	cout<< "bdy1 " << bounds[1][1] << ", " << bounds[1][1] << ", " << bounds[1][2] << endl;
//	cout<< "bdy1 " << bounds[2][1] << ", " << bounds[2][1] << ", " << bounds[2][2] << endl;
//	cout<< "bdy1 " << bounds[3][1] << ", " << bounds[3][1] << ", " << bounds[3][2] << endl;

    /*
     *  Get the coordinate transformation from instrument to
     *  inertial frame at time ET
     */
    pxform_c(instrFrame, inertframe, et, inst2inert);
    if (failed_c()) {
    	cout << "Failed pxform1" << endl;
        return;
    }
    
    /*
    //The values calculated below are not used, they are just
    //for comparing with FITS header attitude (debugging).
    double sc2j[3][3];
    pxform_c("ORX_SPACECRAFT", inertframe, et, sc2j);
    SpiceDouble q[4];
    m2q_c(sc2j, q);
    cout << "sc2j  " << sc2j[0][0] << "  " << sc2j[0][1] << "  " << sc2j[0][2] << endl;
    cout << "sc2j  " << sc2j[1][0] << "  " << sc2j[1][1] << "  " << sc2j[1][2] << endl;
    cout << "sc2j  " << sc2j[2][0] << "  " << sc2j[2][1] << "  " << sc2j[2][2] << endl;
    //end debugging code.
    */

    /*
     *  Get the coordinate transformation from inertial to
     *  body-fixed coordinates at ET minus one light time ago.
     *  This subtraction is neccessary because the body is the
     *  observer in SBMT, but et is the time at the spacecraft.
     *  The light time here is the time it takes light to travel
     *  between the body and the spacecraft.
     */
    pxform_c(inertframe, bodyFrame.c_str(), et - lt, inert2bf);
    if (failed_c()) {
        cout << "Failed pxform2" << endl;
        return;
    }

    /*
     *  Compute complete transformation to go from
     *  instrument-fixed coordinates to body-fixed coords
     */
    mxm_c(inert2bf, inst2inert, inst2bf);


    /*
     *  Get the coordinate transformation from instrument to
     *  body-fixed coordinates at ET minus one light time ago.
     *  This subtraction is neccessary because the body is the
     *  observer in SBMT, but et is the time at the spacecraft.
     *  The light time here is the time it takes light to travel
     *  between the body and the spacecraft.
     *  SEEING IF THIS CAN REPLACE THE TWO PXFORM CALLS ABOVE.
     *  I DON'T THINK SO, THE VALUES DO DIFFER SLIGHTLY.
     */
//    pxform_c(instrFrame, bodyFrame.c_str(), et - lt, inst2bf);
//    if (failed_c()) {
//        cout << "Failed pxform2" << endl;
//        return;
//    }


	//swap the boundary corner vectors so they are in the correct order for SBMT
	//getfov returns them in the following order (quadrants): I, II, III, IV.
    //SBMT expects them in the following order (quadrants): II, I, III, IV.
	//So the vector index mapping is
	//SBMT   SPICE
	//  0       1
	//  1       0
	//  2       2
	//  3       3
	boundssbmt[0][0] = bounds[1][0];
    boundssbmt[0][1] = bounds[1][1];
    boundssbmt[0][2] = bounds[1][2];
    boundssbmt[1][0] = bounds[0][0];
    boundssbmt[1][1] = bounds[0][1];
    boundssbmt[1][2] = bounds[0][2];
    boundssbmt[2][0] = bounds[2][0];
    boundssbmt[2][1] = bounds[2][1];
    boundssbmt[2][2] = bounds[2][2];
    boundssbmt[3][0] = bounds[3][0];
    boundssbmt[3][1] = bounds[3][1];
    boundssbmt[3][2] = bounds[3][2];

    //transform boresight into body frame.
    mxv_c(inst2bf, bsight, boredir);

    //transform boundary corners into body frame and pack into frustum array.
	int k = 0;
	for (int i=0; i<MAXBND; i++)
	{
	    double bdyCorner[3];
	    double bdyCornerBodyFrm[3];
		vpack_c(boundssbmt[i][0], boundssbmt[i][1], boundssbmt[i][2], bdyCorner);
		mxv_c(inst2bf, bdyCorner, bdyCornerBodyFrm);
		for (int j=0; j<3; j++)
		{
			frustum[k] = bdyCornerBodyFrm[j];
			k++;
		}
	}

    /* Then compute the up direction */
    vpack_c(1.0, 0.0, 0.0, tmpvec);
    mxv_c(inst2bf, tmpvec, updir);
}

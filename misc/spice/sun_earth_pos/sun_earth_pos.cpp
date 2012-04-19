/* This program takes the name of a body, e.g. EROS, PHOBOS, DEIMOS
 * and a time in UTC string format (e.g. 2005-09-14T12:50:49.713) and
 * outputs the position of the earth and sun in the frame of the
 * specified body. This program takes 3 arguments:
 *
 * 1. kernel meta-file listing kernel files to load
 * 2. body name, e.g. EROS, PHOBOS, DEIMOS. Case does not matter.
 * 3. time in UTC, e.g. 2005-09-14T12:50:49.713
 *
 * Example: ./sun_earth_pos ./kernels.txt EROS 2005-09-14T12:50:49.713
 */


#include "SpiceUsr.h"
#include <iostream>
#include <algorithm>


using namespace std;


int main(int argc, char** argv)
{
    if (argc != 4)
    {
        cout << "Usage: " << argv[0] << " <kernelfile> <BODY> <time-in-UTC>" << endl;
        return 1;
    }
    
    const char* kernelfile = argv[1];
	string obs = argv[2];
    const char* utc = argv[3];

    // convert obs to upper case
    transform(obs.begin(), obs.end(), obs.begin(), ::toupper);
    
    double et;
    double lt;
    double rad;
    double lon;
    double lat;
	const char* abcorr = "LT+S";
	string ref = "IAU_" + obs;

    // For Lutetia, there is no IAU_ frame but a ROS_ frame instead
    if (obs == "LUTETIA")
        ref = "ROS_" + obs;
    
    
    cout.precision(16);

    // Load in kernels
    furnsh_c(kernelfile);

    utc2et_c(utc, &et);

    
    // Compute sun position
	const char* suntarget = "SUN";
    double sunpos[3];
    spkpos_c(suntarget, et, ref.c_str(), abcorr, obs.c_str(), sunpos, &lt);
    reclat_c(sunpos, &rad, &lon, &lat);
    if (lon < 0.0)
        lon += twopi_c();
    cout << "Sun latitude (deg):    " << lat*dpr_c() << endl;
    cout << "Sun longitude (deg):   " << lon*dpr_c() << endl;
    cout << "Sun distance (km):     " << rad << endl;



    // Compute earth position
	const char* earthtarget = "EARTH";
    double earthpos[3];
    spkpos_c(earthtarget, et, ref.c_str(), abcorr, obs.c_str(), earthpos, &lt);
    reclat_c(earthpos, &rad, &lon, &lat);
    if (lon < 0.0)
        lon += twopi_c();
    cout << "Earth latitude (deg):  " << lat*dpr_c() << endl;
    cout << "Earth longitude (deg): " << lon*dpr_c() << endl;
    cout << "Earth distance (km):   " << rad << endl;


    
    return 0;
}

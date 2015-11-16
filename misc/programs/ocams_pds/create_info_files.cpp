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

// ******************************************************************
// Create info files for OSIRIS-REX cameras MAPCAM and POLYCAM
// ******************************************************************

// The following 3 functions were adapted from
// http://stackoverflow.com/questions/479080/trim-is-not-part-of-the-standard-c-c-library?rq=1
const std::string whiteSpaces(" \f\n\r\t\v");

// Remove initial and trailing whitespace from string. Modifies string in-place
inline void trimRight(std::string& str, const std::string& trimChars =
        whiteSpaces) {
    std::string::size_type pos = str.find_last_not_of(trimChars);
    str.erase(pos + 1);
}

inline void trimLeft(std::string& str, const std::string& trimChars =
        whiteSpaces) {
    std::string::size_type pos = str.find_first_not_of(trimChars);
    str.erase(0, pos);
}

inline void trim(std::string& str, const std::string& trimChars = whiteSpaces) {
    trimRight(str, trimChars);
    trimLeft(str, trimChars);
}

void removeSurroundingQuotes(string& str) {
    int length = str.size();
    if (str[length - 1] == '\'')
        str = str.substr(0, length - 1);
    if (str[0] == '\'')
        str = str.substr(1);
}

vector<string> loadFileList(const string& filelist) {
    ifstream fin(filelist.c_str());

    vector < string > files;

    if (fin.is_open()) {
        string line;
        while (getline(fin, line))
            files.push_back(line);
    } else {
        cerr << "Error: Unable to open file '" << filelist << "'" << endl;
        exit(1);
    }

    return files;
}

void splitFitsHeaderLineIntoKeyAndValue(const string& line, string& key,
        string& value) {
    key = line.substr(0, 8);
    trim(key);
    value = line.substr(10);
    size_t found = value.find_last_of("/");
    if (found != string::npos)
        value = value.substr(0, found);
    trim(value);
    removeSurroundingQuotes(value);
    trim(value);
}

void getFieldsFromFitsHeader(const string& labelfilename, string& startmet,
        string& stopmet, string& target, int& naxis1, int& naxis2)
{
    ifstream fin(labelfilename.c_str());

    if (fin.is_open()) {
        char buffer[81];
        string str;
        string key;
        string value;

        for (int i = 0; i < 100; ++i) {
            fin.read(buffer, 80);
            buffer[80] = '\0';
            str = buffer;
            splitFitsHeaderLineIntoKeyAndValue(str, key, value);

            if (key == "NAXIS1") {
                naxis1 = atoi(value.c_str());
            } else if (key == "NAXIS2") {
                naxis2 = atoi(value.c_str());
            } else if (key == "SPCSCLK") {
                startmet = value;
                stopmet = value;
            } else if (key == "TARGET") {
                target = value;
            }
        }
    } else {
        cerr << "Error: Unable to open file '" << labelfilename << "'" << endl;
        exit(1);
    }

    fin.close();
}

void getStringFieldFromFitsHeader(const string& fitfile,
		   string key,
		   string& value )
{
    ifstream fin(fitfile.c_str());

    if (fin.is_open())
    {
        string str;
        char buffer[81];
        string currkey;
        string val;
        while(true)
        {
            fin.read(buffer, 80);
            buffer[80] = '\0';
            str = buffer;
            splitFitsHeaderLineIntoKeyAndValue(str, currkey, val);
            if (currkey == key)
            {
            	value = val.c_str();
                break;
            }
        }
    }
    else
    {
        cerr << "Error: Unable to open file '" << fitfile << "'" << endl;
        exit(1);
    }

    fin.close();
}

void getEt(const string& fitfile,
		   string sclkkey,
           string& utc,
           double& et,
		   const char* scid)
{
    int instid;
    int found;
    bodn2c_c(scid, &instid, &found);
    if (failed_c() || !found)
        return;

    ifstream fin(fitfile.c_str());

    if (fin.is_open())
    {
    	string str;
    	int len = 25;
    	char utcchar[len];
    	getStringFieldFromFitsHeader(fitfile, sclkkey, str);

        scs2e_c(instid, str.c_str(), &et);
        et2utc_c(et, "ISOC", 3, len, utcchar);
        utc = utcchar;
    }
    else
    {
        cerr << "Error: Unable to open file '" << fitfile << "'" << endl;
        exit(1);
    }

    fin.close();
}

/*
  This function calculates spacecraft position and orientation.

  Input:
  et:         Ephemeris time
  body:       Name of celestial body which is the target
  obs:        Name of observing spacecraft
  instrFrame: NAIF frame ID of instrument on the observing spacecraft

  Output:
  scposbf:    Spacecraft position in bodyframe coordinates
  boredir:    Boresight direction in bodyframe coordinates
  updir:
  frustum:    Field of view boundary corner vectors in bodyframe coordinates

*/
void getScPositionAndOrientation(double et, string body, const char* obs, const char* instrFrame,
					  double scposbf[3], double boredir[3], double updir[3], double frustum[12])
{
    double lt;
    double targpos[3];  // sc to target vector in j2000
    double scpos[3];    // target to sc vector in j2000
    double inst2inert[3][3], inert2bf[3][3], inst2bf[3][3], rot[3][3];
    char shape[32];
    char frame[32];
    double bsight [3];
    int n;
    double bounds [MAXBND][3];
    double boundssbmt [MAXBND][3];
    //  The celestial body is the target when dealing with light time
    const char* target = body.c_str();
    string ref = string("IAU_") + body.c_str();
    const char* abcorr = "LT+S";
    const char* inertframe = "J2000";
    SpiceInt instid;
    double tmpvec[3];

    /*
     *  Compute the apparent position of the center of the target body
     *  as seen from the spacecraft at the epoch of observation (et),
     *  and the one-way light time from the target to the spacecraft.
     */
    spkpos_c(target, et, inertframe, abcorr, obs, targpos, &lt);
    if (failed_c()) {
        cerr << "Failed spkpos" << endl;
        return;
    }

    /*
     *  Get the position of the observer.  This is just the negative of the
     *  spacecraft-target vector using vminus().  Note that this is _NOT_
     *  the same as the apparent position of the spacecraft as seen from
     *  the target!
     */
    vminus_c(targpos, scpos);

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

    /*
     *  There is a 180 degree rotation about the boresight in the
     *  data, not present in the sumfiles (so can't correct in sbmt code).
     */
    axisar_c(bsight, pi_c(), rot);
    mxm_c(inst2inert, rot, inst2inert);
    
    /*
     *  Get the coordinate transformation from inertial to
     *  body-fixed coordinates at ET minus one light time ago.
     */
    pxform_c(inertframe, ref.c_str(), et - lt, inert2bf);
    if (failed_c()) {
        cout << "Failed pxform2" << endl;
        return;
    }

    /*
     *  transform scpos vector into body-fixed from j2000 frame
     */
    mxv_c(inert2bf, scpos, scposbf);

    /*
     *  Compute complete transformation to go from
     *  instrument-fixed coordinates to body-fixed coords
     */
    mxm_c(inert2bf, inst2inert, inst2bf);

	//swap the boundary corner vectors so they are in the correct order for SBMT
	//getfov returns them counter-clockwise starting in the +X,+Y quadrant.
	//SBMT expects them in the following order (quadrants): -X,-Z -> +X,-Z -> -X,+Z -> +X,+Z
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

/*
 This function computes the position of the sun in the body frame.

 Input:
 et:         Ephemeris time

 Output:
 sunpos:     The position of the sun in body coordinates
 */
void getSunPosition(double et, string body, double sunpos[3])
{
    double lt, inert2bf[3][3], targpos[3];
    const char *target = body.c_str();
    const char *inertframe = "J2000";
    const char* abcorr = "LT+S";
    string ref = string("IAU_") + body.c_str();

    /*
     *  Compute the apparent position of the center of the target body
     *  as seen from the sun at the epoch of observation (et),
     *  and the one-way light time from the target to the spacecraft.
     */
    spkpos_c(target, et, inertframe, abcorr, "SUN", targpos, &lt);
    if (failed_c()) {
        cerr << "Failed spkpos" << endl;
        return;
    }

    /*
     *  Get the position of the SUN.  This is just the negative of the
     *  SUN to target vector using vminus().  Note that this is _NOT_
     *  the same as the apparent position of the SUN as seen from
     *  the target!
     */
    vminus_c(targpos, sunpos);    // still in j2000 coordinates

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
     *  transform sun position vector from inertial to body-fixed frame
     */
    mxv_c(inert2bf, sunpos, sunpos);
}

void saveInfoFile(string filename,
                  string utc,
                  const double scposb[3],
                  const double boredir[3],
                  const double updir[3],
                  const double frustum[12],
                  const double sunpos[3])
{
    ofstream fout(filename.c_str());

    if (!fout.is_open())
    {
        cerr << "Error: Unable to open file " << filename << " for writing" << endl;
        exit(1);
    }

    fout.precision(16);

    fout << "START_TIME          = " << utc << "\n";
    fout << "STOP_TIME           = " << utc << "\n";

    fout << "SPACECRAFT_POSITION = ( ";
    fout << scientific << scposb[0] << " , ";
    fout << scientific << scposb[1] << " , ";
    fout << scientific << scposb[2] << " )\n";

    fout << "BORESIGHT_DIRECTION = ( ";
    fout << scientific << boredir[0] << " , ";
    fout << scientific << boredir[1] << " , ";
    fout << scientific << boredir[2] << " )\n";

    fout << "UP_DIRECTION        = ( ";
    fout << scientific << updir[0] << " , ";
    fout << scientific << updir[1] << " , ";
    fout << scientific << updir[2] << " )\n";

    fout << "FRUSTUM1            = ( ";
    fout << scientific << frustum[0] << " , ";
    fout << scientific << frustum[1] << " , ";
    fout << scientific << frustum[2] << " )\n";

    fout << "FRUSTUM2            = ( ";
    fout << scientific << frustum[3] << " , ";
    fout << scientific << frustum[4] << " , ";
    fout << scientific << frustum[5] << " )\n";

    fout << "FRUSTUM3            = ( ";
    fout << scientific << frustum[6] << " , ";
    fout << scientific << frustum[7] << " , ";
    fout << scientific << frustum[8] << " )\n";

    fout << "FRUSTUM4            = ( ";
    fout << scientific << frustum[9] << " , ";
    fout << scientific << frustum[10] << " , ";
    fout << scientific << frustum[11] << " )\n";

    fout << "SUN_POSITION_LT     = ( ";
    fout << scientific << sunpos[0] << " , ";
    fout << scientific << sunpos[1] << " , ";
    fout << scientific << sunpos[2] << " )\n";
}


/*

  Taken from Amica create_info_files.cpp and LORRI create_info_files.cpp

  This program creates an info file for each fit file. For example the
  file N2516167681.INFO is created for the fit file N2516167681.FIT.

  This program takes the following input arguments:

  1. kernelfiles - a SPICE meta-kernel file containing the paths to the kernel files
  2. body - IAU name of the target body, all caps
  3. scid - SPICE spacecraft id
  4. instrframe - SPICE instrument frame name
  5. fitstimekeyword - FITS header time keyword, UTC assumed
  6. input file list - path to file in which all image files are listed
  7. output folder - path to folder where infofiles should be saved to
  8. output file list - path to file in which all files for which an infofile was
     created will be listed along with their start times.
*/
int main(int argc, char** argv)
{
    if (argc < 9)
    {
        cerr << "Usage: create_info_files <kernelfiles> <body> <scid> <instrframe> <fitstimekeyword> <inputfilelist> <infofilefolder> <outputfilelist>" << endl;
        return 1;
    }

    string kernelfiles = argv[1];
    string body = argv[2];
    const char* scid = argv[3];
    const char*  instr = argv[4];
    string sclkkey = argv[5];
    string inputfilelist = argv[6];
    string infofilefolder = argv[7];
    string outputfilelist = argv[8];

    furnsh_c(kernelfiles.c_str());

    erract_c("SET", 1, (char*)"RETURN");

    vector<string> fitfiles = loadFileList(inputfilelist);

    //Image list
    ofstream fout(outputfilelist.c_str());
    if (!fout.is_open()) {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }

    for (unsigned int i=0; i<fitfiles.size(); ++i)
    {
        reset_c();

        string utc;
        double et;
        double scposb[3];
        double boredir[3];
        double updir[3];
        double frustum[12];
        double sunPosition[3];

        getEt(fitfiles[i], sclkkey, utc, et, scid);
        if (failed_c())
            continue;

        getScPositionAndOrientation(et, body, scid, instr, scposb, boredir, updir, frustum);
        if (failed_c())
            continue;

        getSunPosition(et, body, sunPosition);
        if (failed_c())
            continue;

        const size_t last_slash_idx = fitfiles[i].find_last_of("\\/");
        if (std::string::npos != last_slash_idx)
        {
        	fitfiles[i].erase(0, last_slash_idx + 1);
        }
        int length = fitfiles[i].size();
        string infofilename = infofilefolder + "/"
                + fitfiles[i].substr(0, length-4) + ".INFO";
        saveInfoFile(infofilename, utc, scposb, boredir, updir, frustum, sunPosition);
        cout << "created " << infofilename << endl;

        fout << fitfiles[i].substr(0, length) << " " << utc << endl;

    }
    cout << "done." << endl;

    return 0;
}

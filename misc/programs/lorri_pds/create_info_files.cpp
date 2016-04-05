#include <fstream>
#include <iostream>
#include <vector>
#include <string>
#include <cstdlib>
#include <cmath>
#include <libgen.h>
#include <algorithm>
#include "SpiceUsr.h"

#define MAXSTRLEN   1024
#define MAXBND      4

using namespace std;

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
        string& stopmet, string& target, int& naxis1, int& naxis2) {
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

void getEt(const string& startmet, const string& stopmet, string& startutc,
        double& startet, string& stoputc, double& stopet) {

    int found;
    int id;
    bodn2c_c("NEW HORIZONS", &id, &found);

    scs2e_c(id, startmet.c_str(), &startet);
    char utc[25];
    et2utc_c(startet, "ISOC", 3, 25, utc);
    startutc = utc;

    scs2e_c(id, stopmet.c_str(), &stopet);
    et2utc_c(stopet, "ISOC", 3, 25, utc);
    stoputc = utc;

    cout.precision(16);
//    cout << "SPACECRAFT_CLOCK_START_COUNT " << startmet << " " << startutc
//            << " " << startet << std::endl;
//    cout << "SPACECRAFT_CLOCK_STOP_COUNT " << stopmet << " " << stoputc << " "
//            << stopet << std::endl;
}

/*
 This function

 Input:
 et:         Ephemeris time

 Output:
 scposb:
 boredir:
 updir:
 frustum:

 */
void getScOrientation(double et, string body, double scposbf[3],
        double boredir[3], double updir[3], double frustum[12]) {
    double lt;
    double targpos[3];  // sc to target vector in j2000
    double scpos[3];    // target to sc vector in j2000
    double inst2inert[3][3], inert2bf[3][3], inst2bf[3][3];
    double tmpvec[3], xo, yo, pixel_pitch, focal_length, fov;

    //  Note for NAIF this is observer
    const char* obs = "NEW HORIZONS";
    //  Note for NAIF Pluto is target when dealing with light time
    const char* target = body.c_str();
    string ref = string("IAU_") + body.c_str();
    const char* abcorr = "LT+S";
    const char* instframe = "NH_LORRI";
    const char* inertframe = "J2000";
    SpiceInt code;

    namfrm_c(ref.c_str(), &code);
//    cerr << "Code for " << ref << " = " << code << endl;

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

    cout.precision(16);
//    cout << "Spacecraft Position: " << scpos[0] << " " << scpos[1] << " "
//            << scpos[2] << endl;

    /*
     *  Get the coordinate transformation from instrument to
     *  J2000 frame at time ET
     */
    pxform_c(instframe, inertframe, et, inst2inert);
    if (failed_c()) {
        cerr << "Failed pxform" << endl;
        return;
    }

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
     *  transform scpos vector into body-fixed from j2000 frame
     */
    mxv_c(inert2bf, scpos, scposbf);

    /*
     *  Compute complete transformation to go from
     *  instrument-fixed coordinates to body-fixed coords
     */
    mxm_c(inert2bf, inst2inert, inst2bf);

    /*
     *  Compute boresight and frustum vectors in body-fixed coords
     *  xo = -tan(0.2907 * rpd_c() / 2.0);
     *  yo = -tan(0.2907 * rpd_c() / 2.0);
     */
    pixel_pitch = 13.0e-6;      // in m
    focal_length = 2.619082;    // in m from Bill Owen's model
    fov = atan2(pixel_pitch * 512.0, focal_length);    // in radians
    xo = -tan(fov);     // identical because we have
    yo = -tan(fov);     // square pixels

    /* First compute the direction of the center pixel */
    /* Note for LORRI, the boresight points in the -Z direction, not +Z. */
    vpack_c(0.0, 0.0, -1.0, tmpvec);
    mxv_c(inst2bf, tmpvec, boredir);

    /* Then compute the up direction */
    vpack_c(-1.0, 0.0, 0.0, tmpvec);
    mxv_c(inst2bf, tmpvec, updir);

    /* Now compute the frustum corner vectors.  ORDER IS SIGNIFICANT */
    vpack_c(-xo, -yo, -1.0, tmpvec);
    mxv_c(inst2bf, tmpvec, &(frustum[0]));

    vpack_c(xo, -yo, -1.0, tmpvec);
    mxv_c(inst2bf, tmpvec, &(frustum[3]));

    vpack_c(-xo, yo, -1.0, tmpvec);
    mxv_c(inst2bf, tmpvec, &(frustum[6]));

    vpack_c(xo, yo, -1.0, tmpvec);
    mxv_c(inst2bf, tmpvec, &(frustum[9]));
}

/*
 This function computes the position of the sun in the body frame.

 Input:
 et:         Ephemeris time

 Output:
 sunpos:     The position of the sun in body coordinates
 */
void getSunPosition(double et, string body, double sunpos[3]) {
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

    cout.precision(16);
//    cout << "Sun position: " << sunpos[0] << " " << sunpos[1] << " "
//            << sunpos[2] << endl;
}

void saveInfoFile(string filename, string startutc, string stoputc,
        const double scposb[3], const double boredir[3], const double updir[3],
        const double frustum[12], const double sunpos[3]) {
    ofstream fout(filename.c_str());

    if (!fout.is_open()) {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }

    fout.precision(16);

    fout << "START_TIME          = " << startutc << "\n";
    fout << "STOP_TIME           = " << stoputc << "\n";

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

 This program creates an info file for each label file. For example the
 file f339b23.INFO is created for the label file f339b23.img_label.

 This program takes the following input arguments:

    bodyName       - case-independent name of target body
    metaKernel     - SPICE meta kernel 
    fitsFileList   - input file containing a newline-delimited list of LORRI FITS files to process. Only those images targeting the specified body will be processed into INFO files.
    outputFolder   - output folder into which INFO files will be created
    outputFileList - output file containing a list of the FITS files that were successfully processed into INFO files

 */
int main(int argc, char** argv) {
    if (argc < 6) {
        cerr << "Usage:" << endl;
        cerr << "   create_info_files <bodyName> <metaKernel> <fitsFileList> <outputFolder> <outputFileList>" << endl;
        cerr << "Where:" << endl;
        cerr << "   bodyName       - case-independent name of target body" << endl;
        cerr << "   metaKernel     - SPICE meta kernel" << endl;
        cerr << "   fitsFileList   - input file containing a newline-delimited list of LORRI FITS" << endl;
        cerr << "                    files to process. Only those images targeting the specified" << endl;
        cerr << "                    body will be processed into INFO files." << endl;
        cerr << "   outputFolder   - output folder into which INFO files will be created" << endl;
        cerr << "   outputFileList - output file containing a list of the FITS files that were" << endl;                
        cerr << "                    successfully processed into INFO files" << endl;   
        return 1;
    }

    string body = argv[1];
    string kernelfiles = argv[2];
    string labelfilelist = argv[3];
    string outputfolder = argv[4];
    string outputfilelist = argv[5];

    furnsh_c(kernelfiles.c_str());

    erract_c("SET", 1, (char*) "RETURN");

    vector < string > labelfiles = loadFileList(labelfilelist);

    ofstream fout(outputfilelist.c_str());
    if (!fout.is_open()) {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }

    for (unsigned int i = 0; i < labelfiles.size(); ++i) {
        cout << endl;
        cout << "Processing " << labelfiles[i] << "..." << endl;
        reset_c();

        string startmet;
        string stopmet;
        string target;
        int naxis1;
        int naxis2;
        string startutc;
        double startet;
        string stoputc;
        double stopet;
        double et;
        double scposb[3];
        double boredir[3];
        double updir[3];
        double frustum[12];
        double sunPosition[3];

        getFieldsFromFitsHeader(labelfiles[i], startmet, stopmet, target,
                naxis1, naxis2);

        getEt(startmet, stopmet, startutc, startet, stoputc, stopet);
        if (failed_c()) {
            cerr << "Failed to get et for " << target << " in " << labelfiles[i]
                    << endl;
            continue;
        }

        et = startet + (stopet - startet) / 2.0;

        transform(body.begin(), body.end(), body.begin(), ::toupper);
        if (target != body) {
            cerr << body << " image " << labelfiles[i] << " will not be processed because target is " << target << "." << endl;
            continue;
        }
 
        // compute the orientation of the spacecraft
        getScOrientation(et, body, scposb, boredir, updir, frustum);
        if (failed_c()) {
            cerr << "Failed to get SC orientation for " << target << " in "
                    << labelfiles[i] << endl;
            continue;
        }

        // Get position of Sun relative to pluto at time of obeservation
        getSunPosition(et, body, sunPosition);
        if (failed_c()) {
            cerr << "Failed to get sun position for " << target << " in "
                    << labelfiles[i] << endl;
            continue;
        }

        string labelbasename = basename((char*) labelfiles[i].c_str());
        unsigned found = labelbasename.find_last_of(".");
        string infofilename = outputfolder + "/"
                + labelbasename.substr(0, found) + ".INFO";
        saveInfoFile(infofilename, startutc, stoputc, scposb, boredir, updir,
                frustum, sunPosition);
        cerr << target << " infofile created successfully in " << infofilename
                << endl;

        fout << labelbasename << " " << startutc << endl;
    }

    cout << endl;
    return 0;
}

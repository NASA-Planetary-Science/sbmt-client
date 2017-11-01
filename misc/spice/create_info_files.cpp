#include <stdlib.h>
#include <stdio.h>
#include <iomanip>
#include <string>
#include <sys/stat.h>
#include <cstdlib>
#include <iostream>
#include <fstream>
#include <vector>
#include "SpiceUsr.h"

#define  FILSIZ         256
            #define  LNSIZE         81
            #define  MAXCOV         100000
            #define  WINSIZ         ( 2 * MAXCOV )
            #define  TIMLEN         51


#define MAXBND 4
#define WDSIZE 32

using namespace std;

void getTargetState    (double et, const char* spacecraft, const char* observerBody, const char* targetBody, double targetpos[3], double velocity[3]);
void getSpacecraftState(double et, const char* spacecraft, const char* observerBody, double scPosition[3], double velocity[3]);
void getFov            (double et, const char* spacecraft, const char* observerBody, const char* instrFrame, double boredir[3], double updir[3], double frustum[12]);


// ******************************************************************
// Generic package to generate SPICE pointing info files. FITS header
// information is not used (TBD - may need it to orient the images,
// but keywords are mission-specific).
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
  3. sc - SPICE spacecraft name
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
        cerr << "Usage: create_info_files <kernelfiles> <body> <sc> <instrframe> <fitstimekeyword> <inputfilelist> <infofilefolder> <outputfilelist>" << endl;
        return 1;
    }

    string kernelfiles = argv[1];
    const char* body = argv[2];
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
        SpiceDouble unused[3];
        double boredir[3];
        double updir[3];
        double frustum[12];
        double sunPosition[3];

        getEt(fitfiles[i], sclkkey, utc, et, scid);
        if (failed_c())
            continue;

		getSpacecraftState(et, scid, body, scposb, unused);
		getTargetState(et, scid, body, "SUN", sunPosition, unused);
	    getFov(et, scid, body, instr, boredir, updir, frustum);
        if (failed_c())
            continue; //cout?

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

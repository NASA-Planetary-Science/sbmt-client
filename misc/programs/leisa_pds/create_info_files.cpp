#include <fstream>
#include <iostream>
#include <vector>
#include <string>
#include <cstdlib>
#include <cmath>
#include <libgen.h>
#include "SpiceUsr.h"


using namespace std;


// The following 3 functions were adapted from
// http://stackoverflow.com/questions/479080/trim-is-not-part-of-the-standard-c-c-library?rq=1
const std::string whiteSpaces( " \f\n\r\t\v" );

// Remove initial and trailing whitespace from string. Modifies string in-place
inline void trimRight( std::string& str,
                       const std::string& trimChars = whiteSpaces )
{
    std::string::size_type pos = str.find_last_not_of( trimChars );
    str.erase( pos + 1 );
}

inline void trimLeft( std::string& str,
                      const std::string& trimChars = whiteSpaces )
{
    std::string::size_type pos = str.find_first_not_of( trimChars );
    str.erase( 0, pos );
}

inline void trim( std::string& str,
                  const std::string& trimChars = whiteSpaces )
{
    trimRight( str, trimChars );
    trimLeft( str, trimChars );
}

void removeSurroundingQuotes(string& str)
{
    int length = str.size();
    if (str[length-1] == '\'')
    str = str.substr(0, length-1);
    if (str[0] == '\'')
        str = str.substr(1);
}

vector<string> loadFileList(const string& filelist)
{
    ifstream fin(filelist.c_str());

    vector<string> files;

    if (fin.is_open())
    {
        string line;
        while (getline(fin, line))
            files.push_back(line);
    }
    else
    {
        cerr << "Error: Unable to open file '" << filelist << "'" << endl;
        exit(1);
    }

    return files;
}

void splitFitsHeaderLineIntoKeyAndValue(const string& line,
                                        string& key,
                                        string& value)
{
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

void getFieldsFromFitsHeader(const string& labelfilename,
                             string& startmet,
                             string& stopmet,
                             string& duration,
                             string& exptime,
                             string& target,
                             string& frame,
                             int& naxis1,
                             int& naxis2)
{
    ifstream fin(labelfilename.c_str());

    if (fin.is_open())
    {
        char buffer[81];
        string str;
        string key;
        string value;
        
        for (int i=0; i<100; ++i)
        {
            fin.read(buffer, 80);
	    buffer[80] = '\0';
            str = buffer;
            splitFitsHeaderLineIntoKeyAndValue(str, key, value);

            if (key == "NAXIS1")
            {
                naxis1 = atoi(value.c_str());
            }
            // for LEISA, axis 2 is the spectral band axis, while axis 3 is the image vertical axis
            else if (key == "NAXIS3")
            {
                naxis2 = atoi(value.c_str());
            }
            else if (key == "SPCSCLK")
            {
                startmet = value;
                stopmet = value;
            }
            else if (key == "DURMET")
            {
                duration = value;
            }
            else if (key == "EXPTIME")
            {
                exptime = value;
            }
            else if (key == "TARGET")
            {
                target = value;
            }
            else if (key == "SPCINST0")
            {
                frame = value;
            }
        }
    }
    else
    {
        cerr << "Error: Unable to open file '" << labelfilename << "'" << endl;
        exit(1);
    }

    fin.close();
}

void getEt(const string& startmet,
           const string& stopmet,
           string& startutc,
           double& startet,
           string& stoputc,
           double& stopet)
{

    int found;
    int id;
    bodn2c_c("NEW HORIZONS", &id, &found);

    scs2e_c(id, startmet.c_str(), &startet);
    char utc[25];
    et2utc_c ( startet , "ISOC", 3, 25, utc );
    startutc = utc;

    scs2e_c(id, stopmet.c_str(), &stopet);
    et2utc_c ( stopet , "ISOC", 3, 25, utc );
    stoputc = utc;

    cout << "  Spacecraft clock start count: " << startmet << " " << startutc << " " << startet << std::endl;
    cout << "  Spacecraft clock stop count: " << stopmet << " " << stoputc << " " << stopet << std::endl;
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
void getScOrientation(double et, string body, string frame, double scposb[3], double boredir[3], 
                      double updir[3], double frustum[12], int naxis1, int naxis2)
{
    double lt;
    double i2bmat[3][3];
    double vpxi[3];
    double ci[3];
    double width,height;
    double fovanglex, fovangley;
    double pixelsizex, pixelsizey;
    double focalLength;
    int npixelsx = 0;
    int npixelsy = 0;

    const char* target = "NEW HORIZONS";
    string ref = string("IAU_") + body.c_str();
    const char* abcorr = "LT+S";
    const char* obs = body.c_str();

    spkpos_c(target, et, ref.c_str(), abcorr, obs, scposb, &lt);
    if (failed_c())
        return;

    cout.precision(16);
    cout << "  Spacecraft Position: " << scposb[0] << " " << scposb[1] << " " << scposb[2] << endl;

    pxform_c(frame.c_str(), ref.c_str(), et, i2bmat);
    if (failed_c())
        return;

    // specify FOV (lengths in millimeters)
    focalLength = 657.5;
    pixelsizex = 0.04;
    pixelsizey = 0.04;
    npixelsx = naxis2;
    npixelsy = naxis1;

    cout << "  Pixel Dimensions: " << naxis1 << " x " << naxis2 << endl;
    fovanglex = atan(npixelsx * pixelsizex / focalLength);
    fovangley = atan(npixelsy * pixelsizey / focalLength);

    width=-tan(fovanglex/2.0);
    height=-tan(fovangley/2.0);

    /* First compute the direction of the center pixel */
    /* Note for MVIC, the boresight points in the -X direction */
    vpxi[0] = -1.0;
    vpxi[1] = 0.0;
    vpxi[2] = 0.0;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, boredir);

    /* Then compute the up direction */
    vpxi[0] = 0.0;
    vpxi[1] = -1.0;
    vpxi[2] = 0.0;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, updir);

    /* Now compute the frustum */
    vpxi[0] = -1.0;
    vpxi[1] = -width;
    vpxi[2] = -height;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, &frustum[0]);

    vpxi[0] = -1.0;
    vpxi[1] = width;
    vpxi[2] = -height;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, &frustum[3]);

    vpxi[0] = -1.0;
    vpxi[1] = -width;
    vpxi[2] = height;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, &frustum[6]);
                                                                                                                           
    vpxi[0] = -1.0;                                                                                                        
    vpxi[1] = width;                                                                                                          
    vpxi[2] = height;                                                                                                          
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);                                                                                
    mxv_c(i2bmat, ci, &frustum[9]);                                                                                        
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
    double lt;
    const char* target = "SUN";
    string ref = string("IAU_") + body.c_str();
    const char* abcorr = "LT+S";
    const char* obs = body.c_str();

    spkpos_c(target, et, ref.c_str(), abcorr, obs, sunpos, &lt);
    if (failed_c())
        return;

    cout.precision(16);
    cout << "  Sun position: " << sunpos[0] << " " << sunpos[1] << " " << sunpos[2] << endl;
}


void saveInfoFileHeader(ostream &fout,
                  string startutc,
                  string stoputc)
{
    fout << "START_TIME          = " << startutc << "\n";
    fout << "STOP_TIME           = " << stoputc << "\n";
}

void saveInfoFileFrame(ostream &fout,
                  const double scposb[3],
                  const double boredir[3],
                  const double updir[3],
                  const double frustum[12],
                  const double sunpos[3])
{
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

  1. Body name - either DEIMOS or PHOBOS
  2. kernelfiles - a file containing the kernel files
  3. label file list - a file containing a list of label files to process, one per line
  4. output folder - path to folder where infofiles should be saved to
  5. output file list - path to file in which all files for which an infofile was
                        created will be listed along with their start times.
  
*/
int main(int argc, char** argv)
{
    if (argc < 6)
    {
        cerr << "Usage: create_info_files <body> <kernelfiles> <labelfilelist> <outputfolder> <outputfilelist>" << endl;
        return 1;
    }

    string body = argv[1];
    string kernelfiles = argv[2];
    string labelfilelist = argv[3];
    string outputfolder = argv[4];
    string outputfilelist = argv[5];

    furnsh_c(kernelfiles.c_str());

    erract_c("SET", 1, (char*)"RETURN");

    vector<string> labelfiles = loadFileList(labelfilelist);

    ofstream fout(outputfilelist.c_str());
    if (!fout.is_open())
    {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }

    for (unsigned int i=0; i<labelfiles.size(); ++i)
    {
        cout << "searching for " << body << " in " << labelfiles[i] << endl;
        reset_c();

        string startmet;
        string stopmet;
        string target;
        string frame;
        int naxis1;
        int naxis2;
        string startutc;
        double startet;
        string stoputc;
        double stopet;
        string durstr;
        double duration;
        string expstr;
        double exptime;
        double scposb[3];
        double boredir[3];
        double updir[3];
        double frustum[12];
        double sunPosition[3];

        getFieldsFromFitsHeader(labelfiles[i], startmet, stopmet, durstr, expstr, target, frame, naxis1, naxis2);

        cout << "found:" << endl;

        getEt(startmet, stopmet, startutc, startet, stoputc, stopet);
        if (failed_c())
            continue;

	// calculate duration
	duration = strtod(durstr.c_str(), NULL);

	// calculate exposure time
	exptime = strtod(expstr.c_str(), NULL);

        // start and stop et are initially both set to the mid-observation time

        // calculate start and stop times using duration (seems to be too long)
//        startet = startet - duration * 0.5;
//        stopet = stopet + duration * 0.5;

        // calculate start and stop times using exposure time
        startet = startet - exptime * 127.5;
        stopet = stopet + exptime * 127.5;

        // omit checking for target for pluto images
        if (startet < 473342467)
        {
            // ignore bodies other than the specified one
            if (target != body)
                continue;
        }


        string labelbasename = basename((char*)labelfiles[i].c_str());
        unsigned found = labelbasename.find_last_of(".");
        string infofilename = outputfolder + "/" + labelbasename.substr(0, found) + ".INFO";

        ofstream ifout(infofilename.c_str());
        if (!ifout.is_open())
        {
            cerr << "Error: Unable to open file for writing" << endl;
            exit(1);
        }
        ifout.precision(16);

        saveInfoFileHeader(ifout, startutc, stoputc);

        double deltat = (stopet - startet) / (double)255.0;
//        cout << "startet= " << startet << endl;
//        cout << "stopet= " << stopet << endl;
//        cout << "deltat = " << deltat << endl;

        for (int j=0; j<256; j++)
        {
            double et = startet + (double)j * deltat;
//            cout << "et = " << et << endl;

            getScOrientation(et, body, frame, scposb, boredir, updir, frustum, naxis1, naxis2);
            if (failed_c())
                continue;

            getSunPosition(et, body, sunPosition);
            if (failed_c())
                continue;

            saveInfoFileFrame(ifout, scposb, boredir, updir, frustum, sunPosition);
        }


	fout << labelbasename << " " << startutc << endl;

        cout << "finished " << infofilename << endl;
    }

    return 0;
}

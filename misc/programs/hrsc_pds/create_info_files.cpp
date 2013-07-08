#include <fstream>
#include <iostream>
#include <vector>
#include <string>
#include <cstdlib>
#include <cmath>
#include <libgen.h>
extern "C"
{
#include "SpiceUsr.h"
}

using namespace std;

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

void removeTrailingZ(string& str)
{
    int length = str.size();
    if (str[length-1] == 'Z')
        str = str.substr(0, length-1);
}

void getEt(const string& labelfilename,
           string& startutc,
           double& startet,
           string& stoputc,
           double& stopet)
{
    ifstream fin(labelfilename.c_str());

    if (fin.is_open())
    {
        string str;
        while(true)
        {
            fin >> str;

            if (str == "IMAGE_TIME")
            {
                fin >> str; // the equals character
                fin >> str; // the utc string
                removeTrailingZ(str);

                startutc = str;
                stoputc = str;

                utc2et_c(str.c_str(), &startet);
                stopet = startet;

                cout << labelfilename << " IMAGE_TIME " << startutc << " " << startet << std::endl;
                break;
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
void getScOrientation(double et, string body, string frame, double scposb[3], double boredir[3], double updir[3],
                      double frustum[12])
{
    double lt;
    double i2bmat[3][3];
    double vpxi[3];
    double ci[3];
    double xo,yo;
    const char* target = "MEX";
    string ref = string("IAU_") + body.c_str();
    const char* abcorr = "NONE";
    const char* obs = body.c_str();

    spkpos_c(target, et, ref.c_str(), abcorr, obs, scposb, &lt);
    if (failed_c())
        return;

    cout.precision(16);
    cout << "Spacecraft Position: " << scposb[0] << " " << scposb[1] << " " << scposb[2] << endl;

    pxform_c(frame.c_str(), ref.c_str(), et, i2bmat);
    if (failed_c())
        return;

    //if (frame == "MEX_HRSC_SRC")
    {
        xo=-tan(0.0094/2.0);
        yo=-tan(0.0094/2.0);
    }

    /* First compute the direction of the center pixel */
    vpxi[0] = 0.0;
    vpxi[1] = 0.0;
    vpxi[2] = 1.0;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, boredir);

    /* Then compute the up direction */
    vpxi[0] = 1.0;
    vpxi[1] = 0.0;
    vpxi[2] = 0.0;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, updir);

    /* Now compute the frustum */
    vpxi[0] = xo;
    vpxi[1] = -yo;
    vpxi[2] = 1.0;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, &frustum[0]);

    vpxi[0] = -xo;
    vpxi[1] = -yo;
    vpxi[2] = 1.0;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, &frustum[3]);

    vpxi[0] = xo;
    vpxi[1] = yo;
    vpxi[2] = 1.0;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, &frustum[6]);

    vpxi[0] = -xo;
    vpxi[1] = yo;
    vpxi[2] = 1.0;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, &frustum[9]);
}


/*
  This function computes the position of the sun in the Vesta body frame.

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
    cout << "Sun position: " << sunpos[0] << " " << sunpos[1] << " " << sunpos[2] << endl;
}


void saveInfoFile(string filename,
                  string startutc,
                  string stoputc,
                  const double scposb[3],
                  const double boredir[3],
                  const double updir[3],
                  const double frustum[12],
                  const double sunpos[3])
{
    ofstream fout(filename.c_str());

    if (!fout.is_open())
    {
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

string getCamera(string labelfilename)
{
    ifstream fin(labelfilename.c_str());

    if (fin.is_open())
    {
        string str;
        while(true)
        {
            fin >> str;

            if (str == "DETECTOR_ID")
            {
                fin >> str; // the equals character
                fin >> str; // the detector id

                cout << labelfilename << " DETECTOR_ID " << str << std::endl;

                fin.close();
                return str;
            }
        }
    }
    else
    {
        cerr << "Error: Unable to open file '" << labelfilename << "'" << endl;
        exit(1);
    }

    fin.close();

    return "";
}

/*

  This program creates an info file for each label file. For example the
  file f339b23.INFO is created for the label file f339b23.img_label.

  This program takes the following input arguments:

  1. Body name - either DEIMOS or PHOBOS
  1. kernelfiles - a file containing the kernel files
  2. label file list - a file containing a list of label files to process, one per line
  3. output folder - path to folder where infofiles should be saved to

*/
int main(int argc, char** argv)
{
    if (argc < 4)
    {
        cerr << "Usage: create_info_files <body> <kernelfiles> <labelfilelist> <outputfolder>" << endl;
        return 1;
    }

    string body = argv[1];
    string kernelfiles = argv[2];
    string labelfilelist = argv[3];
    string outputfolder = argv[4];

    furnsh_c(kernelfiles.c_str());

    erract_c("SET", 1, (char*)"RETURN");

    vector<string> labelfiles = loadFileList(labelfilelist);

    for (unsigned int i=0; i<labelfiles.size(); ++i)
    {
        cout << "starting " << labelfiles[i] << endl;
        reset_c();

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

        string camera = getCamera(labelfiles[i]);
        if (camera != "MEX_HRSC_SRC")
        {
            cout << "NOT MEX_HRSC_SRC!" << endl;
            continue;
        }

        getEt(labelfiles[i], startutc, startet, stoputc, stopet);
        if (failed_c())
            continue;

        et = startet + (stopet - startet) / 2.0;

        getScOrientation(et, body, camera, scposb, boredir, updir, frustum);
        if (failed_c())
            continue;

        getSunPosition(et, body, sunPosition);
        if (failed_c())
            continue;

        string labelbasename = basename((char*)labelfiles[i].c_str());
        unsigned found = labelbasename.find_last_of(".");
        string infofilename = outputfolder + "/" + labelbasename.substr(0, found) + ".INFO";
        saveInfoFile(infofilename, startutc, stoputc, scposb, boredir, updir, frustum, sunPosition);
        cout << "finished " << infofilename << endl;
    }

    return 0;
}

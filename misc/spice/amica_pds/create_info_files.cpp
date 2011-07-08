#include <fstream>
#include <iostream>
#include <vector>
#include <string>
#include <cstdlib>
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


void getEt(const string& fitfile,
           string& utc,
           double& et)
{
    ifstream fin(fitfile.c_str());

    if (fin.is_open())
    {
        string str;
        while(true)
        {
            fin >> str;
            
            if (str == "UTC_0")
            {
                fin >> str; // the equals character
                fin >> str; // the utc string
                break;
            }
        }

        utc = str.substr(1, 19);

        utc2et_c(utc.c_str(), &et);
    }
    else
    {
        cerr << "Error: Unable to open file '" << fitfile << "'" << endl;
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
void getScOrientation(double et, double scposb[3], double boredir[3], double updir[3],
                      double frustum[12])
{
    double lt;
    double i2bmat[3][3];
    double vpxi[3];
    double ci[3];
    double zo,yo;
    const char* target = "HAYABUSA";
    const char* ref = "IAU_ITOKAWA";
    const char* abcorr = "NONE";
    const char* obs = "ITOKAWA";
    const char* frame = "HAYABUSA_AMICA";
 
    spkpos_c(target, et, ref, abcorr, obs, scposb, &lt);
    if (failed_c())
        return;
    
    cout.precision(16);
    cout << "Spacecraft Position: " << scposb[0] << " " << scposb[1] << " " << scposb[2] << endl;

    pxform_c(frame, ref, et, i2bmat);
    if (failed_c())
        return;

    zo=-0.049782949;
    yo=-0.049782949;


    /* First compute the direction of the center pixel */
    vpxi[0] = 1.0;
    vpxi[1] = 0.0;
    vpxi[2] = 0.0;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, boredir);

    /* Then compute the up direction */
    vpxi[0] = 0.0;
    vpxi[1] = 0.0;
    vpxi[2] = 1.0;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, updir);

    /* Now compute the frustum */
    vpxi[0] = 1.0;
    vpxi[1] = yo;
    vpxi[2] = zo;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, &frustum[0]);

    vpxi[0] = 1.0;
    vpxi[1] = yo;
    vpxi[2] = -zo;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, &frustum[3]);

    vpxi[0] = 1.0;
    vpxi[1] = -yo;
    vpxi[2] = zo;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, &frustum[6]);

    vpxi[0] = 1.0;
    vpxi[1] = -yo;
    vpxi[2] = -zo;
    vpack_c(vpxi[0], vpxi[1], vpxi[2], ci);
    mxv_c(i2bmat, ci, &frustum[9]);
}


/*
  This function computes the position of the sun in the Itokawa body frame.
   
  Input:
  et:         Ephemeris time
   
  Output:
  sunpos:     The position of the sun in body coordinates
*/
void getSunPosition(double et, double sunpos[3])
{
    double lt;
    const char* target = "SUN";
    const char* ref = "IAU_ITOKAWA";
    const char* abcorr = "LT+S";
    const char* obs = "ITOKAWA";
    
    spkpos_c(target, et, ref, abcorr, obs, sunpos, &lt);
    if (failed_c())
        return;
    
    cout.precision(16);
    cout << "Sun position: " << sunpos[0] << " " << sunpos[1] << " " << sunpos[2] << endl;
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
        cerr << "Error: Unable to open file for writing" << endl;
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

  This program creates an info file for each fit file. For example the
  file N2516167681.INFO is created for the fit file N2516167681.FIT.

  This program takes the following input arguments:

  1. kernelfiles - a file containing the kernel files
  2. fit file list - a file containing a list of fit files to process, one per line

*/
int main(int argc, char** argv)
{
    if (argc < 3)
    {
        cerr << "Usage: create_info_files <kernelfiles> <fitfilelist>" << endl;
        return 1;
    }
    
    string kernelfiles = argv[1];
    string fitfilelist = argv[2];

    furnsh_c(kernelfiles.c_str());

    erract_c("SET", 1, (char*)"RETURN");

    vector<string> fitfiles = loadFileList(fitfilelist);

    for (unsigned int i=0; i<fitfiles.size(); ++i)
    {
        cout << "starting " << fitfiles[i] << endl;
        reset_c();
        
        string utc;
        double et;
        double scposb[3];
        double boredir[3];
        double updir[3];
        double frustum[12];
        double sunPosition[3];
        
        getEt(fitfiles[i], utc, et);
        if (failed_c())
            continue;

        getScOrientation(et, scposb, boredir, updir, frustum);
        if (failed_c())
            continue;

        getSunPosition(et, sunPosition);
        if (failed_c())
            continue;

        int length = fitfiles[i].size();
        string infofilename = fitfiles[i].substr(0, length-4) + ".INFO";
        saveInfoFile(infofilename, utc, scposb, boredir, updir, frustum, sunPosition);
        cout << "finished " << fitfiles[i] << endl;
    }

    return 0;
}

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

std::vector<std::string>
split(const std::string& s, const std::string& delim = " \t")
{
    typedef std::string::size_type size_type;
    std::vector<std::string> tokens;

    const size_type n = s.size();
    size_type i = 0;
    size_type e = 0;
    while (i < n && e < n)
    {
        e = s.find_first_of(delim, i); // Find end of current word
        if (e == std::string::npos)
        {   // Found last word
            tokens.push_back(s.substr(i, n - i));
        }
        else
        {
            if (i != e)
            {
                tokens.push_back(s.substr(i, e - i));
            }
            i = s.find_first_not_of(delim, e); // Find start of next word
        }
    }
    return tokens;
}

vector<pair<string, string> > loadFileList(const string& filelist)
{
    ifstream fin(filelist.c_str());

    vector<pair<string, string> > files;
    
    if (fin.is_open())
    {
        string line;
        while (getline(fin, line))
        {
            vector<string> vals = split(line, ",");
            pair<string, string> pr(vals[0], vals[vals.size()-1]);
            files.push_back(pr);
        }
    }
    else
    {
        cerr << "Error: Unable to open file '" << filelist << "'" << endl;
        exit(1);
    }

    return files;
}

/*
void getEt(const string& fitfile,
           string& utc,
           double& et)
{
    ifstream fin(fitfile.c_str());

    if (fin.is_open())
    {
        double startutc;
        double stoputc;
        string str;
        while(true)
        {
            fin >> str;
            
            if (str == "START_TIME")
            {
                fin >> str; // the equals character
                fin >> str; // the utc string
                utc2et_c(str.c_str(), &startutc);
            }
            if (str == "STOP_TIME")
            {
                fin >> str; // the equals character
                fin >> str; // the utc string
                utc2et_c(str.c_str(), &stoputc);
                break;
            }
        }

        utc = str.substr(1, 19);

        et = startutc + (stoputc - startutc) / 2.0;

        char utcstr[64];
        et2utc_c(et, "ISOC", 3, 64, utcstr);
        utc = utcstr;
    }
    else
    {
        cerr << "Error: Unable to open file '" << fitfile << "'" << endl;
        exit(1);
    }

    fin.close();
}
*/

void getEt(const string& not_used,
           const string& utc,
           double& et)
{
    utc2et_c(utc.c_str(), &et);
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
    double xo,yo;
    const char* target = "DAWN";
    const char* ref = "IAU_VESTA";
    const char* abcorr = "NONE";
    const char* obs = "VESTA";
    const char* frame = "DAWN_FC2";
 
    spkpos_c(target, et, ref, abcorr, obs, scposb, &lt);
    if (failed_c())
        return;
    
    cout.precision(16);
    cout << "Spacecraft Position: " << scposb[0] << " " << scposb[1] << " " << scposb[2] << endl;

    pxform_c(frame, ref, et, i2bmat);
    if (failed_c())
        return;

    xo=-0.095480/2.0;
    yo=-0.095420/2.0;


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
void getSunPosition(double et, double sunpos[3])
{
    double lt;
    const char* target = "SUN";
    const char* ref = "IAU_VESTA";
    const char* abcorr = "LT+S";
    const char* obs = "VESTA";
    
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

    vector<pair<string, string> > fitfiles = loadFileList(fitfilelist);

    for (unsigned int i=0; i<fitfiles.size(); ++i)
    {
        cout << "starting " << fitfiles[i].first << endl;
        reset_c();
        
        double et;
        double scposb[3];
        double boredir[3];
        double updir[3];
        double frustum[12];
        double sunPosition[3];
        
        getEt(fitfiles[i].first, fitfiles[i].second, et);
        if (failed_c())
            continue;

        getScOrientation(et, scposb, boredir, updir, frustum);
        if (failed_c())
            continue;

        getSunPosition(et, sunPosition);
        if (failed_c())
            continue;

        int length = fitfiles[i].first.size();
        string infofilename = fitfiles[i].first.substr(0, length-4) + ".INFO";
        saveInfoFile(infofilename, fitfiles[i].second, scposb, boredir, updir, frustum, sunPosition);
        cout << "finished " << fitfiles[i].first << endl;
    }

    return 0;
}

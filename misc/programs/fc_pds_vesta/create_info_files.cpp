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

void removeSurroundingQuotes(string& str)
{
    int length = str.size();
    if (str[length-1] == '"')
        str = str.substr(0, length-1);
    if (str[0] == '"')
        str = str.substr(1);
}

void getEt(const string& fitfile,
           string& startutc,
           double& startet,
           string& stoputc,
           double& stopet)
{
    int length = fitfile.size();
    string lblfilename = fitfile.substr(0, length-4) + ".LBL";
    
    ifstream fin(lblfilename.c_str());

    int found;
    int id;
    bodn2c_c("DAWN", &id, &found);
    
    if (fin.is_open())
    {
        string str;
        while(true)
        {
            fin >> str;
            
            if (str == "SPACECRAFT_CLOCK_START_COUNT")
            {
                fin >> str; // the equals character
                fin >> str; // the count string
                removeSurroundingQuotes(str);

                scs2e_c(id, str.c_str(), &startet);
                char utc[25];
                et2utc_c ( startet , "ISOC", 3, 25, utc );
                startutc = utc;
            
                cout << fitfile << " SPACECRAFT_CLOCK_START_COUNT " << str << " " << startutc << " " << startet << std::endl;
            }
            if (str == "SPACECRAFT_CLOCK_STOP_COUNT")
            {
                fin >> str; // the equals character
                fin >> str; // the count string
                removeSurroundingQuotes(str);

                scs2e_c(id, str.c_str(), &stopet);
                char utc[25];
                et2utc_c ( stopet , "ISOC", 3, 25, utc );
                stoputc = utc;
                
                cout << fitfile << " SPACECRAFT_CLOCK_STOP_COUNT " << str << " " << stoputc << " " << stopet << std::endl;
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
void getScOrientation(double et, int filter, double scposb[3], double boredir[3], double updir[3],
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

    if (filter == 1)
    {
        xo=-tan(0.095480/2.0);
        yo=-tan(0.095420/2.0);
    }
    else if (filter == 2)
    {
        xo=-tan(0.095461/2.0);
        yo=-tan(0.095401/2.0);
    }
    else if (filter == 3)
    {
        xo=-tan(0.095499/2.0);
        yo=-tan(0.095439/2.0);
    }
    else if (filter == 4)
    {
        xo=-tan(0.095452/2.0);
        yo=-tan(0.095392/2.0);
    }
    else if (filter == 5)
    {
        xo=-tan(0.095427/2.0);
        yo=-tan(0.095367/2.0);
    }
    else if (filter == 6)
    {
        xo=-tan(0.095476/2.0);
        yo=-tan(0.095416/2.0);
    }
    else if (filter == 7)
    {
        xo=-tan(0.095492/2.0);
        yo=-tan(0.095432/2.0);
    }
    else if (filter == 8)
    {
        xo=-tan(0.095286/2.0);
        yo=-tan(0.095226/2.0);
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

int getFilter(string fitfilename)
{
    int n = fitfilename.size();
    string filt = fitfilename.substr(n-6, n-5);
    return atoi(filt.c_str());
}

/*

  This program creates an info file for each fit file. For example the
  file N2516167681.INFO is created for the fit file N2516167681.FIT.

  This program takes the following input arguments:

  1. kernelfiles - a file containing the kernel files
  2. fit file list - a file containing a list of fit files to process, one per line
  3. output folder - path to folder where infofiles should be saved to

*/
int main(int argc, char** argv)
{
    if (argc < 4)
    {
        cerr << "Usage: create_info_files <kernelfiles> <fitfilelist> <outputfolder>" << endl;
        return 1;
    }
    
    string kernelfiles = argv[1];
    string fitfilelist = argv[2];
    string outputfolder = argv[3];

    furnsh_c(kernelfiles.c_str());

    erract_c("SET", 1, (char*)"RETURN");

    vector<string> fitfiles = loadFileList(fitfilelist);

    for (unsigned int i=0; i<fitfiles.size(); ++i)
    {
        cout << "starting " << fitfiles[i] << endl;
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
        
        getEt(fitfiles[i], startutc, startet, stoputc, stopet);
        if (failed_c())
            continue;

        et = startet + (stopet - startet) / 2.0;

        int filter = getFilter(fitfiles[i]);
        getScOrientation(et, filter, scposb, boredir, updir, frustum);
        if (failed_c())
            continue;

        getSunPosition(et, sunPosition);
        if (failed_c())
            continue;

        string fitbasename = basename((char*)fitfiles[i].c_str());
        int length = fitbasename.size();
        string infofilename = outputfolder + "/" + fitbasename.substr(0, length-4) + ".INFO";
        saveInfoFile(infofilename, startutc, stoputc, scposb, boredir, updir, frustum, sunPosition);
        cout << "finished " << infofilename << endl;
    }

    return 0;
}

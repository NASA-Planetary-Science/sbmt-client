#include <fstream>
#include <iostream>
#include <vector>
#include <string>
#include <algorithm>
#include <stdlib.h>
extern "C"
{
#include "SpiceUsr.h"
}

using namespace std;


// Remove initial and trailing whitespace from string. Modifies string in-place
void trim(string& s)
{
    const size_t si = s.find_first_not_of(" \t");
    if (si != string::npos)
    {
        const size_t ei = s.find_last_not_of(" \t");
        const size_t l = (ei == string::npos ? ei : ei - si + 1);
        s = s.substr(si, l);
    }
    else
    {
        s = "";
    }
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


void loadSumFile(const string& sumfile,
                 string& utc,
                 double& et)
{
    ifstream fin(sumfile.c_str());

    if (fin.is_open())
    {
        string name;
        
        getline(fin, name);
        trim(name);

        getline(fin, utc);
        trim(utc);
	
        // Replace the third space with a T
        utc[11] = 'T';

        // Replace remaining spaces with dashes
        replace(utc.begin(), utc.end(), ' ', '-');

        // Replace the month abbreviation with an integer. Only SEP,
        // OCT, and NOV are needed.
        string month = utc.substr(5, 3);
        if (month == "SEP")
            month = "09";
        else if (month == "OCT")
            month = "10";
        else if (month == "NOV")
            month = "11";
        else
        {
            cerr << "Error: month " << month << " not recognized" << endl;
            exit(1);
        }
        
        utc = utc.substr(0, 5) + month + utc.substr(8);

        utc2et_c(utc.c_str(), &et);
    }
    else
    {
        cerr << "Error: Unable to open file '" << sumfile << "'" << endl;
        exit(1);
    }

    fin.close();
}

/*
   This function computes the position of the sun in the Eros body frame.
   
   Input:
     et:         Ephemeris time
   
   Output:
     sunpos:     The position of the sun in body cooridinates
*/
void getSunPosition(double et, double sunpos[3])
{
	double lt;
	const char* target = "SUN";
	const char* ref = "IAU_ITOKAWA";
	const char* abcorr = "LT+S";
	const char* obs = "ITOKAWA";
	
	spkpos_c(target, et, ref, abcorr, obs, sunpos, &lt);

    cout.precision(16);
	cout << "Sun position: " << sunpos[0] << " " << sunpos[1] << " " << sunpos[2] << endl;
}

void saveInfoFile(string filename, string utc, double sunpos[3])
{
    ofstream fout(filename.c_str());

    if (!fout.is_open())
    {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }

    fout.precision(16);

    fout << "START_TIME      = " << utc << "\n";
    fout << "STOP_TIME       = " << utc << "\n";
    fout << "SUN_POSITION_LT = ( ";
    fout << scientific << sunpos[0] << " , ";
    fout << scientific << sunpos[1] << " , ";
    fout << scientific << sunpos[2] << " )\n";
}

/*

  This program creates an info file for each sumfile. For example the
  file N2516167681.INFO is created for the sumfile N2516167681.SUM.

  This program takes the following input arguments:

  1. kernelfiles - a file containing the kernel files
  2. sum file list - a file containing a list of sum files to process, one per line

*/
int main(int argc, char** argv)
{
    if (argc < 3)
    {
        cout << "Usage: create_info_files <kernelfiles> <sumfilelist>" << endl;
        return 1;
    }
    
    string kernelfiles = argv[1];
    string sumfilelist = argv[2];

    furnsh_c(kernelfiles.c_str());

    vector<string> sumfiles = loadFileList(sumfilelist);

    for (unsigned int i=0; i<sumfiles.size(); ++i)
    {
        string utc;
        double et;
        
        loadSumFile(sumfiles[i], utc, et);

        double sunPosition[3];
        
        getSunPosition(et, sunPosition);

        int length = sumfiles[i].size();
        string infofilename = sumfiles[i].substr(0, length-4) + ".INFO";
        saveInfoFile(infofilename, utc, sunPosition);
    }

    return 0;
}

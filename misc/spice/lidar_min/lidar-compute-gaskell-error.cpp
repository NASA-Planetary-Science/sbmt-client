#include <iostream>
#include <fstream>
#include <utility>
#include <string>
#include <vector>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
extern "C"
{
#include "SpiceUsr.h"
}

using namespace std;

const int ignore_list[] = {
    6,7,14,15,16,17,18,19,20,21,22,23,24,30,31,32,33,34,35,36,37,38,39,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,122,123,124,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255,256,257,258,259,260,261,262,263,264,265,266,268,269,270,271,272,273,274,275,276,277,278,279,280,281,282,283,284,285,286,287,288,289,290,291,292,293,294,295,296,297,298,299,300,369,410,411,412,413,417,418,419,420,421,422,658,659,661,663,664,665,707,708,709,746,762,763,764,765,766,767,768,769
};

bool isInIgnoreList(int idx)
{
    int size = sizeof(ignore_list) / sizeof(int);
    for (int i=0; i<size; ++i)
        if (ignore_list[i] == idx)
            return true;
    return false;
}

// Remove initial and trailing whitespace from string. Modifies string in-place
void trim(std::string& s)
{
    const std::size_t si = s.find_first_not_of(" \t");
    if (si != std::string::npos)
    {
        const std::size_t ei = s.find_last_not_of(" \t");
        const std::size_t l = (ei == std::string::npos ? ei : ei - si + 1);
        s = s.substr(si, l);
    }
    else
    {
        s = "";
    }
}

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

void getGaskellPos(const string& sumfile, double scpos[3])
{
    ifstream fin(sumfile.c_str());

    if (fin.is_open())
    {
        string line;
        int count = 0;
        while (getline(fin, line))
        {
            if (count == 4)
            {
                trim(line);
                vector<string> fields = split(line);

                for (int i=0; i<3; ++i)
                {
                    size_t pos = fields[i].find( "D" );
                    if ( pos != string::npos )
                        fields[i].replace( pos, 1, "E" );
                }
                
                scpos[0] = -atof(fields[0].c_str());
                scpos[1] = -atof(fields[1].c_str());
                scpos[2] = -atof(fields[2].c_str());
                break;
            }

            ++count;
        }
    }
    else
    {
        cerr << "Error: Unable to open file '" << sumfile << "'" << endl;
        exit(1);
    }
}


vector<pair<string, double> > loadFitTimes(const string& filelist)
{
    ifstream fin(filelist.c_str());

    vector<pair<string, double> > files;
    
    int found;
    int id;
    bodn2c_c("HAYABUSA", &id, &found);
    
    if (fin.is_open())
    {
        string line;
        while (getline(fin, line))
        {
            vector<string> fields = split(line);

            // convert met to ephemeris time
            double et = 0.0;
            scs2e_c(id, (fields[0]).c_str(), &et);
            char utc[25];
            et2utc_c ( et , "C", 3, 25, utc );
            
            //cout << fields[1] << " " << utc << std::endl;
            pair<string, double> p(fields[1], et);
            files.push_back(p);
        }
    }
    else
    {
        cerr << "Error: Unable to open file '" << filelist << "'" << endl;
        exit(1);
    }

    return files;
}


int main(int argc, char** argv)
{
    if (argc < 4)
    {
        cerr << "Usage: lidar-compute-gaskell-error <kernelfiles> <fittimeslist> <sumfilefolder>" << endl;
        return 1;
    }
    
    string kernelfiles = argv[1];
    string fittimeslist = argv[2];
    string sumfilefolder = argv[3];

    furnsh_c(kernelfiles.c_str());
   
    erract_c("SET", 1, (char*)"RETURN");

    vector<pair<string, double> > fittimes = loadFitTimes(fittimeslist);

    double meandist = 0.0;
    int count = 0;
    for (unsigned int i=0; i<fittimes.size()-1; ++i)
    {
        if (isInIgnoreList(i))
            continue;
        
        reset_c();

        string sumfile = sumfilefolder + "/N" + fittimes[i].first.substr(3, 10) + ".SUM";
        double scpos_gas[3];
        getGaskellPos(sumfile, scpos_gas);

        double scpos_spice[3];

        const char* target = "HAYABUSA";
        const char* ref = "IAU_ITOKAWA";
        const char* abcorr = "NONE";
        const char* obs = "ITOKAWA";
        double lt;

        spkpos_c(target, fittimes[i].second, ref, abcorr, obs, scpos_spice, &lt);
        if (failed_c())
            continue;

        double dist = vdist_c(scpos_gas, scpos_spice);
        if (dist >= 0.1)
        {
            //cout << i << ",";
            continue;
        }
        
        meandist += dist;
        cout << "starting " << (i+1) << " / " << fittimes.size() << " " << fittimes[i].first << " " << dist << endl;
        ++count;
    }

    meandist /= (double)count;

    cout << "meandist: " << meandist << " count: " << count << endl;
    
    return 0;
}

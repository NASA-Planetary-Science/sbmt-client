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

/************************************************************************
* Constants
************************************************************************/
#define MAX_NUMBER_POINTS 2000000
#define LINE_SIZE 1024
#define UTC_SIZE 128
#define TRACK_BREAK_THRESHOLD 1.0


/************************************************************************
* Structure for storing a lidar point
************************************************************************/
struct LidarPoint
{
    double time;
    double scpos[3];
};


/************************************************************************
* Global varaiables
************************************************************************/

/* Array for storing all lidar points */
struct LidarPoint g_points[MAX_NUMBER_POINTS];
int g_actual_number_points;

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

void loadPoints(int argc, char** argv)
{
    printf("Loading data\n");
    int i;
    int count = 0;
    for (i=4; i<argc; ++i)
    {
        const char* filename = argv[i];
        FILE *f = fopen(filename, "r");
        if (f == NULL)
        {
            printf("Could not open %s", filename);
            exit(1);
        }

        char line[LINE_SIZE];
        char utc[UTC_SIZE];
        double sx;
        double sy;
        double sz;
        
        while ( fgets ( line, sizeof line, f ) != NULL ) /* read a line */
        {
            if (count >= MAX_NUMBER_POINTS)
            {
                printf("Error: Max number of allowable points exceeded!");
                exit(1);
            }
            
            sscanf(line, "%*s %s %*s %lf %lf %lf", utc, &sx, &sy, &sz);


            struct LidarPoint point;

            utc2et_c(utc, &point.time);

            point.scpos[0] = sx;
            point.scpos[1] = sy;
            point.scpos[2] = sz;
            
            g_points[count] = point;

            ++count;
        }

        printf("points read %d\n", count);
        fflush(NULL);
        fclose ( f );
    }
    
    g_actual_number_points = count;
    printf("Finished loading data\n\n\n");
}

bool getLidarPosAtTime(double time, double pos[3])
{
    if (time < g_points[0].time)
    {
        return false;
    }
    
    for (int i=1; i<g_actual_number_points; ++i)
    {
        struct LidarPoint pt1 = g_points[i];
        double t1 = pt1.time;

        if (time == t1)
        {
            pos[0] = pt1.scpos[0];
            pos[1] = pt1.scpos[1];
            pos[2] = pt1.scpos[2];
            return true;
        }
        else if (time < t1)
        {
            double t0 = g_points[i-1].time;
            if (time - t0 > TRACK_BREAK_THRESHOLD || t1 - time > TRACK_BREAK_THRESHOLD)
            {
                return false;
            }
            else
            {
                struct LidarPoint pt0 = g_points[i-1];
                
                // do linear interpolation for each dimension
                for (int j = 0; j<3; ++j)
                {
                    pos[j] = pt0.scpos[j] + ( (time-t0) * (pt1.scpos[j]-pt0.scpos[j]) ) / (t1 - t0);
                }

                return true;
            }
        }
    }

    return false;
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
            scs2e_c(id, (fields[0]+":0").c_str(), &et);
            char utc[25];
            et2utc_c ( et , "C", 3, 25, utc );
            
            //cout << fields[1] << " " << utc << " " << et << std::endl;
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
    if (argc < 5)
    {
        cerr << "Usage: lidar-compute-gaskell-error <kernelfiles> <fittimeslist> <sumfilefolder> <lidarfile1> [<lidarfile2> ...]" << endl;
        return 1;
    }
    
    string kernelfiles = argv[1];
    string fittimeslist = argv[2];
    string sumfilefolder = argv[3];

    furnsh_c(kernelfiles.c_str());
   
    erract_c("SET", 1, (char*)"RETURN");

    loadPoints(argc, argv);

    vector<pair<string, double> > fittimes = loadFitTimes(fittimeslist);

    double meandist = 0.0;
    int count = 0;
    for (unsigned int i=0; i<fittimes.size()-1; ++i)
    {
        reset_c();

        
        string sumfile = sumfilefolder + "/N" + fittimes[i].first.substr(3, 10) + ".SUM";
        double scpos_gas[3];
        getGaskellPos(sumfile, scpos_gas);


        double scpos_spice[3];
        bool isInRange = getLidarPosAtTime(fittimes[i].second, scpos_spice);
        if (!isInRange)
        {
            //cout << "image " << i << " " << fittimes[i].first << " not in lidar range" << endl;
            //cout << i << ",";
            continue;
        }

        /*
          // The following is just for a consistency check to make sure
          // our interpolation is the same as you would get using the
          // spk kernels.
          
        double scpos_spice2[3];
        const char* target = "HAYABUSA";
        const char* ref = "IAU_ITOKAWA";
        const char* abcorr = "NONE";
        const char* obs = "ITOKAWA";
        double lt;
        spkpos_c(target, fittimes[i].second, ref, abcorr, obs, scpos_spice2, &lt);
        if (failed_c())
            continue;
        cout << "distance between spice and mine " << vdist_c(scpos_spice2, scpos_spice) << endl;
        */

        double dist = vdist_c(scpos_gas, scpos_spice);

        //if (dist >= 0.1)
        //{
        //    cout << i << ",";
        //    continue;
        //}
        
        meandist += dist;
        cout << "starting " << (i+1) << " / " << fittimes.size() << " " << fittimes[i].first << " " << dist << endl;
        ++count;
    }

    meandist /= (double)count;

    cout << "meandist: " << meandist << " count: " << count << endl;
    
    return 0;
}

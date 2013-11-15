#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <libgen.h>
#include "closest-point-vtk.h"
#include "lidardata.h"


using namespace std;


void writeStats(const string& outputfile,
                const vector<string>& trackfiles,
                const vector<Track>& tracks)
{
    ofstream fout(outputfile.c_str());

    if (fout.is_open())
    {
        fout << "track,min-distance,max-distance,rms\n";

        for (size_t i=0;i<tracks.size(); ++i)
        {
            double minError, maxError, rms;
            computeTrackStats(tracks.at(i), minError, maxError, rms);

            string name = trackfiles.at(i);
            fout << basename(&name[0]) << "," << minError << "," << maxError << "," << rms << "\n";

        }

        double minError, maxError, rms;
        computeTracksStats(tracks, minError, maxError, rms);

        fout << "all," << minError << "," << maxError << "," << rms << "\n";
    }
    else
    {
        cerr << "Error: Unable to open file '" << outputfile << "'" << endl;
        exit(1);
    }
}


/************************************************************************
* This program loads a group of track files and prints out information
* regarding each track such as min and max errors as well as rms. It
* also prints out such information for all the tracks as a whole.
************************************************************************/
int main(int argc, char** argv)
{
    if (argc < 4)
    {
        cout << "Usage: lidar-compute-track-stats <vtkfile> <output-file> <track1> [<track2> <track3> ...]" << endl;
        return 1;
    }

    string vtkfile = argv[1];
    string outputfile = argv[2];

    vector<string> trackfiles;
    for (int i=3; i<argc; ++i)
        trackfiles.push_back(argv[i]);

    initializeVtk(vtkfile.c_str());

    vector<Track> tracks;
    for (size_t i=0;i<trackfiles.size(); ++i)
        tracks.push_back(LidarData::loadTrack(trackfiles.at(i), false));

    writeStats(outputfile, trackfiles, tracks);

    return 0;
}

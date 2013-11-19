#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <libgen.h>
#include "SpiceUsr.h"
#include "closest-point-vtk.h"
#include "lidardata.h"


using namespace std;


void writeStats(const string& outputfile,
                const vector<string>& trackfiles,
                const vector<Track>& tracksBefore,
                const vector<Track>& tracksAfter)
{
    ofstream fout(outputfile.c_str());

    if (!fout.is_open())
    {
        cerr << "Error: Unable to open file '" << outputfile << "'" << endl;
        exit(1);
    }

    fout.precision(16);
    fout << "track,start time,end time,x translation,y translation,z translation,total translation,"
         << "min distance before,max distance before,rms before,mean distance before,stdev before,"
         << "min distance after,max distance after,rms after,mean distance after,stdev after\n";

    for (size_t i=0;i<tracksBefore.size(); ++i)
    {
        const Track& trackBefore = tracksBefore.at(i);
        const Track& trackAfter = tracksAfter.at(i);

        if (trackBefore.size() != trackAfter.size())
        {
            cerr << "Error: track sizes not the same." << endl;
            exit(1);
        }

        double minErrorBefore, maxErrorBefore, rmsBefore, meanErrorBefore, stdBefore;
        computeTrackStats(trackBefore, minErrorBefore, maxErrorBefore, rmsBefore, meanErrorBefore, stdBefore);
        double minErrorAfter, maxErrorAfter, rmsAfter, meanErrorAfter, stdAfter;
        computeTrackStats(trackAfter, minErrorAfter, maxErrorAfter, rmsAfter, meanErrorAfter, stdAfter);

        double translation[3];
        computeMeanTranslationBetweenTracks(trackBefore, trackAfter, translation);

        double distance = vnorm_c(translation);

        string name = trackfiles.at(i);

        fout << basename(&name[0]) << ","
                                   << trackBefore[0].utc << ","
                                   << trackAfter[trackAfter.size()-1].utc << ","
                                   << translation[0] << ","
                                   << translation[1] << ","
                                   << translation[2] << ","
                                   << distance << ","
                                   << minErrorBefore << ","
                                   << maxErrorBefore << ","
                                   << rmsBefore << ","
                                   << meanErrorBefore << ","
                                   << stdBefore << ","
                                   << minErrorAfter << ","
                                   << maxErrorAfter << ","
                                   << rmsAfter << ","
                                   << meanErrorAfter << ","
                                   << stdAfter << "\n";
    }

    // The final line shows stats for a new track consisting of all other tracks combined together
    Track allTracksBefore = concatTracks(tracksBefore, true);
    Track allTracksAfter = concatTracks(tracksAfter, true);

    double minErrorBefore, maxErrorBefore, rmsBefore, meanErrorBefore, stdBefore;
    computeTrackStats(allTracksBefore, minErrorBefore, maxErrorBefore, rmsBefore, meanErrorBefore, stdBefore);

    double minErrorAfter, maxErrorAfter, rmsAfter, meanErrorAfter, stdAfter;
    computeTrackStats(allTracksAfter, minErrorAfter, maxErrorAfter, rmsAfter, meanErrorAfter, stdAfter);

    double translation[3];
    computeMeanTranslationBetweenTracks(allTracksBefore, allTracksAfter, translation);

    double distance = vnorm_c(translation);

    fout << "all,"
         << allTracksBefore[0].utc << ","
         << allTracksBefore[allTracksBefore.size()-1].utc << ","
         << translation[0] << ","
         << translation[1] << ","
         << translation[2] << ","
         << distance << ","
         << minErrorBefore << ","
         << maxErrorBefore << ","
         << rmsBefore << ","
         << meanErrorBefore << ","
         << stdBefore << ","
         << minErrorAfter << ","
         << maxErrorAfter << ","
         << rmsAfter << ","
         << meanErrorAfter << ","
         << stdAfter << "\n";

    fout.close();
}


/************************************************************************
* This program loads a group of track files and prints out information
* regarding each track such as min and max errors as well as rms. It
* also prints out such information for all the tracks as a whole.
************************************************************************/
int main(int argc, char** argv)
{
    if (argc < 5)
    {
        cout << "Usage: lidar-compute-track-stats <vtkfile> <kernelfile> <output-file> <track1-before> <track1-after> [<track2-before> <track2-after> ...]" << endl;
        return 1;
    }

    string vtkfile = argv[1];
    string kernelfile = argv[2];
    string outputfile = argv[3];

    vector<string> trackfilesBefore;
    vector<string> trackfilesAfter;
    for (int i=4; i<argc; ++i)
    {
        trackfilesBefore.push_back(argv[i]);
        ++i;
        trackfilesAfter.push_back(argv[i]);
    }

    furnsh_c(kernelfile.c_str());

    initializeVtk(vtkfile.c_str());

    vector<Track> tracksBefore;
    for (size_t i=0;i<trackfilesBefore.size(); ++i)
        tracksBefore.push_back(LidarData::loadTrack(trackfilesBefore.at(i), true));

    vector<Track> tracksAfter;
    for (size_t i=0;i<trackfilesAfter.size(); ++i)
        tracksAfter.push_back(LidarData::loadTrack(trackfilesAfter.at(i), true));

    writeStats(outputfile, trackfilesBefore, tracksBefore, tracksAfter);

    return 0;
}

#include <stdio.h>
#include "SpiceUsr.h"
#include "track-optimizer.h"
#include "lidardata.h"
#include "closest-point-vtk.h"


/************************************************************************
* This program optimizes the location of a track by trying to compute the best
* translation that best aligns the track with a shape model. This program
* requires 3 arguments to run in this order:
* 1. vtkfile - the shape model in VTK format
* 2. outputfile - the name to be given to the optimized track file
* 3. inputfile - path to input track file to be optimized
************************************************************************/
int main(int argc, char** argv)
{
    if (argc < 4)
    {
        printf("Usage: lidar-min-icp <vtkfile> <outputfile> <inputfile>\n");
        return 1;
    }

    char* vtkfile = argv[1];
    const char* const outfile = argv[2];
    const char* const infile = argv[3];

    initializeVtk(vtkfile);

    Track track = LidarData::loadTrack(infile, false);

    TrackOptimizer trackOptimizer;
    trackOptimizer.setTrack(track);
    trackOptimizer.optimize();

    Track optimizedTrack = trackOptimizer.getOptimizedTrack();

    LidarData::saveTrack(outfile, optimizedTrack);

    double translation[3];
    trackOptimizer.getOptimalTranslation(translation);
    printf("\nOptimal Translation:\n%.16g %.16g %.16g\n",
            translation[0],
            translation[1],
            translation[2]);

    return 0;
}

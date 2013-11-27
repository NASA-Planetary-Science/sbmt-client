#include <stdio.h>
#include "SpiceUsr.h"
#include "track-optimizer.h"
#include "lidardata.h"
#include "closest-point-vtk.h"


/************************************************************************
* This program optimizes the location of a track by trying to compute the best
* translation that best aligns the track with a shape model. This program
* requires 4 arguments to run in this order:
* 1. vtkfile - the shape model in VTK format
* 2. kernelfile - SPICE leap second kernel file
* 3. outputfile - the name to be given to the optimized track file
* 4. inputfile - path to input track file to be optimized
************************************************************************/
int main(int argc, char** argv)
{
    if (argc < 5)
    {
        printf("Usage: lidar-min-icp <vtkfile> <kernelfiles> <outputfile> <inputfile>\n");
        return 1;
    }

    char* vtkfile = argv[1];
    const char* const kernelfiles = argv[2];
    const char* const outfile = argv[3];
    const char* const infile = argv[4];

    initializeVtk(vtkfile);

    furnsh_c(kernelfiles);

    Track track = LidarData::loadTrack(infile, true);

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

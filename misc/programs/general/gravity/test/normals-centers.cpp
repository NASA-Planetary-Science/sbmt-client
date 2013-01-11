#include <iostream>
#include <fstream>
#include <libgen.h>
#include <stdlib.h>
#include "SpiceUsr.h"
#include "platemodel.h"

using namespace std;

// This program takes a platefile of an ellipsoid (i.e. it assumes the platemodel approximates and ellipsoid)
// as well as it's dimensions (semimajor and semiminor axes) and outputs 3 files containing the following:
// 1. the centers of each plate
// 2. the normals of each plate
// 3. the points on the ellipsoid intersected by rays starting from the plate centers and pointing
//    in the direction of the plate normals.
int main(int argc, char** argv)
{
    if (argc < 4)
    {
        cerr << "Usage: <platemodelfile-of-ellipsoid> <ellipsoid-semimajor-axis> <ellipsoid-semiminor-axis>" << endl;
        exit(1);
    }

    char* pltfile = argv[1];
    double a = atof(argv[2]);
    double c = atof(argv[3]);

    string pltfilebasename = basename(pltfile);

    string outputCenters = pltfilebasename + "-centers.txt";
    ofstream foutC(outputCenters.c_str());
    if (!foutC.is_open())
    {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }
    foutC.precision(16);

    string outputShiftedCenters = pltfilebasename + "-shifted-centers.txt";
    ofstream foutS(outputShiftedCenters.c_str());
    if (!foutS.is_open())
    {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }
    foutS.precision(16);

    string outputNormals = pltfilebasename + "-normals.txt";
    ofstream foutN(outputNormals.c_str());
    if (!foutN.is_open())
    {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }
    foutN.precision(16);

    Platemodel* polyData = new Platemodel();
    polyData->load(pltfile);

    double center[3];
    double normal[3];
    double shiftedCenter[3];

    SpiceBoolean found = 0;

    int numFaces = polyData->getNumberOfPlates();
    for (int i=0; i<numFaces; ++i)
    {
        polyData->getPlateCenter(i, center);
        polyData->getNormal(i, normal);

        // shift the center along the normal until it intersects the ellipsoid
        surfpt_c(center, normal, a, a, c, shiftedCenter, &found);

        if (!found)
        {
            cout << i << " Serious error: not found! " << endl;
            exit(1);
        }

        foutC << center[0] << " " << center[1] << " " << center[2] << endl;
        foutS << shiftedCenter[0] << " " << shiftedCenter[1] << " " << shiftedCenter[2] << endl;
        foutN << normal[0] << " " << normal[1] << " " << normal[2] << endl;
    }

    foutC.close();
    foutS.close();
    foutN.close();

    return 0;
}

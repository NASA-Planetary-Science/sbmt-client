#include <iostream>
#include <fstream>
#include <libgen.h>
#include <vector>
#include <string>
#include <stdlib.h>
#include "SpiceUsr.h"
#include "platemodel.h"
#include "util.h"
#include "mathutil.h"


using namespace std;


struct PlateData
{
    double potential;
    double acc[3];
};


static void loadPotential(const string& filename, vector<PlateData>& plateData)
{
    ifstream fin(filename.c_str());

    if (fin.is_open())
    {
        int i = 0;
        string line;
        while (getline(fin, line))
        {
            trim(line);
            double pot = atof(line.c_str());
            plateData[i].potential = pot;
            ++i;
        }
    }
    else
    {
        cerr << "Error: Unable to open file '" << filename << "'" << endl;
        exit(1);
    }
}


static void loadAcceleration(const string& filename, vector<PlateData>& plateData)
{
    ifstream fin(filename.c_str());

    if (fin.is_open())
    {
        int i = 0;
        string line;
        while (getline(fin, line))
        {
            trim(line);
            vector<string> tokens = split(line);
            plateData[i].acc[0] = atof(tokens[0].c_str());
            plateData[i].acc[1] = atof(tokens[1].c_str());
            plateData[i].acc[2] = atof(tokens[2].c_str());
            ++i;
        }
    }
    else
    {
        cerr << "Error: Unable to open file '" << filename << "'" << endl;
        exit(1);
    }
}


static double computeRefPotential(Platemodel* polyData, vector<PlateData> platedata)
{
    int numFaces = polyData->getNumberOfPlates();

    double potTimesAreaSum = 0.0;
    double totalArea = 0.0;
    for (int i=0; i<numFaces; ++i)
    {
        double area = polyData->getPlateArea(i);
        potTimesAreaSum += platedata[i].potential * area;
        totalArea += area;
    }

    return potTimesAreaSum / totalArea;
}


static void usage()
{
    cout << "This program computes the elevation, slope and acceleration magnitude at\n"
         << "the plate centers of a shape model. It uses the gravitational acceleration\n"
         << "vector and potential files computed using the gravity program.\n"
         << "Usage: elevation-slope <platemodel-file> <potential-file> <acceleration-vector-file>\n";
    exit(1);
}


int main(int argc, char** argv)
{
    if (argc < 4)
        usage();

    char* pltfile = argv[1];
    char* potentialfile = argv[2];
    char* accfile = argv[3];

    string pltfilebasename = basename(argv[1]);
    string outputElev = pltfilebasename + "-elevation.txt";
    string outputSlope = pltfilebasename + "-slope.txt";
    string outputAccMag = pltfilebasename + "-acceleration-magnitude.txt";

    Platemodel* polyData = new Platemodel();
    polyData->load(pltfile);

    int numFaces = polyData->getNumberOfPlates();

    vector<PlateData> platedata;
    platedata.resize(numFaces);

    loadPotential(potentialfile, platedata);

    double refPotential = computeRefPotential(polyData, platedata);

    cout.precision(16);
    cout << scientific << "reference potential = " << refPotential << endl;

    loadAcceleration(accfile, platedata);

    ofstream foutE(outputElev.c_str());
    if (!foutE.is_open())
    {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }
    foutE.precision(16);

    ofstream foutS(outputSlope.c_str());
    if (!foutS.is_open())
    {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }
    foutS.precision(16);

    ofstream foutAM(outputAccMag.c_str());
    if (!foutAM.is_open())
    {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }
    foutAM.precision(16);

    for (int i=0; i<numFaces; ++i)
    {
        // Compute elevation
        double accMag = Norm(platedata[i].acc);
        double elevation = (platedata[i].potential - refPotential) / accMag;

        // Compute slope
        double cellNormal[3];
        polyData->getNormal(i, cellNormal);
        double negaticeAcc[3];
        negaticeAcc[0] = -platedata[i].acc[0];
        negaticeAcc[1] = -platedata[i].acc[1];
        negaticeAcc[2] = -platedata[i].acc[2];
        double slope = vsep_c(cellNormal, negaticeAcc) * 180.0 / M_PI;
        if (slope > 90.0)
            accMag = -accMag;

        foutE << elevation << endl;
        foutS << slope << endl;
        foutAM << accMag << endl;
    }

    foutE.close();
    foutS.close();
    foutAM.close();

    return 0;
}

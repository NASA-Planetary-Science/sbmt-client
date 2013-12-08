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
    double accMag;
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
        double acc[3];
        while (getline(fin, line))
        {
            trim(line);
            vector<string> tokens = split(line);
            acc[0] = atof(tokens[0].c_str());
            acc[1] = atof(tokens[1].c_str());
            acc[2] = atof(tokens[2].c_str());

            double accMag = Normalize(acc);
            plateData[i].acc[0] = acc[0];
            plateData[i].acc[1] = acc[1];
            plateData[i].acc[2] = acc[2];
            plateData[i].accMag = accMag;
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
    cout << "This program computes the elevation and slope at the plate centers of a \n"
         << "shape model. It uses the gravitational acceleration vector and potential \n"
         << "files computed using the gravity program.\n"
         << "Usage: elevation-slope <platemodel-file> <potential-file> <acceleration-vector-file>\n";
    exit(0);
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

    Platemodel* polyData = new Platemodel();
    polyData->load(pltfile);

    int numFaces = polyData->getNumberOfPlates();

    vector<PlateData> platedata;
    platedata.resize(numFaces);

    loadPotential(potentialfile, platedata);
    loadAcceleration(accfile, platedata);

    double refPotential = computeRefPotential(polyData, platedata);

    cout.precision(16);
    cout << scientific << "reference potential = " << refPotential << endl;

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

    for (int i=0; i<numFaces; ++i)
    {
        // Compute elevation
        double elevation = (platedata[i].potential - refPotential) / platedata[i].accMag;

        // Compute slope
        double cellNormal[3];
        polyData->getNormal(i, cellNormal);
        cellNormal[0] = -cellNormal[0];
        cellNormal[1] = -cellNormal[1];
        cellNormal[2] = -cellNormal[2];
        double slope = vsep_c(cellNormal, platedata[i].acc) * 180.0 / M_PI;

        foutE << elevation << endl;
        foutS << slope << endl;
    }

    foutE.close();
    foutS.close();

    return 0;
}

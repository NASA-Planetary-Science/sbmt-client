#include <iostream>
#include <fstream>
#include "gravity-werner.h"
#include "gravity-cheng.h"
#include "gravity-point.h"
#include "util.h"
#include <fenv.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>

using namespace std;

static const double g_G = 6.67384e-11 * 1.0e-9;

typedef enum GravityAlgorithmType
{
    WERNER,
    CHENG,
    POINT_SOURCE
} GravityAlgorithmType;

typedef enum HowToEvaluateAtPlate
{
    EVALUATE_AT_CENTER,
    AVERAGE_VERTICES,
    FROM_FILE
} HowToEvaluateAtPlate;

struct GravityResult
{
    double potential;
    double acc[3];
    bool filled;
};

static void usage()
{
    cout << "This program computes the gravitational acceleration and potential of a \n"
         << "shape model at specified points and saves the values to files.\n"
         << "Usage: gravity [options] <platemodel-file> <output-potential-file> <output-acceleration-file>\n"
         << "Options:\n"
         << " -d          Density of shape model in g/cm^3 (default is 1)\n"
         << " -r          Rotation rate of shape model in radians/sec (default is 0)\n"
         << " --werner    Use the Werner algorithm for computing the gravity (this is the default if neither --werner or --cheng option provided)\n"
         << " --cheng     Use Andy Cheng's algorithm for computing the gravity (default is to use Werner method if neither --werner or --cheng option provided)\n"
         << " --centers   Evaluate gravity directly at the centers of plates (this is the default if neither --centers or -vertices option provided)\n"
         << " --vertices  Evaluate gravity of each plate by avereging the gravity computed at the 3 vertices of the plate (default is to evaluate directly at centers) \n"
         << " --file      Evaluate gravity at points specified in file (default is to evaluate directly at centers of plates of shape model)\n";

    exit(0);
}

int main(int argc, char** argv)
{
    const int numberRequiredArgs = 3;
    if (argc-1 < numberRequiredArgs)
        usage();

    double density = 1.0;
    double omega = 0.0;
    double mass = 1.0;
    //double density = 2.67; // for Eros
    //double density = 1.95; // for Itokawa
    //double density = 3.42; // for Vesta
    //double density = 2.5;  // for Andy Cheng's gravity poster
    //double omega = 0.0003311657616706400000;// for Eros (in radians per second)
    //double omega = 0.000143857148947075; // for Itokawa (in radians per second)
    //double omega = (1617.3329428 / 86400.0) * (M_PI / 180.0); // for Vesta (in radians per second)
    GravityAlgorithmType gravityType = WERNER;
    HowToEvaluateAtPlate howToEvalute = EVALUATE_AT_CENTER;
    char* fieldpointsfile = 0;

    int i = 1;
    for(; i<argc; ++i)
    {
        if (!strcmp(argv[i], "-d"))
        {
            density = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "-r"))
        {
            omega = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "-m"))
        {
            mass = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "--werner"))
        {
            gravityType = WERNER;
        }
        else if (!strcmp(argv[i], "--cheng"))
        {
            gravityType = CHENG;
        }
        else if (!strcmp(argv[i], "--pointsource"))
        {
            gravityType = POINT_SOURCE;
        }
        else if (!strcmp(argv[i], "--centers"))
        {
            howToEvalute = EVALUATE_AT_CENTER;
        }
        else if (!strcmp(argv[i], "--vertices"))
        {
            howToEvalute = AVERAGE_VERTICES;
        }
        else if (!strcmp(argv[i], "--file"))
        {
            howToEvalute = AVERAGE_VERTICES;
            fieldpointsfile = argv[++i];
        }
        else
        {
            break;
        }
    }
    cout << argc << " " << i << endl;
    // There must be numRequiredArgs arguments remaining after the options. Otherwise abort.
    if (argc - i != numberRequiredArgs)
        usage();

    char* pltfile = argv[i];
    char* outputPot = argv[i+1];
    char* outputAcc = argv[i+2];


    Platemodel* polyData = 0;

    if (gravityType == WERNER)
        polyData = initializeGravityWerner(pltfile);
    else if (gravityType == CHENG)
        polyData = initializeGravityCheng(pltfile);
    else
        abort();

    ofstream foutP(outputPot);
    if (!foutP.is_open())
    {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }
    foutP.precision(16);

    ofstream foutA(outputAcc);
    if (!foutA.is_open())
    {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }
    foutA.precision(16);
    cout.precision(16);

    double acc[3] = {0.0, 0.0, 0.0};
    double potential = 0.0;

    if (howToEvalute == FROM_FILE)
    {
        ifstream fin(fieldpointsfile);
        if (fin.is_open())
        {
            string line;
            int count = 0;
            while (getline(fin, line))
            {
                vector<string> tokens = split(line);
                double pt[3] = {
                    atof(tokens[0].c_str()),
                    atof(tokens[1].c_str()),
                    atof(tokens[2].c_str())
                };

                if (gravityType == WERNER)
                    potential = 1.0e6*1.0e12*g_G*density*getGravityWerner(pt, acc);
                else
                    potential = 1.0e6*1.0e12*g_G*density*getGravityCheng(pt, acc);

                acc[0] *= 1.0e3 * 1.0e12 * g_G * density;
                acc[1] *= 1.0e3 * 1.0e12 * g_G * density;
                acc[2] *= 1.0e3 * 1.0e12 * g_G * density;

                // add centrifugal force
                if (omega != 0.0)
                {
                    potential -= 1.0e6 * 0.5 * omega*omega * (pt[0]*pt[0] + pt[1]*pt[1]);
                    acc[0] += 1.0e3 * omega*omega * pt[0];
                    acc[1] += 1.0e3 * omega*omega * pt[1];
                    // do nothing for z component
                }

                double accMag = Norm(acc);

                foutP << potential << endl;
                foutA << accMag << endl;

                if (count % 100 == 0)
                    cout << "Number plates completed: " << count << endl;
            }
        }
        else
        {
            cerr << "Error: Unable to open file '" << fieldpointsfile << "'" << endl;
            exit(1);
        }
    }
    else
    {
        vector<GravityResult> results;
        if (howToEvalute == AVERAGE_VERTICES)
        {
            int numPoints = polyData->getNumberOfPoints();
            results.resize(numPoints);
            for (int i=0; i<numPoints; ++i)
                results[i].filled = false;
        }

        int idList[3];

        int numPlates = polyData->getNumberOfPlates();
        for (int i=0; i<numPlates; ++i)
        {
            // Get center of cell
            polyData->getPlatePoints(i, idList);

            if (howToEvalute == EVALUATE_AT_CENTER)
            {
                double pt1[3];
                double pt2[3];
                double pt3[3];
                polyData->getPoint(idList[0], pt1);
                polyData->getPoint(idList[1], pt2);
                polyData->getPoint(idList[2], pt3);

                double center[3];

                TriangleCenter(pt1, pt2, pt3, center);

                if (gravityType == WERNER)
                    potential = 1.0e6*1.0e12*g_G*density*getGravityWerner(center, acc);
                else
                    potential = 1.0e6*1.0e12*g_G*density*getGravityCheng(center, acc);

                acc[0] *= 1.0e3 * 1.0e12 * g_G * density;
                acc[1] *= 1.0e3 * 1.0e12 * g_G * density;
                acc[2] *= 1.0e3 * 1.0e12 * g_G * density;

                // add centrifugal force
                if (omega != 0.0)
                {
                    potential -= 1.0e6 * 0.5 * omega*omega * (center[0]*center[0] + center[1]*center[1]);
                    acc[0] += 1.0e3 * omega*omega * center[0];
                    acc[1] += 1.0e3 * omega*omega * center[1];
                    // do nothing for z component
                }
            }
            else if(howToEvalute == AVERAGE_VERTICES)
            {
                double pt[3];
                for (int j=0; j<3; ++j)
                {
                    int ptId = idList[j];
                    GravityResult& result = results[ptId];
                    if (!result.filled)
                    {
                        polyData->getPoint(ptId, pt);

                        if (gravityType == WERNER)
                            result.potential = 1.0e6*1.0e12*g_G*density*getGravityWerner(pt, result.acc);
                        else
                            result.potential = 1.0e6*1.0e12*g_G*density*getGravityCheng(pt, result.acc);

                        result.acc[0] *= 1.0e3 * 1.0e12 * g_G * density;
                        result.acc[1] *= 1.0e3 * 1.0e12 * g_G * density;
                        result.acc[2] *= 1.0e3 * 1.0e12 * g_G * density;

                        // add centrifugal force
                        if (omega != 0.0)
                        {
                            result.potential -= 1.0e6 * 0.5 * omega*omega * (pt[0]*pt[0] + pt[1]*pt[1]);
                            result.acc[0] += 1.0e3 * omega*omega * pt[0];
                            result.acc[1] += 1.0e3 * omega*omega * pt[1];
                            // do nothing for z component
                        }

                        result.filled = true;
                    }

                    potential += result.potential;
                    acc[0] += result.acc[0];
                    acc[1] += result.acc[1];
                    acc[2] += result.acc[2];
                }

                potential /= 3.0;
                acc[0] /= 3.0;
                acc[1] /= 3.0;
                acc[2] /= 3.0;
            }

            double accMag = Norm(acc);

            foutP << potential << endl;
            foutA << accMag << endl;

            if (i % 100 == 0)
                cout << "Number plates completed: " << i << endl;
        }
    }

    foutP.close();
    foutA.close();
}

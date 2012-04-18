#include <iostream>
#include <fstream>
#include "gravity-werner.h"
#include "gravity-cheng.h"
#include "gravity-point.h"
#include <fenv.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>

using namespace std;

//static const double g_G = 6.67384e-11 * 1.0e-9;
static const double g_G = 6.673e-11 * 1.0e-9;

typedef enum GravityAlgorithmType
{
    WERNER,
    CHENG
} GravityAlgorithmType;

typedef enum HowToEvaluateAtPlate
{
    EVALUATE_AT_CENTER,
    AVERAGE_VERTICES
} HowToEvaluateAtPlate;

struct GravityResult
{
    double potential;
    double acc[3];
    bool filled;
};

static void usage()
{
    cout << "This program computes the gravitational acceleration and potential of a"
         <<   " shape model at specified points and saves the values to files ";
}

int main(int argc, char** argv)
{
    char* vtkfile = argv[1];
    char* outputPot = argv[2];
    char* outputAcc = argv[3];

    GravityAlgorithmType gravityType = WERNER;
    //double density = 2.67; // for Eros
    //double density = 1.95; // for Itokawa
    double density = 3.42; // for Vesta
    //double omega = 0.0003311657616706400000;// for Eros (in radians per second)
    //double omega = 0.000143857148947075; // for Itokawa (in radians per second)
    double omega = (1617.3329428 / 86400.0) * (M_PI / 180.0); // for Vesta (in radians per second)
    HowToEvaluateAtPlate howToEvalute = EVALUATE_AT_CENTER;

    for(int i = 1; i<argc; ++i)
    {
        if (!strcmp(argv[i], "-ed"))
        {

        }
        else if (!strcmp(argv[i], "-ep"))
        {

        }
    }

    Platemodel* polyData = 0;

    if (gravityType == WERNER)
        polyData = initializeGravityWerner(vtkfile);
    else if (gravityType == CHENG)
        polyData = initializeGravityCheng(vtkfile);
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


        double acc[3] = {0.0, 0.0, 0.0};
        double potential = 0.0;

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

//        potential *= (1.0e6 * 1.0e12 * g_G * density);
//        acc[0] *=    (1.0e3 * 1.0e12 * g_G * density);
//        acc[1] *=    (1.0e3 * 1.0e12 * g_G * density);
//        acc[2] *=    (1.0e3 * 1.0e12 * g_G * density);

        double accMag = Norm(acc);

        foutP << potential << endl;
        foutA << accMag << endl;

        if (i % 100 == 0)
            cout << "Number plates completed: " << i << endl;
    }

    foutP.close();
    foutA.close();
}

#include <vtkMath.h>
#include <vtkTriangle.h>
#include <vtkMassProperties.h>
#include <vtkIdList.h>
#include <iostream>
#include "gravity-werner.h"
#include "gravity-cheng.h"
#include "gravity-point.h"
#include <fenv.h>
#include <time.h>

using namespace std;

const double GM = 398600.4415; // gravitational constant times mass of earth in km^3 s^-2
static const double g_G = 6.67384e-11 * 1.0e-9;
//static const double g_G = 6.673e-11 * 1.0e-9;

// This compares timing and accuracy between the Werner and Cheng method
void compareChengAndWerner(const char* vtkfile)
{
    clock_t start, end;

    double density = 2.67e12; // of Eros in kg/km^3

    const int size = 1000;
    const double incr = 0.1;

    double chengPotentials[size];
    double wernerPotentials[size];
    double chengAccMags[size];
    double wernerAccMags[size];

    double pt[3] = {0.0, 0.0, 0.0};



    // First do Cheng method
    start = clock();
    vtkPolyData* polyData = initializeGravityCheng(vtkfile);
    for (int i=0; i<size; ++i)
    {
        //pt[0] = (double)i * incr;
        //pt[1] = (double)i * incr;
        pt[2] = (double)i * incr;


        double acc[3];
        double potential = 1.0e6*g_G*density*getGravityCheng(pt, acc);

        for (int j=0; j<3; ++j)
            acc[j] = 1.0e3 * g_G*density*acc[j];

        chengPotentials[i] = potential;

        chengAccMags[i] = vtkMath::Norm(acc);
    }
    end = clock();
    double chengdiff = double(end - start) / double(CLOCKS_PER_SEC);



    // Next do the Werner method
    start = clock();
    polyData = initializeGravityWerner(vtkfile);
    //end = clock();
    //double wernersetupdiff = double(end - start) / double(CLOCKS_PER_SEC);

    //start = clock();
    for (int i=0; i<size; ++i)
    {
        //pt[0] = (double)i * incr;
        //pt[1] = (double)i * incr;
        pt[2] = (double)i * incr;

        double acc[3];
        double potential = 1.0e6*g_G*density*getGravityWerner(pt, acc);

        for (int j=0; j<3; ++j)
            acc[j] = 1.0e3 * g_G*density*acc[j];

        wernerPotentials[i] = potential;

        wernerAccMags[i] = vtkMath::Norm(acc);
    }
    end = clock();
    double wernerdiff = double(end - start) / double(CLOCKS_PER_SEC);



    // print out results
    cout << "total Cheng  time: " << chengdiff << " seconds" << endl;
    cout << "total Werner time: " << wernerdiff << " seconds" << endl;
    //cout << "total Werner setup time: " << wernersetupdiff << " seconds" << endl;
    cout << "Ratio of Werner to Cheng: " << wernerdiff / chengdiff << endl << endl;

    cout << "z-coordinate Cheng-Potential Werner-Potential Potential-Error Cheng-AccMag Werner-AccMag AccMag-Error" << endl;
    for (int i=0; i<size; ++i)
    {
        //pt[0] = (double)i * incr;
        //pt[1] = (double)i * incr;
        pt[2] = (double)i * incr;

        cout << pt[2] << " " << chengPotentials[i] << " " << wernerPotentials[i] << " " << chengPotentials[i] - wernerPotentials[i] << " ";
        cout << chengAccMags[i] << " " << wernerAccMags[i] << " " << chengAccMags[i] - wernerAccMags[i] << endl;
    }


}

int main(int argc, char** argv)
{
    char* vtkfile = argv[1];
    char* output = argv[2];

//    compareChengAndWerner(vtkfile);
//    return 0;

    vtkPolyData* polyData = initializeGravityCheng(vtkfile);
    vtkPoints* points = polyData->GetPoints();

//    vtkMassProperties* massProperties = vtkMassProperties::New();
//    massProperties->SetInput(polyData);
//    massProperties->Update();
//    double volume = massProperties->GetVolume();

//    cout << "volume: " << volume << endl;
    vtkIdList* idList = vtkIdList::New();

    int numCells = polyData->GetNumberOfCells();
    //int numCells = polyData->GetNumberOfPoints();
    for (vtkIdType i=0; i<numCells; ++i)
    {
        double center[3];


        // Get center of cell
        polyData->GetCellPoints(i, idList);

        double pt1[3];
        double pt2[3];
        double pt3[3];
        points->GetPoint(idList->GetId(0), pt1);
        points->GetPoint(idList->GetId(1), pt2);
        points->GetPoint(idList->GetId(2), pt3);

        vtkTriangle::TriangleCenter(pt1, pt2, pt3, center);

        //double normal[3];
        //vtkTriangle::ComputeNormal(pt1, pt2, pt3, normal);
        //vtkMath::MultiplyScalar(normal, .1);

        // shift the center slightly in the normal direction
        //vtkMath::Add(center, normal, center);


/*
        points->GetPoint(i, center);
*/

        //center[0] = 0.0;
        //center[1] = 0.0;
        //center[2] = 6372.0;

//        cout << "center: " << center[0] << " " << center[1] << " " << center[2] << " " << endl;

        double density = 1.0;
        double omega = 0.0;

        // For Earth
        //density = 5.515e12;

        // For Eros
        density = 2.67e12; // of Eros in kg/km^3
        omega=0.0003311657616706400000;

        // For Itokawa
        //density = 1.90e12; // of Itokawa in kg/km^3


        // For Cheng poster
        //density = 2.5e12;

        // For PRISM
        //density = 2.6627e12;

        double acc[3];
        double potential = 1.0e6*g_G*density*getGravityCheng(center, acc);
        //double potential = 1.0e6*g_G*density*volume*getGravityPoint(center, acc);

        // add centrifugal force
        //potential -= 1.0e6 * 0.5 * omega*omega * (center[0]*center[0] + center[1]*center[1]);

        for (int j=0; j<3; ++j)
        {
            acc[j] = 1.0e3 * g_G * density * acc[j];
            //acc[j] = (1.0e3 * g_G * density * volume) * acc[j];
        }

        double accMag = vtkMath::Norm(acc);

        //cout << "mass: " << density*volume << endl;

//        cout << i << " potential: " << potential << endl;
//        cout << "acc: " << acc[0] << " " << acc[1] << " " << acc[2] << endl;
//        cout << "acc mag: " << accMag << endl << endl;



        double pot1 = 1.0e6*g_G*density*getGravityCheng(pt1, acc);
        double pot2 = 1.0e6*g_G*density*getGravityCheng(pt2, acc);
        double pot3 = 1.0e6*g_G*density*getGravityCheng(pt3, acc);
        pot1 -= 1.0e6*0.5 * omega*omega * (pt1[0]*pt1[0] + pt1[1]*pt1[1]);
        pot2 -= 1.0e6*0.5 * omega*omega * (pt2[0]*pt2[0] + pt2[1]*pt2[1]);
        pot3 -= 1.0e6*0.5 * omega*omega * (pt3[0]*pt3[0] + pt3[1]*pt3[1]);

        cout << (pot1+pot2+pot3)/3.0 << endl;

//        exit(0);
        if (i % 100 == 0)
            cout << "finished " << i << endl;
    }

}

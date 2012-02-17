#include <vtkMath.h>
#include <vtkTriangle.h>
#include <vtkMassProperties.h>
#include <vtkIdList.h>
#include <iostream>
#include "gravity.h"
#include "gravity2.h"
#include "gravity3.h"
#include <fenv.h>

using namespace std;

const double GM = 398600.4415; // gravitational constant times mass of earth in km^3 s^-2
//static const double g_G = 6.67384e-11 * 1.0e-9;
static const double g_G = 6.673e-11 * 1.0e-9;

int main(int argc, char** argv)
{
    char* vtkfile = argv[1];
    char* output = argv[2];

//    fetestexcept (FE_ALL_EXCEPT);

    vtkPolyData* polyData = initializeGravity3(vtkfile);
    vtkPoints* points = polyData->GetPoints();

    vtkMassProperties* massProperties = vtkMassProperties::New();
    massProperties->SetInput(polyData);
    massProperties->Update();
    double volume = massProperties->GetVolume();

    cout << "volume: " << volume << endl;
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

        double normal[3];
        vtkTriangle::ComputeNormal(pt1, pt2, pt3, normal);
        vtkMath::MultiplyScalar(normal, .1);

        // shift the center slightly in the normal direction
        vtkMath::Add(center, normal, center);


/*
        points->GetPoint(i, center);
*/

        //center[0] = 0.0;
        //center[1] = 0.0;
        //center[2] = 1.00001;

        //cout << "center: " << center[0] << " " << center[1] << " " << center[2] << " " << endl;


        double density = 2.67e12; // of Eros in kg/km^3
        //double density = 1.90e12; // of Itokawa in kg/km^3
        //double density = 2.5e12;
        //double density = 5.515e12;

        double factor = g_G * density;
        //double factor = 4.463e-4/volume; // GM of eros divided by its volume
        //double factor = GM / volume;

        double omega=0.0003311657616706400000;

        double acc[3];


        double potential = 1.0e6*factor*getPotential3(center, acc);
        //double potential = factor*getPotential(center, acc);
        //double potential = factor*getPotential(pt1, acc);

        // add centrifugal force
//        potential -= 1.0e6 * 0.5 * omega*omega * (center[0]*center[0] + center[1]*center[1]);

        //double accMag = 1.0e3 * (GM / 1.08321e12) * vtkMath::Norm(acc);
        double accMag = 1.0e3 * (GM / volume) * vtkMath::Norm(acc);
        //double accMag = 1.0e3 * (factor) * vtkMath::Norm(acc);
        cout << potential << endl;
        //cout << accMag << endl;


/*
        double pot1 = 1.0e6*factor*getPotential2(pt1, acc);
        double pot2 = 1.0e6*factor*getPotential2(pt2, acc);
        double pot3 = 1.0e6*factor*getPotential2(pt3, acc);
//        pot1 -= 1.0e6*0.5 * omega*omega * (pt1[0]*pt1[0] + pt1[1]*pt1[1]);
//        pot2 -= 1.0e6*0.5 * omega*omega * (pt2[0]*pt2[0] + pt2[1]*pt2[1]);
//        pot3 -= 1.0e6*0.5 * omega*omega * (pt3[0]*pt3[0] + pt3[1]*pt3[1]);

        cout << (pot1+pot2+pot3)/3.0 << endl;
*/
    }

}

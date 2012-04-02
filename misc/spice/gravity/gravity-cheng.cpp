#include <vector>
#include "platemodel.h"

using namespace std;

/*
  These functions compute gravitation potential and acceleration
  of a closed triangular plate model using the approximation derived by A. Cheng.
  */
struct FaceCenters
{
    double center[3];
    double normal[3]; // with length equal to twice plate area
};

static vector<FaceCenters> faceCenters;
static Platemodel* polyData = 0;

Platemodel* initializeGravityCheng(const char* vtkfile)
{
    if (polyData != 0)
        delete polyData;

    polyData = new Platemodel();

    int pointIds[3];
    // Compute the face data
    int numFaces = polyData->getNumberOfPlates();
    faceCenters.resize(numFaces);
    for (int i=0; i<numFaces; ++i)
    {
        FaceCenters fc;

        // Get center of cell
        polyData->getPlatePoints(i, pointIds);
        int p1 = pointIds[0];
        int p2 = pointIds[1];
        int p3 = pointIds[2];

        double pt1[3];
        double pt2[3];
        double pt3[3];
        polyData->getPoint(p1, pt1);
        polyData->getPoint(p2, pt2);
        polyData->getPoint(p3, pt3);

        TriangleCenter(pt1, pt2, pt3, fc.center);


        polyData->getNormal(i, fc.normal);

        double area = TriangleArea(pt1, pt2, pt3);
        MultiplyScalar(fc.normal, 2.0 * area);

        faceCenters[i] = fc;
    }

    return polyData;
}

double getGravityCheng(const double fieldPoint[3], double acc[3])
{
    double potential = 0.0;
    acc[0] = 0.0;
    acc[1] = 0.0;
    acc[2] = 0.0;

    // Compute the edge data
    int numFaces = polyData->getNumberOfPlates();
    for (int i=0; i<numFaces; ++i)
    {
        const FaceCenters& fc = faceCenters[i];

        double x_minus_R[3];
        Subtract(fieldPoint, fc.center, x_minus_R);

        double x_minus_R_dot_N = Dot(x_minus_R, fc.normal);
        double mag_x_minus_R = Norm(x_minus_R);

        if (mag_x_minus_R == 0.0)
            continue;

        potential +=  x_minus_R_dot_N / mag_x_minus_R;

        acc[0] -= ( (fc.normal[0] - x_minus_R[0] * x_minus_R_dot_N / (mag_x_minus_R*mag_x_minus_R)) / mag_x_minus_R );
        acc[1] -= ( (fc.normal[1] - x_minus_R[1] * x_minus_R_dot_N / (mag_x_minus_R*mag_x_minus_R)) / mag_x_minus_R );
        acc[2] -= ( (fc.normal[2] - x_minus_R[2] * x_minus_R_dot_N / (mag_x_minus_R*mag_x_minus_R)) / mag_x_minus_R );
    }

    potential *= 0.25;
    acc[0] *= 0.25;
    acc[1] *= 0.25;
    acc[2] *= 0.25;

    return potential;
}

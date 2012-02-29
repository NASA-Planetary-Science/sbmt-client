#include <vtkPolyDataReader.h>
#include <vtkPolyData.h>
#include <vtkCellData.h>
#include <vtkMath.h>
#include <vtkIdList.h>
#include <vtkPolyDataNormals.h>
#include <vtkTriangle.h>
#include <vector>

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
static vtkPolyData* polyData = 0;
static vtkIdList* idList = 0;

vtkPolyData* initializeGravity2(const char* vtkfile)
{
    vtkPolyDataReader* smallBodyReader = vtkPolyDataReader::New();
    smallBodyReader->SetFileName(vtkfile);
    smallBodyReader->Update();

    if (polyData != 0)
        polyData->Delete();

    vtkPolyDataNormals* normalsFilter = vtkPolyDataNormals::New();
    normalsFilter->SetInput(smallBodyReader->GetOutput());
    normalsFilter->SetComputeCellNormals(1);
    normalsFilter->SetComputePointNormals(0);
    normalsFilter->SplittingOff();
    normalsFilter->ConsistencyOn();
    normalsFilter->AutoOrientNormalsOn();
    normalsFilter->Update();

    polyData = vtkPolyData::New();
    polyData->ShallowCopy(normalsFilter->GetOutput());

    polyData->BuildCells();
    polyData->BuildLinks();


    vtkPoints* points = polyData->GetPoints();
    vtkDataArray* normals = polyData->GetCellData()->GetNormals();

    idList = vtkIdList::New();


    // Compute the face data
    int numFaces = polyData->GetNumberOfCells();
    for (vtkIdType i=0; i<numFaces; ++i)
    {
        FaceCenters fc;

        // Get center of cell
        polyData->GetCellPoints(i, idList);

        double pt1[3];
        double pt2[3];
        double pt3[3];
        points->GetPoint(idList->GetId(0), pt1);
        points->GetPoint(idList->GetId(1), pt2);
        points->GetPoint(idList->GetId(2), pt3);

        vtkTriangle::TriangleCenter(pt1, pt2, pt3, fc.center);


        normals->GetTuple(i, fc.normal);

        double area = vtkTriangle::TriangleArea(pt1, pt2, pt3);
        vtkMath::MultiplyScalar(fc.normal, 2.0 * area);

        faceCenters.push_back(fc);
    }

    return polyData;
}

double getGravity2(const double fieldPoint[3], double acc[3])
{
    double potential = 0.0;
    acc[0] = 0.0;
    acc[1] = 0.0;
    acc[2] = 0.0;

    // Compute the edge data
    int numFaces = polyData->GetNumberOfCells();
    for (vtkIdType i=0; i<numFaces; ++i)
    {
        const FaceCenters& fc = faceCenters[i];

        double x_minus_R[3];
        vtkMath::Subtract(fieldPoint, fc.center, x_minus_R);

        double x_minus_R_dot_N = vtkMath::Dot(x_minus_R, fc.normal);
        double mag_x_minus_R = vtkMath::Norm(x_minus_R);

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
